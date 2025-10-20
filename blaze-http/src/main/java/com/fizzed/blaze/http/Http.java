package com.fizzed.blaze.http;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.internal.IntRangeHelper;
import com.fizzed.blaze.util.*;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Http extends Action<Http.Result,Integer> implements VerbosityMixin<Http> {

    static public final String METHOD_HEAD = "HEAD";
    static public final String METHOD_GET = "GET";
    static public final String METHOD_POST = "POST";
    static public final String METHOD_PUT = "PUT";
    static public final String METHOD_PATCH = "PATCH";
    static public final String METHOD_DELETE = "DELETE";

    static public class Result extends com.fizzed.blaze.core.Result<Http,Integer,Result> {

        Result(Http action, Integer value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;
    private final OkHttpClient.Builder clientBuilder;
    private final Request.Builder requestBuilder;
    private final String method;
    private FormBody.Builder formBuilder;
    private RequestBody body;
    private final List<IntRange> statusCodes;
    private StreamableOutput target;
    private boolean progress;

    public Http(Context context, String method, String url) {
        super(context);
        this.log = new VerboseLogger(this);
        this.clientBuilder = new OkHttpClient.Builder();
        this.requestBuilder = new Request.Builder();
        this.statusCodes = new ArrayList<>();
        this.statusCodes.add(new IntRange(200, 299));
        this.requestBuilder.url(url);
        this.method = method;
        this.progress = false;
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public Http statusCodes(Integer ... codes) {
        this.statusCodes.clear();
        for (Integer code : codes) {
            this.statusCodes.add(new IntRange(code, code));
        }
        return this;
    }

    public Http statusCodes(IntRange... intRanges) {
        this.statusCodes.clear();
        this.statusCodes.addAll(Arrays.asList(intRanges));
        return this;
    }

    public Http statusCodesAny() {
        this.statusCodes.clear();
        return this;
    }

    public Http header(String name, String value) {
        this.requestBuilder.header(name, value);
        return this;
    }

    public Http addHeader(String name, String value) {
        this.requestBuilder.addHeader(name, value);
        return this;
    }

    public Http form(String param, String value) {
        if (this.formBuilder == null) {
            this.formBuilder = new FormBody.Builder();
        }
        this.formBuilder.add(param, value);
        return this;
    }

    public Http body(Path file, String contentType) {
        this.body = RequestBody.create(file.toFile(), MediaType.parse(contentType));
        return this;
    }

    public Http body(byte[] bytes, String contentType) {
        this.body = RequestBody.create(bytes, MediaType.parse(contentType));
        return this;
    }

    public Http body(String str, String contentType) {
        this.body = RequestBody.create(str, MediaType.parse(contentType));
        return this;
    }

    public Http target(Path file) {
        this.target = Streamables.output(file);
        return this;
    }

    public Http target(File file) {
        this.target = Streamables.output(file);
        return this;
    }

    public Http target(String file) {
        return this.target(Paths.get(file));
    }

    public Http target(StreamableOutput output) {
        this.target = output;
        return this;
    }

    public Http progress() {
        return this.progress(true);
    }

    public Http progress(boolean progress) {
        this.progress = progress;
        return this;
    }

    private void require(RequestBody requestBody) {
        if (requestBody == null) {
            throw new BlazeException("Request body must be set");
        }
    }

    public CaptureOutput runCaptureOutput() throws BlazeException {
        CaptureOutput captureOutput;
        // already set as capture output?
        if (this.target != null && this.target instanceof CaptureOutput) {
            captureOutput = (CaptureOutput)this.target;
        } else {
            captureOutput = Streamables.captureOutput(false);
            this.target = captureOutput;
        }

        this.run();

        return captureOutput;
    }

    @Override
    protected Result doRun() throws BlazeException {
        if (this.method == null) {
            throw new BlazeException("Method must be set by calling .get(), .post(), etc.");
        }

        // is there a request body?
        RequestBody requestBody = null;
        if (this.formBuilder != null) {
            requestBody = this.formBuilder.build();
        } else if (this.body != null) {
            requestBody = this.body;
        }

        switch (this.method.toLowerCase()) {
            case "get":
                this.requestBuilder.get();
                break;
            case "delete":
                // delete does NOT always require a body
                if (requestBody != null) {
                    this.requestBuilder.delete(requestBody);
                } else {
                    this.requestBuilder.delete();
                }
                break;
            default:
                // everything else requires a body
                this.require(requestBody);
                this.requestBuilder.method(this.method, requestBody);
                break;
        }

        final Request request = this.requestBuilder.build();

        final long startTime = System.currentTimeMillis();
        final AtomicBoolean isFirstRequest = new AtomicBoolean(true);
        final OkHttpClient client = this.clientBuilder
            .addNetworkInterceptor(new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Chain chain) throws IOException {
                    final Request _request = chain.request();
                    if (isFirstRequest.get()) {
                        log.info("Http request method={} url={}", request.method(), request.url());
                        if (log.isDebug()) {
                            _request.headers().forEach(h -> {
                                log.debug("{}: {}", h.getFirst(), h.getSecond());
                            });
                        }
                    }
                    isFirstRequest.set(false);
                    final Response response = chain.proceed(_request);
                    if (!response.isRedirect()) {
                        log.info("Http response method={}, url={}, code={}, protocol={} (in {} ms)",
                            request.method(), request.url(), response.code(), response.protocol(), (System.currentTimeMillis() - startTime));
                        if (log.isDebug()) {
                            response.headers().forEach(h -> {
                                log.debug("{}: {}", h.getFirst(), h.getSecond());
                            });
                        }
                    }
                    return response;
                }
            })
            .build();

        try (Response response = client.newCall(request).execute()) {
            Result result = new Result(this, response.code());

            // was the status code what we expect?
            if (!this.statusCodes.isEmpty()) {
                if (!IntRangeHelper.contains(this.statusCodes, response.code())) {
                    throw new BlazeException("Unexpected http response code " + response.code());
                }
            }

            // what to do with the response?
            if (this.target != null) {
                final ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    final long knownContentLength = responseBody.contentLength();

                    // should we activate the progress bar?
                    final ConsoleIOProgressBar progressBar;
                    if (this.progress && (knownContentLength > 0 || knownContentLength == -1)) {
                        progressBar = new ConsoleIOProgressBar(knownContentLength);
                    } else {
                        progressBar = null;
                    }

                    try (InputStream is = responseBody.byteStream()) {
                        try (OutputStream os = this.target.stream()) {
                            byte[] buffer = new byte[8192];
                            int n;
                            while (-1 != (n = is.read(buffer))) {
                                os.write(buffer, 0, n);

                                if (progressBar != null) {
                                    progressBar.update(n);
                                    if (progressBar.isRenderStale(1)) {
                                        System.out.print("\r" + progressBar.render());
                                    }
                                }
                            }
                        }
                    }

                    // we need 1 more render to make sure it shows 100% and to newline it
                    if (progressBar != null) {
                        System.out.println("\r" + progressBar.render());
                    }
                }
            }

            return result;
        } catch (IOException e) {
            throw new BlazeException(e.getMessage(), e);
        }
    }

}
