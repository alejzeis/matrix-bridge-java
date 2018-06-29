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
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides all the methods necessary to execute common operations
 * on the homeserver using the special bridge account, such as creating and controlling
 * bridge users, sending messages, etc.
 *
 * @author jython234
 * @see <a href="https://matrix.org/docs/spec/application_service/unstable.html#client-server-api-extensions">Matrix Client-Server API Extensions</a>
 */
public class MatrixBridgeClient {
    protected static final Gson gson = new GsonBuilder().create();

    private final Logger logger;
    private final MatrixBridge bridge;

    private HttpClient httpClient;

    private Map<String, BridgeUserClient> bridgeUsers = new ConcurrentHashMap<>(); // Map of 'bot created' users by the appservice

    public MatrixBridgeClient(MatrixBridge bridge) {
        this.logger = LoggerFactory.getLogger("MatrixBridge-Client");
        this.bridge = bridge;

        this.httpClient = HttpClient.newBuilder().executor(bridge.getAppservice().threadPoolTaskExecutor).build();
    }

    public BridgeUserClient getClientForUser(String localpart) {
        if(localpart.contains("@") || localpart.contains(":")) throw new IllegalArgumentException("Invalid localpart! Must not be the full address!");

        if(!bridgeUsers.containsKey(localpart)) {
            var client = new BridgeUserClient(this, localpart);
            try {
                client.register();
                this.bridgeUsers.put(localpart, client);
                return client;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else return bridgeUsers.get(localpart);
    }

    public URI getURI(String matrixPath, boolean appendAccessToken) {
        try {
            var sb = new StringBuilder();

            sb.append(this.bridge.getConfig().getServerURL());
            sb.append("/_matrix/client/r0/");
            sb.append(matrixPath);

            if(appendAccessToken) {
                sb.append("?access_token=");
                sb.append(this.bridge.getAppservice().getRegistration().getAsToken());
            }

            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getURI(String matrixPath, String userId) {
        try {
            var sb = new StringBuilder();

            sb.append(this.bridge.getConfig().getServerURL());
            sb.append("/_matrix/client/r0/");
            sb.append(matrixPath);

            sb.append("?access_token=");
            sb.append(this.bridge.getAppservice().getRegistration().getAsToken());

            sb.append("&user_id=");
            sb.append(userId);

            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> sendRawPOSTRequest(URI uri, String json) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublisher.fromString(json))
                .timeout(Duration.ofSeconds(20))
                .build();

        return this.httpClient.send(request, HttpResponse.BodyHandler.asString());
    }

    public HttpResponse<String> sendRawPOSTRequest(URI uri) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublisher.fromString("{}"))
                .timeout(Duration.ofSeconds(20))
                .build();

        return this.httpClient.send(request, HttpResponse.BodyHandler.asString());
    }

    public HttpResponse<String> sendRawPUTRequest(URI uri, String json) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublisher.fromString(json))
                .timeout(Duration.ofSeconds(20))
                .build();

        return this.httpClient.send(request, HttpResponse.BodyHandler.asString());
    }

    public HttpResponse<String> sendRawGETRequest(URI uri, String json) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .method("GET", HttpRequest.BodyPublisher.fromString(json))
                .timeout(Duration.ofSeconds(20))
                .build();

        return this.httpClient.send(request, HttpResponse.BodyHandler.asString());
    }

    public HttpResponse<String> sendRawGETRequest(URI uri) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();

        return this.httpClient.send(request, HttpResponse.BodyHandler.asString());
    }
}
