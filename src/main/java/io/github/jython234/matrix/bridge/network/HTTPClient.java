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

import org.springframework.http.HttpMethod;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an HTTP client that can send different requests
 * to the Matrix homeserver.
 *
 * @author jython234
 */
interface HTTPClient {
    default CompletableFuture<HTTPResult> sendGET(String url) {
        return this.sendRequest(HttpMethod.GET, url);
    }

    default CompletableFuture<HTTPResult> sendGET(String url, String payload, String contentType) {
        return this.sendRequest(HttpMethod.GET, url, payload, contentType);
    }

    default CompletableFuture<HTTPResult> sendPOST(String url) {
        return this.sendRequest(HttpMethod.POST, url);
    }

    default CompletableFuture<HTTPResult> sendPOST(String url, String payload, String contentType) {
        return this.sendRequest(HttpMethod.POST, url, payload, contentType);
    }

    default CompletableFuture<HTTPResult> sendPUT(String url) {
        return this.sendRequest(HttpMethod.PUT, url);
    }

    default CompletableFuture<HTTPResult> sendPUT(String url, String payload, String contentType) {
        return this.sendRequest(HttpMethod.PUT, url, payload, contentType);
    }

    CompletableFuture<HTTPResult> downloadFile(String url, File saveTo);

    CompletableFuture<HTTPResult> uploadFile(String url, File uploadFrom);

    CompletableFuture<HTTPResult> sendRequest(HttpMethod method, String url);
    CompletableFuture<HTTPResult> sendRequest(HttpMethod method, String url, String payload, String contentType);
}
