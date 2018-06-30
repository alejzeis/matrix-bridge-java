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
import io.github.jython234.matrix.appservice.Util;
import io.github.jython234.matrix.appservice.event.room.message.MessageContent;
import io.github.jython234.matrix.appservice.event.room.message.MessageMatrixEvent;
import io.github.jython234.matrix.bridge.network.registration.UserRegisterRequest;
import jdk.incubator.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * A helper class that allows the bridge to control a "bot user"
 * on the matrix server. This is a user that is controlled by the appservice
 * to represent a user on the other application.
 *
 * @author jython234
 */
public class BridgeUserClient {
    private static Long nextTransactionId = 0L;

    private MatrixBridgeClient client;
    private String userId;

    protected BridgeUserClient(MatrixBridgeClient client, String userId) {
        this.client = client;
        this.userId = userId;
    }

    protected void register() throws IOException, InterruptedException {
        var json = MatrixBridgeClient.gson.toJson(new UserRegisterRequest(Util.getLocalpart(this.userId)));

        var response = this.client.sendRawPOSTRequest(this.client.getURI("register", true), json);
        switch (response.statusCode()) {
            case 200:
                break;
            case 400:
                JSONParser parser = new JSONParser();
                try {
                    var obj = (JSONObject) parser.parse(response.body());
                    if(obj.get("errcode").equals("M_USER_IN_USE")) {
                        return; // Silent ignore, as the user is already registered
                    } else if(obj.get("errcode").equals("M_EXCLUSIVE")) {
                        throw new RuntimeException("Attempting to register a user outside of this appservice's exclusive zone!");
                    } else {
                        throw new RuntimeException("Unknown error from server while registering BridgeUser: " + obj.get("errcode"));
                    }
                } catch (ParseException e) {
                    throw new RuntimeException("Bad JSON while registering BridgeUser");
                }
            default:
                throw new RuntimeException("Recieved unknown response code while registering BridgeUser: " + response.statusCode());
        }
    }

    /**
     * Sends a message to a Matrix room. The user must be joined to the room first!
     * @param roomId The matrix room ID of the room to send the message to.
     * @param content The Message content.
     * @return An {@link HttpResponse} object containing the result of the request to the homeserver.
     * @throws IOException If there was an error while performing IO on the network request.
     * @throws InterruptedException If the request was interrupted
     * @see #joinRoom(String)
     */
    public HttpResponse sendMessage(String roomId, MessageContent content) throws IOException, InterruptedException {
        long txnId;
        synchronized (nextTransactionId) {
            txnId = nextTransactionId++;
        }

        var uri = this.client.getURI("rooms/" + roomId + "/send/m.room.message" + "/" + txnId, this.userId);
        var json = MatrixBridgeClient.gson.toJson(content);
        return this.client.sendRawPUTRequest(uri, json);
    }

    /**
     * Joins this user to a Matrix room. The room must either be public, or someone
     * must have invited the user to the room.
     * @param roomIdOrAlias The matrix room ID of the room, OR a room alias of the room.
     * @return An {@link HttpResponse} object containing the result of the request to the homeserver.
     * @throws IOException If there was an error while performing IO on the network request.
     * @throws InterruptedException If the request was interrupted
     */
    public HttpResponse joinRoom(String roomIdOrAlias) throws IOException, InterruptedException {
        var uri = this.client.getURI("join/" + roomIdOrAlias, this.userId);
        return this.client.sendRawPOSTRequest(uri);
    }
}
