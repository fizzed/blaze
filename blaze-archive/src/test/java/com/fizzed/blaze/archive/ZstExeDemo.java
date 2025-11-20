package com.fizzed.blaze.archive;

import com.fizzed.blaze.archive.zstd.ZstdNativeInputStream;
import com.fizzed.crux.util.Resources;

import java.io.IOException;
import java.nio.file.Path;

public class ZstExeDemo {

    static public void main(String[] args) throws Exception {
        // the "zst" executable can provide a stream of decompressed data via stdin/stdout
        final Path file = Resources.file("/fixtures/hello.txt.zst");


        // NOTE: To run this test, you need 'zstd' installed and in your PATH.
        // You also need a file named 'test.txt.zst' in the working directory.

        System.out.println("Starting Zstd External Process Test...");

        // Create a dummy test file if you want to test programmatically:
        // echo "Hello World from ZSTD" | zstd > test.txt.zst

        try (java.io.FileInputStream fileIn = new java.io.FileInputStream(file.toFile());
             ZstdNativeInputStream zstdIn = new ZstdNativeInputStream(fileIn)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = zstdIn.read(buffer)) > 0) {
                System.out.print(new String(buffer, 0, len));
            }
            System.out.println("\n\nDone reading.");

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure 'test.txt.zst' exists and 'zstd' is installed.");
        }
    }

}