package com.fizzed.blaze.http;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.IntMatcher;
import com.fizzed.blaze.util.VerboseLogger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Http extends Action<Http.Result,Integer> implements VerbosityMixin<Http> {

    static public class Result extends com.fizzed.blaze.core.Result<Http,Integer,Result> {

        Result(Http action, Integer value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;
    private final OkHttpClient.Builder clientBuilder;
    private final Request.Builder requestBuilder;
    private String method;
    private FormBody.Builder formBuilder;
    private Path target;
    private List<IntMatcher> statusCodes;

    public Http(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.clientBuilder = new OkHttpClient.Builder();
        this.requestBuilder = new Request.Builder();
        this.statusCodes = new ArrayList<>();
        this.statusCodes.add(new IntMatcher(200, 299));
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    public Http get(String url) {
        this.requestBuilder.url(url);
        this.method = "get";
        return this;
    }

    public Http post(String url) {
        this.requestBuilder.url(url);
        this.method = "post";
        return this;
    }

    public Http statusCodes(Integer ... codes) {
        this.statusCodes.clear();
        for (Integer code : codes) {
            this.statusCodes.add(new IntMatcher(code, code));
        }
        return this;
    }

    public Http statusCodes(IntMatcher ... intMatchers) {
        this.statusCodes.clear();
        this.statusCodes.addAll(Arrays.asList(intMatchers));
        return this;
    }

    public Http statusCodesAny() {
        this.statusCodes.clear();
        return this;
    }

    public Http form(String param, String value) {
        if (this.formBuilder == null) {
            this.formBuilder = new FormBody.Builder();
        }
        this.formBuilder.add(param, value);
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

    public Http target(Path target) {
        this.target = target;
        return this;
    }

    public Http target(File target) {
        return this.target(target.toPath());
    }

    public Http target(String target) {
        return this.target(Paths.get(target));
    }

    @Override
    protected Result doRun() throws BlazeException {
        if (this.method == null) {
            throw new BlazeException("Method must be set by calling .get(), .post(), etc.");
        }

        // is there a request body?
        RequestBody body = null;
        if (this.formBuilder != null) {
            body = this.formBuilder.build();
        }

        switch (this.method) {
            case "get":
                this.requestBuilder.get();
                break;
            case "post":
                this.requestBuilder.post(body);
                break;
            case "delete":
                this.requestBuilder.delete(body);
                break;
            default:
                throw new BlazeException("Unknown http method: " + this.method);
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
                boolean matched = false;
                for (IntMatcher intMatcher : this.statusCodes) {
                    if (intMatcher.matches(response.code())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    throw new BlazeException("Unexpected http response code " + response.code());
                }
            }

            // what to do with the response?
            if (this.target != null) {
                final ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    try (InputStream is = responseBody.byteStream()) {
                        Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            return result;
        } catch (IOException e) {
            throw new BlazeException(e.getMessage(), e);
        }
    }

}
