package com.fizzed.blaze;

import com.fizzed.blaze.http.Http;

public class Https {

    static public Http httpGet(String url) {
        return (new Http(Contexts.currentContext())).get(url);
    }

}