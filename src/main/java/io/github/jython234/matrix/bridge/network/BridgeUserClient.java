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
import io.github.jython234.matrix.appservice.event.room.message.MessageMatrixEvent;
import io.github.jython234.matrix.bridge.network.registration.UserRegisterRequest;
import jdk.incubator.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;

/**
 * A helper class that allows the bridge to control a "bot user"
 * on the matrix server. This is a user that is controlled by the appservice
 * to represent a user on the other application.
 *
 * @author jython234
 */
public class BridgeUserClient {
    private static Integer nextTransactionId = 0;

    private MatrixBridgeClient client;
    private String localpart;

    public BridgeUserClient(MatrixBridgeClient client, String localpart) {
        this.client = client;
        this.localpart = localpart;
    }

    protected void register() throws IOException, InterruptedException {
        var json = MatrixBridgeClient.gson.toJson(new UserRegisterRequest(this.localpart));

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

    public HttpResponse sendMessage(String roomId, MessageMatrixEvent message) throws IOException, InterruptedException {
        int txnId;
        synchronized (nextTransactionId) {
            txnId = nextTransactionId++;
        }

        var uri = this.client.getURI("rooms/" + roomId + "/send/" + message.content.msgtype + "/" + txnId, true);
        var json = MatrixBridgeClient.gson.toJson(message.content);
        return this.client.sendRawPUTRequest(uri, json);
    }

    public HttpResponse joinRoom(String roomIdOrAlias) throws IOException, InterruptedException {
        var uri = this.client.getURI("join/" + roomIdOrAlias, this.localpart);
        return this.client.sendRawPOSTRequest(uri);
    }
}
