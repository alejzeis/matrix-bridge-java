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

import io.github.jython234.matrix.appservice.Util;
import io.github.jython234.matrix.appservice.event.room.message.MessageContent;
import io.github.jython234.matrix.bridge.db.User;
import io.github.jython234.matrix.bridge.network.directory.PutRoomAlias;
import io.github.jython234.matrix.bridge.network.directory.RoomAliasInfo;
import io.github.jython234.matrix.bridge.network.profile.SetAvatarURLRequest;
import io.github.jython234.matrix.bridge.network.profile.SetDisplayNameRequest;
import io.github.jython234.matrix.bridge.network.registration.UserExclusiveException;
import io.github.jython234.matrix.bridge.network.registration.UserRegisterRequest;
import io.github.jython234.matrix.bridge.network.room.InviteRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;

/**
 * A helper class that allows the bridge to control a "bot user"
 * on the matrix server. This is a user that is controlled by the appservice
 * to represent a user on the other application.
 *
 * @author jython234
 */
public class MatrixUserClient {
    private MatrixClientManager client;
    private String userId;

    private User user;

    protected MatrixUserClient(MatrixClientManager client, String userId) throws IOException {
        this.client = client;
        this.userId = userId;

        if(!this.client.bridge.getDatabase().userExists(userId)) {
            this.user = new User(this.client.bridge.getDatabase(), User.Type.REMOTE_USER, userId);
            this.client.bridge.getDatabase().putUser(this.user);
        } else this.user = this.client.bridge.getDatabase().getUser(userId);
    }

    protected void register() throws MatrixNetworkException {
        var json = MatrixClientManager.gson.toJson(new UserRegisterRequest(Util.getLocalpart(this.userId)));

        try {
            var response = this.client.sendRawPOSTRequest(this.client.getURI("register", true), json);
            switch (response.statusCode()) {
                case 200:
                    break;
                case 400:
                    var error = MatrixClientManager.gson.fromJson(response.body(), MatrixErrorResponse.class);
                    switch (error.errorCode) {
                        case "M_USER_IN_USE":
                            return; // Silent ignore, as the user is already registered
                        case "M_EXCLUSIVE":
                            throw new UserExclusiveException("Attempting to register a user outside of this appservice's exclusive zone!");
                        default:
                            throw new MatrixNetworkException("Unknown error from server while registering BridgeUser: " + error);
                    }
                default:
                    this.client.bridge.getBridgeLogger().warn("Unknown response code while registering, " + response.statusCode() + ", " + response.body());
                    throw new MatrixNetworkException("Recieved unknown response code while registering BridgeUser: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    // PROFILE ------------------------------------------

    /**
     * Set this user's Matrix display name.
     * @param displayName The new displayname.
     * @return An {@link HttpResponse} object containing the result of the request to the homeserver.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult setDisplayName(String displayName) throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/displayname", this.userId);
        var json = MatrixClientManager.gson.toJson(new SetDisplayNameRequest(displayName));

        try {
            var response = this.client.sendRawPUTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Set this user's avatar URL. To set the user's avatar to a file, you must upload the file first
     * using the resource API to get an MXC URL to it. Then use that URL with this function to set the avatar.
     *
     *
     * @param url An MXC URL to the user's avatar.
     * @return An {@link HttpResponse} object containing the result of the request to the homeserver.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     * @see MatrixClientManager#uploadMatrixFromFile(String)
     */
    public MatrixNetworkResult setAvatarURL(String url) throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/avatar_url", this.userId);
        var json = MatrixClientManager.gson.toJson(new SetAvatarURLRequest(url));

        try {
            var response = this.client.sendRawPUTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Get this user's display name from the matrix user.
     * @return The user's displayname.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult<String> getDisplayName() throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/displayname", this.userId);
        try {
            var response = this.client.sendRawGETRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    // SetDisplayNameRequest is the same format as getting  the displayname
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), SetDisplayNameRequest.class).displayName);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Get this user's avatar URL, which is an MXC URL.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     *          To get the Avatar URL, just use {@link MatrixNetworkResult#result}, the URL will be stored in that variable.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult<String> getAvatarURL() throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/avatar_url", this.userId);
        try {
            var response = this.client.sendRawGETRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    // SetAvatarURLRequest is the same format as getting the avatar URL
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), SetAvatarURLRequest.class).avatarURL);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    // ROOMS --------------------------------------------------------------

    /**
     * Sends a message to a Matrix room. The user must be joined to the room first!
     * @param roomId The matrix room ID of the room to send the message to.
     * @param content The Message content.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request.
     * @see #joinRoom(String)
     */
    public MatrixNetworkResult sendMessage(String roomId, MessageContent content) throws MatrixNetworkException {
        var txnId = this.client.random.nextLong(); // Generate random Transaction ID

        var uri = this.client.getURI("rooms/" + roomId + "/send/m.room.message" + "/" + txnId, this.userId);
        var json = MatrixClientManager.gson.toJson(content);
        try {
            var response = this.client.sendRawPUTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Invites another user to a room. You must be in the room to invite someone to it.
     * @param roomId The full room ID (no aliases) of the room that the user will be invited to.
     * @param userId The full UserID of the user to be invited.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request.
     */
    public MatrixNetworkResult invite(String roomId, String userId) throws MatrixNetworkException {
        var uri = this.client.getURI("rooms/" + roomId + "/invite", this.userId);
        var json = MatrixClientManager.gson.toJson(new InviteRequest(userId));

        try {
            var response = this.client.sendRawPOSTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 403: // No permissions
                case 429:
                    // TODO: rate limiting
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Joins this user to a Matrix room. The room must either be public, or someone
     * must have invited the user to the room.
     * @param roomIdOrAlias The matrix room ID of the room, OR a room alias of the room.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request.
     */
    public MatrixNetworkResult joinRoom(String roomIdOrAlias) throws MatrixNetworkException {
        var uri = this.client.getURI("join/" + roomIdOrAlias, this.userId);
        try {
            var response = this.client.sendRawPOSTRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 403: // No permissions, maybe not invited?
                case 429:
                    // TODO: rate limiting
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Leaves this user from a room.
     * @param roomId The full room ID (no aliases) of the room to be left.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request
     */
    public MatrixNetworkResult leaveRoom(String roomId) throws MatrixNetworkException {
        var uri = this.client.getURI("rooms/" + roomId + "/leave", this.userId);
        try {
            var response = this.client.sendRawPOSTRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 429:
                    // TODO: rate limiting
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    // ROOM Aliases ----------------------------------------------------------------

    /**
     * Creates a new alias for a Matrix room.
     * @param alias The alias to be set.
     * @param roomId The room ID
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request
     */
    public MatrixNetworkResult createRoomAlias(String alias, String roomId) throws MatrixNetworkException {
        var uri = this.client.getURI("directory/room/" + alias, this.userId);
        try {
            var response = this.client.sendRawPUTRequest(uri, MatrixClientManager.gson.toJson(new PutRoomAlias(roomId)));
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 409:
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch(IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Resolves a Matrix Room alias to a Matrix Room ID.
     * @param alias The room alias.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     *          To get the actual room ID, use the {@link MatrixNetworkResult#result} value, which will contain the {@link RoomAliasInfo} object.
     * @throws MatrixNetworkException If there was an error while performing the network request
     */
    public MatrixNetworkResult<RoomAliasInfo> getRoomIdFromAlias(String alias) throws MatrixNetworkException {
        var uri = this.client.getURI("directory/room/" + alias, this.userId);
        try {
            var response = this.client.sendRawGETRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), RoomAliasInfo.class));
                case 404: // Not found
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch(IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Deletes a Matrix Room alias.
     * @param alias The room alias to be deleted.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request
     */
    public MatrixNetworkResult deleteRoomAlias(String alias) throws MatrixNetworkException {
        var uri = this.client.getURI("directory/room/" + alias, this.userId);
        try {
            var response = this.client.sendRawDELETERequest(uri);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch(IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }
}
