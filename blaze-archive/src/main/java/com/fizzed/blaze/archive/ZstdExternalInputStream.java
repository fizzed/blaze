package com.fizzed.blaze.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An InputStream that acts as a transparent decompression filter using the
 * external 'zstd' executable.
 * * Architecture:
 * 1. Spawns 'zstd -d -c' process.
 * 2. Starts a background thread to pipe raw compressed bytes into the process (stdin).
 * 3. Reads decompressed bytes from the process output (stdout).
 */
public class ZstdExternalInputStream extends InputStream {

    private final Process process;
    private final InputStream processStdout; // The decompressed data we read
    private final OutputStream processStdin; // Where we push compressed data
    private final Thread feederThread;
    private final InputStream sourceStream;

    // To propagate exceptions from the background thread to the main thread
    private final AtomicReference<IOException> feederException = new AtomicReference<>();

    /**
     * Creates a Zstd InputStream using the default "zstd" command.
     * * @param source The input stream containing compressed zstd data.
     * @throws IOException If the process cannot be started.
     */
    public ZstdExternalInputStream(InputStream source) throws IOException {
        this(source, "zstd");
    }

    /**
     * Creates a Zstd InputStream using a specific executable path.
     * * @param source The input stream containing compressed zstd data.
     * @param zstdCommand The command or path to the zstd executable (e.g., "/usr/bin/zstd").
     * @throws IOException If the process cannot be started.
     */
    public ZstdExternalInputStream(InputStream source, String zstdCommand) throws IOException {
        this.sourceStream = source;

        // -d: decompress
        // -c: force write to stdout
        ProcessBuilder pb = new ProcessBuilder(zstdCommand, "-d", "-c");

        // Capture stderr so it doesn't leak to console, but we aren't reading it
        // strictly in this implementation to keep it simple, though logging it is wise in prod.
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        this.process = pb.start();
        this.processStdout = process.getInputStream();
        this.processStdin = process.getOutputStream();

        // Start the background thread to feed data to the process
        this.feederThread = new Thread(new DataFeeder());
        this.feederThread.setDaemon(true); // Don't prevent JVM shutdown
        this.feederThread.setName("Zstd-Feeder-Thread");
        this.feederThread.start();
    }

    @Override
    public int read() throws IOException {
        checkFeederException();
        return processStdout.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkFeederException();
        return processStdout.read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        checkFeederException();
        return processStdout.available();
    }

    @Override
    public void close() throws IOException {
        // 1. Close the stream we are reading from
        try {
            processStdout.close();
        } catch (IOException ignored) {}

        // 2. Close the source stream
        try {
            sourceStream.close();
        } catch (IOException ignored) {}

        // 3. Kill the process if it's still alive
        if (process.isAlive()) {
            process.destroy();
        }

        // Note: We do not explicitly stop the feeder thread;
        // closing sourceStream or processStdin will cause it to exit naturally.
    }

    /**
     * Checks if the background writer thread encountered an error.
     * If so, re-throws it on the reader thread.
     */
    private void checkFeederException() throws IOException {
        IOException ex = feederException.get();
        if (ex != null) {
            throw new IOException("Exception occurred in zstd feeder thread", ex);
        }
    }

    /**
     * Background task that reads from the User's source stream
     * and writes into the ZSTD Process's Standard Input.
     */
    private class DataFeeder implements Runnable {
        @Override
        public void run() {
            byte[] buffer = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = sourceStream.read(buffer)) != -1) {
                    // Write compressed data to zstd process
                    processStdin.write(buffer, 0, bytesRead);
                }
                // Important: Flush and Close stdin tells zstd "That's all the data".
                // If we don't close this, zstd will wait forever.
                processStdin.flush();
                processStdin.close();
            } catch (IOException e) {
                // If the pipe broke because the main thread closed the stream,
                // that's expected. Otherwise, capture the error.
                if (!e.getMessage().contains("Broken pipe") &&
                    !e.getMessage().contains("Stream closed")) {
                    feederException.set(e);
                }
            }
        }
    }

}