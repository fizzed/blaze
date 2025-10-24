package com.fizzed.blaze.util;

import java.util.concurrent.TimeUnit;

public class TerminalIOProgressBar {

    private final long totalBytes;
    private long bytesProcessed;
    private final long startTime;

    private long lastRenderTime;
    private long lastBytesProcessed;
    private int spinnerState = 0;
    private static final char[] SPINNER_CHARS = {'|', '/', '-', '\\'};

    /**
     * Constructs a new NetworkProgress tracker.
     * @param totalBytes The total number of bytes expected in the operation.
     */
    public TerminalIOProgressBar(long totalBytes) {
        if (totalBytes == 0) {
            throw new IllegalArgumentException("Total bytes must be a positive number.");
        }
        this.totalBytes = totalBytes;
        this.startTime = System.nanoTime();
        this.lastRenderTime = this.startTime;
        this.bytesProcessed = 0;
        this.lastBytesProcessed = 0;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Updates the progress by adding the number of bytes processed since the last update.
     *
     * @param bytesJustProcessed The number of bytes processed in the latest chunk.
     */
    public void update(long bytesJustProcessed) {
        this.bytesProcessed += bytesJustProcessed;
    }

    public boolean isRenderStale(int seconds) {
        long now = System.nanoTime();
        long timeElapsedSinceLastUpdate = now - this.lastRenderTime;
        return timeElapsedSinceLastUpdate > TimeUnit.SECONDS.toNanos(seconds);
    }

    /**
     * Generates and returns the formatted progress bar string.
     *
     * @return A string representing the current progress, e.g.,
     * "75% [=======>   ] 75.00 MB / 100.00 MB | 10.5 MB/s | ETA: 00:02:23"
     */
    public String render() {
        // If total size is unknown, show a different style of progress bar.
        if (this.totalBytes < 0) {
            return renderUnknownSizeProgressBar();
        }

        long now = System.nanoTime();
        long currentBytes = this.bytesProcessed;

        // The time delta is now correctly calculated between calls to this method.
        long timeElapsedSinceLastUpdate = now - this.lastRenderTime;
        long bytesProcessedSinceLastUpdate = currentBytes - this.lastBytesProcessed;

        double percentage = (double) currentBytes * 100 / totalBytes;

        // Calculate speed based on the last update interval for a more dynamic reading.
        double speed = 0;
        if (timeElapsedSinceLastUpdate > 0) {
            speed = (double) bytesProcessedSinceLastUpdate * 1_000_000_000 / timeElapsedSinceLastUpdate;
        }

        // Calculate ETA based on average speed since the start for more stability.
        long timeElapsedTotal = now - startTime;
        long etaSeconds = 0;
        if (currentBytes > 0 && timeElapsedTotal > 0) {
            long remainingBytes = totalBytes - currentBytes;
            double averageSpeed = (double) currentBytes * 1_000_000_000 / timeElapsedTotal;
            if (averageSpeed > 0) {
                etaSeconds = (long) (remainingBytes / averageSpeed);
            }
        }

        // Update the state for the *next* call to getProgressBar.
        // This must be done after the calculations for the current call.
        this.lastBytesProcessed = currentBytes;
        this.lastRenderTime = now;

        return renderKnownSizeProgressBar(percentage, speed, etaSeconds, currentBytes);
    }

    /**
     * Generates and returns a progress string for operations with unknown total size.
     * Displays a spinner, bytes processed, current speed, and elapsed time.
     */
    private String renderUnknownSizeProgressBar() {
        long now = System.nanoTime();
        long currentBytes = this.bytesProcessed;

        long timeElapsedSinceLastUpdate = now - lastRenderTime;
        long bytesProcessedSinceLastUpdate = currentBytes - lastBytesProcessed;

        // Calculate speed
        double speed = 0;
        if (timeElapsedSinceLastUpdate > 0) {
            speed = (double) bytesProcessedSinceLastUpdate * 1_000_000_000 / timeElapsedSinceLastUpdate;
        }

        // Calculate total elapsed time
        long timeElapsedTotal = now - startTime;
        long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(timeElapsedTotal);


        // Update state for next call
        this.lastBytesProcessed = currentBytes;
        this.lastRenderTime = now;

        // Build the string
        StringBuilder sb = new StringBuilder();
        char spinnerChar = SPINNER_CHARS[spinnerState++ % SPINNER_CHARS.length];

        sb.append("  ");
        sb.append(String.format("[%c] ", spinnerChar));
        sb.append(String.format("%s processed", formatBytes(currentBytes)));
        sb.append(String.format(" | %s/s", formatBytes((long) speed)));
        sb.append(String.format(" | Elapsed: %s", formatDuration(elapsedSeconds)));

        return sb.toString();
    }

    /**
     * Helper method to construct the final progress string.
     */
    private String renderKnownSizeProgressBar(double percentage, double speed, long etaSeconds, long currentBytes) {
        StringBuilder sb = new StringBuilder();
        int barWidth = 25; // Width of the progress bar in characters

        sb.append("  ");
        sb.append(String.format("%6.2f%% ", percentage));
        sb.append("[");
        int progress = (int) (percentage * barWidth / 100);
        for (int i = 0; i < barWidth; i++) {
            if (i < progress) {
                sb.append("=");
            } else if (i == progress) {
                sb.append(">");
            } else {
                sb.append(" ");
            }
        }
        sb.append("] ");
        sb.append(String.format("%s / %s", formatBytes(currentBytes), formatBytes(totalBytes)));
        sb.append(String.format(" | %s/s", formatBytes((long) speed)));
        sb.append(String.format(" | ETA: %s", formatDuration(etaSeconds)));

        return sb.toString();
    }

    /**
     * Formats bytes into a human-readable string (KB, MB, GB, etc.).
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Formats a duration in seconds into HH:mm:ss format.
     */
    private String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "--:--:--";
        }
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Main method to demonstrate the NetworkProgress class.
     * Simulates a file download.
     */
    public static void main(String[] args) {
        final long TOTAL_BYTES = 100 * 1024 * 1024; // 100 MB

        TerminalIOProgressBar progressBar = new TerminalIOProgressBar(TOTAL_BYTES);
//        TerminalIOProgressBar2 progressBar = new TerminalIOProgressBar2(-1);

        System.out.println("Starting simulated network download...");

        long bytesDownloaded = 0;
        while (bytesDownloaded < TOTAL_BYTES) {
            // Simulate downloading a random chunk of data
            long chunkSize = (long) (Math.random() * 512 * 1024); // up to 512KB
            bytesDownloaded += chunkSize;
            if (bytesDownloaded > TOTAL_BYTES) {
                bytesDownloaded = TOTAL_BYTES;
            }

            progressBar.update(chunkSize);

            if (progressBar.isRenderStale(1)) {
                // Print the progress bar, using \r to overwrite the line
                System.out.print("\r" + progressBar.render());
            }

            try {
                // Simulate network latency
                Thread.sleep(50 + (long) (Math.random() * 200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("\nDownload interrupted.");
                return;
            }
        }

        System.out.println("\nDownload complete.");
    }

}