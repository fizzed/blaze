package com.fizzed.blaze.vfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Checksums {

    // 8KB is the standard optimal buffer size for most file systems
    static private final int BUFFER_SIZE = 32768;

    // Lookup table for fast hex conversion (lowercase to match linux md5sum)
    static private final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    // The standard POSIX CRC polynomial (0x04C11DB7)
    static private final int[] CRC_TABLE = new int[256];
    static {
        // Generate the table at class loading time
        for (int i = 0; i < 256; ++i) {
            int entry = i << 24;
            for (int j = 0; j < 8; ++j) {
                if ((entry & 0x80000000) != 0) {
                    entry = (entry << 1) ^ 0x04C11DB7;
                } else {
                    entry = entry << 1;
                }
            }
            CRC_TABLE[i] = entry;
        }
    }

    /**
     * Calculates the POSIX standard 'cksum' (CRC32 + Length)
     *
     * @param input The input stream of data to checksum
     * @return The long value representing the unsigned 32-bit checksum
     */
    static public long cksum(InputStream input) throws IOException {
        int crc = 0;
        long length = 0;

        // Optimization: Create a buffer to read 8KB chunks
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        // 1. Process file in chunks
        while ((bytesRead = input.read(buffer)) != -1) {
            length += bytesRead;

            // Process the chunk in memory (Fast CPU loop, no I/O overhead)
            for (int i = 0; i < bytesRead; i++) {
                // IMPORTANT: buffer[i] is a signed byte (-128 to 127).
                // We use & 0xFF to convert it to an unsigned int (0 to 255)
                // just like input.read() would have returned.
                int b = buffer[i] & 0xFF;
                crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) ^ b) & 0xFF];
            }
        }

        // 2. POSIX requirement: Append the length of the file to the stream.
        // (This logic remains exactly the same as your original code)
        long tempLength = length;
        do {
            int byteVal = (int) (tempLength & 0xFF);
            crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) ^ byteVal) & 0xFF];
            tempLength >>>= 8;
        } while (tempLength > 0);

        // 3. Final bit inversion
        return (~crc) & 0xFFFFFFFFL;
    }

    /**
     * Calculates the MD5 hash of an InputStream.
     * * @param inputStream The stream to read. This method reads until EOF
     * but does NOT close the stream.
     * @return The 32-character hexadecimal string (lowercase).
     * @throws IOException If an I/O error occurs.
     */
    public static String hash(String algorithm, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        MessageDigest digest;

        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            // MD5 is a standard algorithm guaranteed to be present in all JVMs
            throw new RuntimeException(algorithm + " algorithm not found", e);
        }

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        return bytesToHex(digest.digest());
    }

    /**
     * Extremely fast conversion of raw bytes to a Hex String.
     * Avoiding String.format() improves performance significantly.
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            // High nibble
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            // Low nibble
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    static public class HashEntry {

        private final long cksum;
        private final String hash;
        private final String file;

        public HashEntry(long cksum, String hash, String file) {
            this.cksum = cksum;
            this.hash = hash;
            this.file = file;
        }

        public long getCksum() {
            return cksum;
        }

        public String getHash() {
            return hash;
        }

        public String getFile() {
            return file;
        }

        @Override
        public String toString() {
            return "HashEntry{" + "cksum=" + cksum + ", hash=" + hash + ", file=" + file + '}';
        }
    }

    static public List<HashEntry> parsePosixCksumOutput(String output) {
        final String[] lines = output.split("\n");
        final List<HashEntry> entries = new java.util.ArrayList<>(lines.length);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                // skip empty lines
                continue;
            }

            int spacePos1 = line.indexOf(" ");
            int spacePos2 = line.indexOf(" ", spacePos1 + 1);

            if (spacePos1 < 0 || spacePos2 < 0) {
                throw new IllegalArgumentException("Invalid cksum file output: " + line);
            }

            final long cksum = Long.parseLong(line.substring(0, spacePos1).trim());
            final long size = Long.parseLong(line.substring(spacePos1 + 1, spacePos2).trim());
            final String file = line.substring(spacePos2 + 1).trim();

            entries.add(new HashEntry(cksum, null, file));
        }

        return entries;
    }

    static public List<HashEntry> parsePosixHashOutput(String output) {
        final String[] lines = output.split("\n");
        final List<HashEntry> entries = new java.util.ArrayList<>(lines.length);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                // skip empty lines
                continue;
            }

            int spacePos = line.indexOf(" ");

            if (spacePos < 0) {
                throw new IllegalArgumentException("Invalid hash file output: " + line);
            }

            final String hash = line.substring(0, spacePos).trim();
            final String file = line.substring(spacePos + 1).trim();

            entries.add(new HashEntry(0L, hash.toLowerCase(), file));
        }

        return entries;
    }

    static public List<HashEntry> parsePowershellHashFileOutput(String output) {
        final String[] lines = output.split("\n");
        final List<HashEntry> entries = new java.util.ArrayList<>(lines.length);

        String hash = null;
        String file = null;

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                // skip empty lines
                continue;
            }

            int colonPos = line.indexOf(":");

            if (colonPos < 0) {
                // this line probably represents a "wrapped" file name
                String fileToAppend = line.trim();
                // file MUST not be null
                if (file == null) {
                    throw new RuntimeException("Unexpected null file name in powershell hash file output - unable to parse " + output);
                }
                file += fileToAppend;
            } else {
                // we are ready to keep whatever we read last
                if (hash != null && file != null) {
                    // always use lowercase for hash
                    entries.add(new HashEntry(0L, hash.toLowerCase(), file));
                    hash = null;
                    file = null;
                }

                final String key = line.substring(0, colonPos).trim();
                final String value = line.substring(colonPos + 1).trim();

                if (key.equalsIgnoreCase("hash")) {
                    hash = value;
                } else if (key.equalsIgnoreCase("path")) {
                    file = value;
                } else {
                    throw new RuntimeException("Unexpected key '" + key + "' in powershell hash file output");
                }
            }
        }

        // we are ready to keep whatever we read last
        if (hash != null && file != null) {
            // always use lowercase for hash
            entries.add(new HashEntry(0L, hash.toLowerCase(), file));
        }

        return entries;
    }

}