package com.fizzed.blaze.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoHelper {

    static public void copy(InputStream input, OutputStream output, boolean progress, long knownContentLength) throws IOException {
        // should we activate the progress bar?
        final ConsoleIOProgressBar progressBar;
        if (progress) {
            progressBar = new ConsoleIOProgressBar(knownContentLength);
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
                    System.out.print("\r" + progressBar.render());
                }
            }
        }

        // we need 1 more render to make sure it shows 100% and to newline it
        if (progressBar != null) {
            System.out.println("\r" + progressBar.render());
        }
    }

}