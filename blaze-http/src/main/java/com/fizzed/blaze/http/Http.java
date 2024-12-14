package com.fizzed.blaze.http;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.VerboseLogger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class Http extends Action<Http.Result,Void> implements VerbosityMixin<Http> {

    static public class Result extends com.fizzed.blaze.core.Result<Http,Void,Result> {

        Result(Http action, Void value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;
    private final OkHttpClient.Builder clientBuilder;
    private final Request.Builder requestBuilder;
    private String method;
    private FormBody.Builder formBuilder;

    public Http(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.clientBuilder = new OkHttpClient.Builder();
        this.requestBuilder = new Request.Builder();
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

    public Http form(String param, String value) {
        if (this.formBuilder == null) {
            this.formBuilder = new FormBody.Builder();
        }
        this.formBuilder.add(param, value);
        return this;
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

        final OkHttpClient client = this.clientBuilder
            .addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Chain chain) throws IOException {
                    Request r = chain.request();
                    log.verbose("Http {} {}", r.method(), r.url());
                    return chain.proceed(r);
                }
            })
            .build();

        try (Response response = client.newCall(request).execute()) {
            Result result = new Result(this, null);

            return result;
        } catch (IOException e) {
            throw new BlazeException(e.getMessage(), e);
        }
    }

}
