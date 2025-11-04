package com.fizzed.blaze.http.okhttp;

import com.fizzed.blaze.util.TerminalIOProgressBar;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

import java.io.IOException;

import static com.fizzed.blaze.util.TerminalHelper.clearLine;

/**
 * A custom {@link RequestBody} implementation that wraps an existing {@link RequestBody} and allows
 * monitoring of write progress by updating a progress bar during the write operation.
 *
 * This class is useful for tracking the upload progress of large files.
 */
public class ProgressRequestBody extends RequestBody {

    private final RequestBody delegate;

    public ProgressRequestBody(RequestBody requestBody) {
        this.delegate = requestBody;
    }

    @Override
    public MediaType contentType() {
        return this.delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return this.delegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        final long contentLength = this.delegate.contentLength();
        final TerminalIOProgressBar progressBar = new TerminalIOProgressBar(contentLength);

        final ForwardingSink progressForwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                progressBar.update(byteCount);
                if (progressBar.isRenderStale(1)) {
                    System.out.print(clearLine(progressBar.render()));
                }
            }
        };

        // unfortunately, we can only writeTo a buffered sink, while we used the ForwardingSink to provide the progress
        // so we'll wrap the forwarding sink to provide a buffered sink
        final BufferedSink progressBufferedSink = Okio.buffer(progressForwardingSink);

        this.delegate.writeTo(progressBufferedSink);

        progressBufferedSink.flush(); // Crucial to ensure all bytes are written

        // one last render to clear the line
        System.out.print(clearLine(""));
    }

}