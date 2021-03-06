/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.slice;

import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.rs.RsWithStatus;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

/**
 * Test case for {@link HiddenRoutingSlice}.
 * @since 0.8
 */
class HiddenRoutingSliceTest {

    @Test
    void removesInitialPartOfUri() {
        new HiddenRoutingSlice(new LineSlice()).response(
            requestLine("http://www.w3.org/pub/WWW/TheProject.html"),
            Arrays.asList(),
            Flowable.empty()
        ).send(
            (status, headers, body) -> {
                final String line = headers.iterator().next().getValue();
                MatcherAssert.assertThat(
                    "url retrieved",
                    line,
                    IsEqual.equalTo(requestLine("http://www.w3.org/WWW/TheProject.html"))
                );
                return CompletableFuture.allOf();
            }
        );
    }

    @Test
    void shortRequestLine() {
        final String url = requestLine("http://www.w3.org");
        new HiddenRoutingSlice(new LineSlice()).response(
            url,
            Arrays.asList(),
            Flowable.empty()
        ).send(
            (status, headers, body) -> {
                final String line = headers.iterator().next().getValue();
                MatcherAssert.assertThat(
                    "short url retrieved",
                    line,
                    IsEqual.equalTo(url)
                );
                return CompletableFuture.allOf();
            }
        );
    }

    @Test
    void absoluteLine() {
        new HiddenRoutingSlice(new LineSlice()).response(
            requestLine("/one/two/three"),
            Arrays.asList(),
            Flowable.empty()
        ).send(
            (status, headers, body) -> {
                final String line = headers.iterator().next().getValue();
                MatcherAssert.assertThat(
                    "absolute url retrieved",
                    line,
                    IsEqual.equalTo(requestLine("/two/three"))
                );
                return CompletableFuture.allOf();
            }
        );
    }

    private static String requestLine(final String path) {
        return new RequestLine("GET", path, "HTTP/1.1").toString();
    }

    /**
     * Slice that returns the line with headers for testing.
     * @since 0.8
     */
    class LineSlice implements Slice {
        @Override
        public Response response(
            final String line,
            final Iterable<Map.Entry<String, String>> headers,
            final Publisher<ByteBuffer> body
        ) {
            return new RsWithHeaders(
                new RsWithStatus(RsStatus.OK),
                "line",
                line
            );
        }
    }
}
