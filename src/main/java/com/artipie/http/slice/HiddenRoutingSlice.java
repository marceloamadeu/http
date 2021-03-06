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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.client.utils.URIBuilder;
import org.reactivestreams.Publisher;

/**
 * Slice that removes the first part from the request URI.
 * for example `GET http://www.w3.org/pub/WWW/TheProject.html HTTP/1.1`
 * would be `GET http://www.w3.org/WWW/TheProject.html HTTP/1.1`
 *
 * @since 0.8
 */
public final class HiddenRoutingSlice implements Slice {

    /**
     * Delegate slice.
     */
    private final Slice slice;

    /**
     * Ctor.
     *
     * @param slice Slice.
     */
    public HiddenRoutingSlice(final Slice slice) {
        this.slice = slice;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body
    ) {
        String newline = "";
        try {
            final String[] parts = line.split(" ");
            final URI uri = new URI(parts[1]);
            final String path = String.join(
                "/",
                ArrayUtils.remove(uri.getPath().split("/"), 1)
            );
            final URIBuilder builder = new URIBuilder(uri).setPath(path);
            parts[1] = builder.toString();
            newline = String.join(" ", parts);
        } catch (final URISyntaxException | IndexOutOfBoundsException exception) {
            newline = line;
        }
        return this.slice.response(newline, headers, body);
    }
}
