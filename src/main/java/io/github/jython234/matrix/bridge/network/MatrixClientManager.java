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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Paths;
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
public class MatrixClientManager {
    protected static final Gson gson = new GsonBuilder().create();

    protected final Logger logger;
    protected final MatrixBridge bridge;

    private HttpClient httpClient;

    private Map<String, MatrixUserClient> bridgeUsers = new ConcurrentHashMap<>(); // Map of 'bot created' users by the appservice
    private MatrixUserClient bridgeClient;

    public MatrixClientManager(MatrixBridge bridge) {
        this.logger = LoggerFactory.getLogger("MatrixBridge-Client");
        this.bridge = bridge;

        this.httpClient = HttpClient.newBuilder().executor(bridge.getAppservice().threadPoolTaskExecutor).build();

        try {
            bridgeClient = new MatrixUserClient(this, "@" + this.bridge.getAppservice().getRegistration().getSenderLocalpart() + ":" + this.bridge.getConfig().getMatrixDomain());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@link MatrixUserClient} instance for the appservice user. You can use
     * it to control the appservice's user.
     *
     * @return A {@link MatrixUserClient} instance for the appservice user.
     */
    public MatrixUserClient getBridgeClient() {
        return this.bridgeClient;
    }

    /**
     * Returns a {@link MatrixUserClient} instance for a specific user within the appservice's domain.
     * If it doesn't exist it will be automatically registered onto the server. It must be within the appservice's domain,
     * specified in <code>registration.yml</code>
     *
     * @param userId The full User ID for the specific user. It must be within the domain of the appservice.
     * @throws io.github.jython234.matrix.bridge.network.registration.UserExclusiveException If the user in question is exclusive and can't be controlled by this appservice.
     * @return A {@link MatrixUserClient} instance for the specific user.
     * @see io.github.jython234.matrix.bridge.network.registration.UserExclusiveException
     */
    public MatrixUserClient getClientForUser(String userId) {
        if(!(userId.contains("@") && userId.contains(":"))) throw new IllegalArgumentException("Invalid userID! Correct format: \"@user:domain\"");

        if(!bridgeUsers.containsKey(userId)) {
            try {
                var client = new MatrixUserClient(this, userId);
                client.register();
                this.bridgeUsers.put(userId, client);
                return client;
            } catch (MatrixNetworkException | IOException e ) {
                throw new RuntimeException(e);
            }
        } else return bridgeUsers.get(userId);
    }

    /**
     * Returns a full URI for a matrix API call.
     * @param matrixPath The path past "/_matrix/client/r0"
     * @param appendAccessToken If the appservice's access token should be appended to the request.
     * @return The full URI.
     */
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

    /**
     * Returns a full URI for a matrix API call to be performed as a specific user.
     * @param matrixPath The matrix path past "/_matrix/client/r0/"
     * @param userId The full UserID of the user that this action will be performed by.
     * @return The full URI
     */
    public URI getURI(String matrixPath, String userId) {
        try {
            var sb = new StringBuilder();

            sb.append(this.bridge.getConfig().getServerURL());
            sb.append("/_matrix/client/r0/");
            sb.append(matrixPath);

            sb.append("?access_token=");
            sb.append(this.bridge.getAppservice().getRegistration().getAsToken());

            if(!userId.equals("@" + this.bridge.getAppservice().getRegistration().getSenderLocalpart() + ":" + this.bridge.getConfig().getMatrixDomain())) {
                sb.append("&user_id=");
                sb.append(userId);
            } else {
                sb.append("&ts=");
                sb.append(System.currentTimeMillis());
            }

            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Uploads a file to the Matrix Content Repository. The "Content-Type" will be inferred using the method
     * {@link URLConnection#guessContentTypeFromName(String)}.
     * @param path The path to the file to be uploaded.
     * @return The MXC URL of the uploaded file.
     * @throws MatrixNetworkException If there was an error while processing the upload.
     */
    public String uploadMatrixFromFile(String path) throws MatrixNetworkException {
        try {
            var uri = new URI(this.bridge.getConfig().getServerURL() + "/_matrix/media/r0/upload?access_token=" + this.bridge.getAppservice().getRegistration().getAsToken());
            var request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", URLConnection.guessContentTypeFromName(path))
                    .header("Content-Length", String.valueOf(new File(path).length()))
                    .POST(HttpRequest.BodyPublisher.fromFile(Paths.get(path)))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            var response = this.httpClient.send(request, HttpResponse.BodyHandler.asString());
            switch (response.statusCode()) {
                case 200:
                    return MatrixClientManager.gson.fromJson(response.body(), UploadResponse.class).contentUri;
                case 429:
                    // TODO Rate limiting support
                default:
                    throw new MatrixNetworkException("Non-200 status code while uploading file: " + response.statusCode());

            }
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new MatrixNetworkException(e);
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
