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
