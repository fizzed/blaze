#!/usr/bin/env blaze

import org.slf4j.Logger
import com.fizzed.blaze.Contexts
import com.fizzed.blaze.util.MutableUri
import static com.fizzed.blaze.Https.*;

def main() {
    def log = Contexts.logger()

    def uri = MutableUri.of("http://jsonplaceholder.typicode.com/comments")
        .query("postId", 1)
        .toURI()

    def output = httpGet(uri.toString())
        .addHeader("Accept", "application/json")
        .runCaptureOutput()
        .toString();

    log.info("Quote of the day JSON is {}", output)
}