package com.fizzed.blaze.http;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;

public class HttpTest {

    private Config config;
    private ContextImpl context;
    private MockWebServer mockWebServer;

    @Before
    public void setup() throws Exception {
        this.config = ConfigHelper.create(null);
        this.context = spy(new ContextImpl(null, null, Paths.get("blaze.java"), config));
        this.mockWebServer = new MockWebServer();
    }

    private String getMockUrl() {
        return this.mockWebServer.url("/").toString();
    }

    @Test
    public void get() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201).setBody("ok"));

        Integer code = new Http(context).get(this.getMockUrl())
            .verbose()
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(rr.getMethod(), is("GET"));
    }

    @Test
    public void unexpectedStatusCode() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201).setBody("ok"));

        try {
            new Http(context).get(this.getMockUrl())
                .verbose()
                .statusCodes(200)
                .run();
        } catch (BlazeException e) {
            assertThat(e.getMessage(), containsString("code 201"));
        }
    }

    @Test
    public void statusCodesAny() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(499).setBody("ok"));

        Integer code = new Http(context).get(this.getMockUrl())
            .verbose()
            .statusCodesAny()
            .run();

        assertThat(code, is(499));
    }

    @Test
    public void postForm() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setBody("ok"));

        int code = new Http(context).post(this.getMockUrl())
            .verbose()
            .form("a", "b")
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(rr.getMethod(), is("POST"));
        assertThat(rr.getHeader("Content-Type"), is("application/x-www-form-urlencoded"));
        assertThat(rr.getBody().readUtf8(), is("a=b"));
    }

}