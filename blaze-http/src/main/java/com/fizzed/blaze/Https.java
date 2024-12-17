package com.fizzed.blaze;

import com.fizzed.blaze.http.Http;

public class Https {

    static public Http httpHead(String url) {
        return new Http(Contexts.currentContext(), Http.METHOD_HEAD, url);
    }

    static public Http httpGet(String url) {
        return new Http(Contexts.currentContext(), Http.METHOD_GET, url);
    }

    static public Http httpPost(String url) {
        return new Http(Contexts.currentContext(), Http.METHOD_POST, url);
    }

    static public Http httpPut(String url) {
        return new Http(Contexts.currentContext(), Http.METHOD_PUT, url);
    }

    static public Http httpPatch(String url) {
        return new Http(Contexts.currentContext(), Http.METHOD_PATCH, url);
    }

    static public Http httpDelete(String url) {
        return new Http(Contexts.currentContext(), Http.METHOD_DELETE, url);
    }

    static public Http httpMethod(String method, String url) {
        return new Http(Contexts.currentContext(), method, url);
    }

}