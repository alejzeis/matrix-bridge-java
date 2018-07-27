/*
 * Copyright © 2018, jython234
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.jython234.matrix.bridge.network;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class JDKHttpClient implements HTTPClient {
    private HttpClient client;

    public JDKHttpClient() {
        this.client = HttpClient.newBuilder()
                .build();
    }

    @Override
    public CompletableFuture<HTTPResult> downloadFile(String url, File saveTo) {
        var future = new CompletableFuture<HTTPResult>();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(Duration.ofSeconds(20))
                    .GET()
                    .build();

            this.client.sendAsync(request, HttpResponse.BodyHandler.asFile(saveTo.toPath())).thenAccept(response -> future.complete(new HTTPResult(response.statusCode(), null)));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<HTTPResult> uploadFile(String url, File uploadFrom) {
        var future = new CompletableFuture<HTTPResult>();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .timeout(Duration.ofSeconds(20))
                    .PUT(HttpRequest.BodyPublisher.fromFile(uploadFrom.toPath()))
                    .build();

            this.client.sendAsync(request, HttpResponse.BodyHandler.asString()).thenAccept(response -> future.complete(new HTTPResult(response.statusCode(), response.body())));
        } catch (URISyntaxException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<HTTPResult> sendRequest(HttpMethod method, String url) {
        var future = new CompletableFuture<HTTPResult>();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .method(method.name(), HttpRequest.BodyPublisher.noBody())
                    .timeout(Duration.ofSeconds(20)).build();

            this.client.sendAsync(request, HttpResponse.BodyHandler.asString()).thenAccept(response -> future.complete(new HTTPResult(response.statusCode(), response.body())));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<HTTPResult> sendRequest(HttpMethod method, String url, String payload, String contentType) {
        var future = new CompletableFuture<HTTPResult>();
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", contentType)
                    .method(method.name(), HttpRequest.BodyPublisher.fromString(payload))
                    .timeout(Duration.ofSeconds(20)).build();

            this.client.sendAsync(request, HttpResponse.BodyHandler.asString()).thenAccept(response -> future.complete(new HTTPResult(response.statusCode(), response.body())));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return future;
    }
}
