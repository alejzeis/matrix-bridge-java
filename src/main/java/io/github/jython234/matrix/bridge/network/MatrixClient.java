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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jython234.matrix.bridge.MatrixBridge;
import io.github.jython234.matrix.bridge.network.error.MatrixErrorResponse;
import io.github.jython234.matrix.bridge.network.error.MatrixRatelimitedErrorResponse;
import io.github.jython234.matrix.bridge.network.response.VersionsResponse;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Handles networking for sending requests to the Matrix homeserver.
 *
 * @author jython234
 */
public class MatrixClient {
    public static final Gson GSON;

    static {
        GSON = new GsonBuilder().create();
    }

    private MatrixBridge bridge;
    private HTTPClient client;

    public MatrixClient(MatrixBridge bridge) {
        this.bridge = bridge;

        this.client = new JDKHttpClient(); // Replace if necessary TODO
    }

    /**
     * Get a list of supported Matrix versions the server supports.
     * @return A Future which will resolve to a {@link MatrixNetworkResult} that's {@link MatrixNetworkResult#result} will contain
     *          a {@link VersionsResponse} which contains a list of Matrix protocol versions the server supports.
     */
    public CompletableFuture<MatrixNetworkResult<VersionsResponse>> queryServerForSupportedVersions() {
        var future = new CompletableFuture<MatrixNetworkResult<VersionsResponse>>();
        var url = this.bridge.getConfig().getServerURL() + "/_matrix/client/versions";

        this.client.sendGET(url).thenAccept(result -> {
            if(result.statusCode != 200) {
                future.complete(new MatrixNetworkResult<>(result.statusCode, GSON.fromJson(result.body, MatrixErrorResponse.class)));
            } else {
                var versions = GSON.fromJson(result.body, VersionsResponse.class);
                future.complete(new MatrixNetworkResult<>(result.statusCode, versions));
            }
        });

        return future;
    }

    /**
     * Sends an HTTP request using the "Server admin-style permissions". This is only internally used for
     * registering bot users on the server.
     * @param payload Payload of the request.
     * @param handler A handler to handle the response of the request once it is completed.
     * @see MatrixBridge#registerMatrixUser(String)
     */
    public void _sendMatrixRequestAdmin(HTTPPayload payload, HTTPResultHandler handler) {
        this.sendMatrixRequestMasqueraded(HttpMethod.POST, "_matrix/client/r0/register", null, payload, handler);
    }

    /**
     * Sends an HTTP request to the Matrix homeserver. The request will be executed
     * as the appservice Bot on the homeserver.
     *
     * @param method The HTTP method to use.
     * @param matrixURL The Matrix URL of the request (/_matrix/client/r0/...) etc.
     * @param payload The payload of the request, if any. You may specify <code>null</code> if there is
     *                no payload.
     * @param handler A handler to handle the response of the request once it is completed.
     */
    public void sendMatrixRequestAsAppservice(HttpMethod method, String matrixURL, HTTPPayload payload, HTTPResultHandler handler) {
        this.sendMatrixRequestMasqueraded(method, matrixURL, this.bridge.appserviceUserID, payload, handler);
    }

    /**
     * Sends an HTTP request to the Matrix homeserver, masquerading as an appservice bot user. This allows you
     * to send requests as one of the registered bot users in the Appservice namespace.
     *
     * @param method The HTTP method to use.
     * @param matrixURL The Matrix URL of the request (/_matrix/client/r0/...) etc.
     * @param userId The Matrix User ID of the user to masquerade or act as.
     * @param payload The payload of the request, if any. You may specify <code>null</code> if there is
     *                no payload.
     * @param handler A handler to handle the response of the request once it is completed.
     */
    public void sendMatrixRequestMasqueraded(HttpMethod method, String matrixURL, String userId, HTTPPayload payload, HTTPResultHandler handler) {
        var url = this.bridge.getConfig().getServerURL() + "/" + matrixURL
                + "?access_token=" + this.bridge.getAppservice().getRegistration().getAsToken();

        if(userId != null) {
            url = url + "&user_id=" + userId;
        }

        CompletableFuture<HTTPResult> future;

        if(payload != null) {
            future = this.client.sendRequest(method, url, payload);
        } else {
            future = this.client.sendRequest(method, url);
        }

        future.thenAccept(httpResult -> {
            if(httpResult.statusCode == 429) { // Check if rate-limited
                var error = GSON.fromJson(httpResult.body, MatrixRatelimitedErrorResponse.class);
                if(error.retryAfterMs > 0) {
                    // Schedule tast to retry sending after specified milliseconds
                    this.bridge.getScheduler().schedule(() -> this.sendMatrixRequestMasqueraded(method, matrixURL, userId, payload, handler), Instant.ofEpochMilli(System.currentTimeMillis() + error.retryAfterMs));
                } else {
                    // Server didn't give us a time, retry in 5 seconds
                    this.bridge.getScheduler().schedule(() -> this.sendMatrixRequestMasqueraded(method, matrixURL, userId, payload, handler), Instant.ofEpochMilli(System.currentTimeMillis() + 5000));
                }
            } else {
                // Not ratelimited, pass to handler
                handler.handle(httpResult);
            }
        });
    }
}
