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
import io.github.jython234.matrix.bridge.network.response.VersionsResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Handles networking for sending requests to the Matrix homeserver.
 *
 * @author jython234
 */
public class MatrixClient {
    static final Gson GSON;

    static {
        GSON = new GsonBuilder().create();
    }

    private MatrixBridge bridge;
    private HTTPClient client;

    public MatrixClient(MatrixBridge bridge) {
        this.bridge = bridge;

        this.client = new JDKHttpClient(); // Replace if necessary TODO
    }

    public CompletableFuture<MatrixNetworkResult<VersionsResponse>> queryServerForSupportedVersions() {
        var future = new CompletableFuture<MatrixNetworkResult<VersionsResponse>>();
        var url = this.bridge.getConfig().getServerURL() + "/_matrix/client/versions";

        this.client.sendGET(url).thenAccept(result -> {
            if(result.statusCode != 200) {
                future.complete(new MatrixNetworkResult<>(false, result.statusCode, null));
            } else {
                var versions = GSON.fromJson(result.body, VersionsResponse.class);
                future.complete(new MatrixNetworkResult<>(true, result.statusCode, versions));
            }
        });

        return future;
    }
}
