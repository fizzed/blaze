package com.fizzed.blaze.vfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Cksums {

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
        int b;

        // 1. Process all file bytes
        while ((b = input.read()) != -1) {
            crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) ^ b) & 0xFF];
            length++;
        }

        // 2. POSIX requirement: Append the length of the file to the stream.
        // We append the length in Little Endian byte order, one byte at a time,
        // stripping high-order null bytes, but ensuring at least one byte is written.
        long tempLength = length;
        do {
            int byteVal = (int) (tempLength & 0xFF);
            crc = (crc << 8) ^ CRC_TABLE[((crc >>> 24) ^ byteVal) & 0xFF];
            tempLength >>>= 8;
        } while (tempLength > 0);

        // 3. Final bit inversion
        return (~crc) & 0xFFFFFFFFL; // Mask to return as unsigned 32-bit integer
    }

    static public class Entry {

        private final long cksum;
        private final long size;
        private final String file;

        Entry(long cksum, long size, String file) {
            this.cksum = cksum;
            this.size = size;
            this.file = file;
        }

        public long getCksum() {
            return cksum;
        }

        public long getSize() {
            return size;
        }

        public String getFile() {
            return file;
        }

    }

    static public List<Entry> parse(String output) {
        final String[] lines = output.split("\n");
        final List<Entry> entries = new java.util.ArrayList<>(lines.length);

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                // skip empty lines
                continue;
            }

            final String[] parts = line.split(" ");
            final long cksum = Long.parseLong(parts[0].trim());
            final long size = Long.parseLong(parts[1].trim());
            final String file = parts[2].trim();

            entries.add(new Entry(cksum, size, file));
        }

        return entries;
    }

}