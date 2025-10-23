package com.fizzed.blaze.http;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import com.fizzed.blaze.util.CaptureOutput;
import com.fizzed.blaze.util.Streamables;
import com.fizzed.crux.util.TemporaryPath;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
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
        this.config = ConfigHelper.createEmpty();
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

        Integer code = new Http(context, Http.METHOD_GET, this.getMockUrl())
            .verbose()
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(rr.getMethod(), is("GET"));
    }

    @Test
    public void getToString() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201).setBody("ok"));

        CaptureOutput output = Streamables.captureOutput(false);

        Integer code = new Http(context, Http.METHOD_GET, this.getMockUrl())
            .verbose()
            .target(output)
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(output.asString(), is("ok"));
    }

    @Test
    public void getFile() throws InterruptedException, IOException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201).setBody("ok"));

        try (TemporaryPath temporaryPath = TemporaryPath.tempFile()) {

            Integer code = new Http(context, Http.METHOD_GET, this.getMockUrl())
                .verbose()
                .target(temporaryPath.getPath())
                .run();

            assertThat(code, is(201));

            RecordedRequest rr = this.mockWebServer.takeRequest();

            assertThat(new String(Files.readAllBytes(temporaryPath.getPath())), is("ok"));
        }
    }

    @Test
    public void getFileWithProgress() throws InterruptedException, IOException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setBody("this is a test"));

        try (TemporaryPath temporaryPath = TemporaryPath.tempFile()) {

            Integer code = new Http(context, Http.METHOD_GET, this.getMockUrl())
                .verbose()
                .progress()
                .target(temporaryPath.getPath())
                .run();

            assertThat(code, is(201));

            RecordedRequest rr = this.mockWebServer.takeRequest();

            assertThat(new String(Files.readAllBytes(temporaryPath.getPath())), is("this is a test"));
        }
    }

    @Test
    public void requireBody() throws InterruptedException {
        try {
            new Http(context, Http.METHOD_POST, this.getMockUrl())
                .verbose()
                .run();
        } catch (BlazeException e) {
            assertThat(e.getMessage(), containsString("body"));
        }
    }

    @Test
    public void postBody() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setBody("ok"));

        int code = new Http(context, Http.METHOD_POST, this.getMockUrl())
            .verbose()
            .body("body", "text/plain")
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(rr.getMethod(), is("POST"));
        assertThat(rr.getHeader("Content-Type"), is("text/plain; charset=utf-8"));
        assertThat(rr.getBody().readUtf8(), is("body"));
    }

    @Test
    public void postFileWithProgress() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setBody("ok"));

        int code = new Http(context, Http.METHOD_POST, this.getMockUrl())
            .verbose()
            .progress()
            .body("body", "text/plain")
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(rr.getMethod(), is("POST"));
        assertThat(rr.getHeader("Content-Type"), is("text/plain; charset=utf-8"));
        assertThat(rr.getBody().readUtf8(), is("body"));
    }

    @Test
    public void postForm() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setBody("ok"));

        int code = new Http(context, Http.METHOD_POST, this.getMockUrl())
            .verbose()
            .form("a", "b")
            .run();

        assertThat(code, is(201));

        RecordedRequest rr = this.mockWebServer.takeRequest();

        assertThat(rr.getMethod(), is("POST"));
        assertThat(rr.getHeader("Content-Type"), is("application/x-www-form-urlencoded"));
        assertThat(rr.getBody().readUtf8(), is("a=b"));
    }

    @Test
    public void unexpectedStatusCode() throws InterruptedException {
        this.mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201).setBody("ok"));

        try {
            new Http(context, Http.METHOD_GET, this.getMockUrl())
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

        Integer code = new Http(context, Http.METHOD_DELETE, this.getMockUrl())
            .verbose()
            .statusCodesAny()
            .run();

        assertThat(code, is(499));
    }

}