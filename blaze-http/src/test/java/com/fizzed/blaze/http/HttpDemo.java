package com.fizzed.blaze.http;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.fizzed.blaze.Https.httpGet;

public class HttpDemo {

    static public void main(String[] args) throws Exception {
        final Path file = Files.createTempFile("test", ".tar.gz");
        try {
            httpGet("https://dl.fizzed.com/java/jdk-8u11-linux-x64.tar.gz")
                //.progress()
                .target(file)
                .run();
        }  finally {
            Files.deleteIfExists(file);
        }
    }

}