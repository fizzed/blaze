package com.fizzed.blaze.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import static com.fizzed.blaze.util.TerminalHelper.clearLinePrint;

public class IoHelper {

    static public void copy(Path input, OutputStream output, boolean progress, boolean clearProgressLineAtEnd, OpenOption... options) throws IOException {
        final long knownContentLength = Files.size(input);
        try (InputStream is = Files.newInputStream(input, options)) {
            copy(is, output, progress, clearProgressLineAtEnd, knownContentLength);
        }
    }

    static public void copy(InputStream input, Path output, boolean progress, boolean clearProgressLineAtEnd, OpenOption... options) throws IOException {
        copy(input, output, progress, clearProgressLineAtEnd, -1L, options);
    }

    static public void copy(InputStream input, Path output, boolean progress, boolean clearProgressLineAtEnd, long knownContentLength, OpenOption... options) throws IOException {
        try (OutputStream os = Files.newOutputStream(output, options)) {
            copy(input, os, progress, clearProgressLineAtEnd, knownContentLength);
        }
    }

    static public void copy(InputStream input, OutputStream output, boolean progress, boolean clearProgressLineAtEnd, long knownContentLength) throws IOException {
        // should we activate the progress bar?
        final TerminalIOProgressBar progressBar;
        if (progress) {
            progressBar = new TerminalIOProgressBar(knownContentLength);
        } else {
            progressBar = null;
        }

        byte[] buffer = new byte[8192];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);

            if (progressBar != null) {
                progressBar.update(n);
                if (progressBar.isRenderStale(1)) {
                    clearLinePrint(progressBar.render());
                }
            }
        }

        // we need 1 more render to make sure it shows 100%
        if (progressBar != null) {
            if (clearProgressLineAtEnd) {
                clearLinePrint();
            } else {
                clearLinePrint(progressBar.render());
            }
        }
    }

}