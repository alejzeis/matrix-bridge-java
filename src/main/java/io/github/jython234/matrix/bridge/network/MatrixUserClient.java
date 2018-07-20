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
import io.github.jython234.matrix.appservice.event.presence.Presence;
import io.github.jython234.matrix.appservice.event.room.message.MessageContent;
import io.github.jython234.matrix.bridge.db.User;
import io.github.jython234.matrix.bridge.network.directory.RoomAliasData;
import io.github.jython234.matrix.bridge.network.directory.RoomAliasInfo;
import io.github.jython234.matrix.bridge.network.error.MatrixErrorData;
import io.github.jython234.matrix.bridge.network.presence.RetrievedPresenceData;
import io.github.jython234.matrix.bridge.network.presence.SetPresenceData;
import io.github.jython234.matrix.bridge.network.profile.AvatarURLData;
import io.github.jython234.matrix.bridge.network.profile.DisplaynameData;
import io.github.jython234.matrix.bridge.network.registration.UserExclusiveException;
import io.github.jython234.matrix.bridge.network.registration.UserRegisterData;
import io.github.jython234.matrix.bridge.network.room.InviteData;
import io.github.jython234.matrix.bridge.network.room.JoinedMembersData;
import io.github.jython234.matrix.bridge.network.room.KickBanData;
import io.github.jython234.matrix.bridge.network.typing.TypingData;

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
        var json = MatrixClientManager.gson.toJson(new UserRegisterData(Util.getLocalpart(this.userId)));

        try {
            var response = this.client.sendRawPOSTRequest(this.client.getURI("register", true), json);
            switch (response.statusCode()) {
                case 200:
                    break;
                case 400:
                    var error = MatrixClientManager.gson.fromJson(response.body(), MatrixErrorData.class);
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

    // TYPING --------------------------------------------------------------

    /**
     * Set's this user's typing status for a specific room on Matrix.
     *
     * This sets a default of 30 seconds, so the user will show up as typing for 30 seconds.
     * If you want a different amount, use {@link #setTyping(String, boolean, int)}
     *
     * @param roomId The Matrix room ID that this user is either typing/not typing in.
     * @param typing If the user is typing or not.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult setTyping(String roomId, boolean typing) throws MatrixNetworkException {
        return setTyping(roomId, typing, 30000);
    }

    /**
     * Set's this user's typing status for a specific room on Matrix.
     * @param roomId The Matrix room ID that this user is either typing/not typing in.
     * @param typing If the user is typing or not.
     * @param duration If the user is typing, then this is how long the user should show as typing. If <code>typing</code> is false
     *                 then this doesn't matter.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult setTyping(String roomId, boolean typing, int duration) throws MatrixNetworkException {
        if(duration < 0) throw new IllegalArgumentException("Duration must be positive!");

        var uri = this.client.getURI("rooms/" + roomId + "/typing/" + this.userId, this.userId);
        var json = MatrixClientManager.gson.toJson(new TypingData(typing, duration));

        try {
            var response = this.client.sendRawPUTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 429: // TODO: ratelimiting
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    // PRESENCE ---------------------------------------------------------

    /**
     * Set's this user's presence on Matrix.
     *
     * Due to how bugged presence is on Matrix, you may need to call this
     * repeatedly and it may not behave as you expect it to.
     * @param presence The user's presence state.
     * @param statusMessage A status message accompanying the presence state, for example: "Idle" or "Working Remotely"
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult setPresence(Presence presence, String statusMessage) throws MatrixNetworkException {
        var uri = this.client.getURI("presence/" + this.userId + "/status", this.userId);
        var json = MatrixClientManager.gson.toJson(new SetPresenceData(presence, statusMessage));

        try {
            var response = this.client.sendRawPUTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 429: // TODO: ratelimiting
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Get's this user's presence data on Matrix.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     *         The presence data is stored in {@link MatrixNetworkResult#result}
     * @throws MatrixNetworkException If there was any network exception while processing the request
     * @see RetrievedPresenceData
     */
    public MatrixNetworkResult<RetrievedPresenceData> getPresence() throws MatrixNetworkException {
        var uri = this.client.getURI("presence/" + this.userId + "/status", this.userId);

        try {
            var response = this.client.sendRawGETRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), RetrievedPresenceData.class));
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    // PROFILE ----------------------------------------------------------

    /**
     * Set this user's Matrix display name.
     * @param displayName The new displayname.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult setDisplayName(String displayName) throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/displayname", this.userId);
        var json = MatrixClientManager.gson.toJson(new DisplaynameData(displayName));

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
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     * @see MatrixClientManager#uploadMatrixFromFile(String)
     */
    public MatrixNetworkResult setAvatarURL(String url) throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/avatar_url", this.userId);
        var json = MatrixClientManager.gson.toJson(new AvatarURLData(url));

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
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     *          To get the Display Name, just use {@link MatrixNetworkResult#result}, the display name will be stored in that variable.
     * @throws MatrixNetworkException If there was any network exception while processing the request
     */
    public MatrixNetworkResult<String> getDisplayName() throws MatrixNetworkException {
        var uri = this.client.getURI("profile/" + this.userId + "/displayname", this.userId);
        try {
            var response = this.client.sendRawGETRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    // DisplaynameData is the same format as getting  the displayname
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), DisplaynameData.class).displayName);
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
                    // AvatarURLData is the same format as getting the avatar URL
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), AvatarURLData.class).avatarURL);
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    // ROOMS --------------------------------------------------------------

    /**
     * Sends any type of message to a Matrix room. The user must be joined to the room first!
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
     * Sends a simple text-only message to the Matrix room. The user must be joined to the room first!
     *
     * This method is simply a wrapper around {@link #sendMessage(String, MessageContent)}.
     * @param roomId The matrix room ID of the room to send the message to.
     * @param content The text content of the message.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request.
     * @see #sendMessage(String, MessageContent)
     */
    public MatrixNetworkResult sendSimpleMessage(String roomId, String content) throws MatrixNetworkException {
        var msg = new MessageContent.TextMessageContent();
        msg.body = content;

        return sendMessage(roomId, msg);
    }

    /**
     * Sends a simple text-only "m.notice" message to the Matrix room. The user must be joined to the room first!
     *
     * This method is simply a wrapper around {@link #sendMessage(String, MessageContent)}.
     * @param roomId The matrix room ID of the room to send the message to.
     * @param content The text content of the message.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request.
     * @see #sendMessage(String, MessageContent)
     */
    public MatrixNetworkResult sendSimpleNoticeMessage(String roomId, String content) throws MatrixNetworkException {
        var msg = new MessageContent.NoticeMessageContent();
        msg.body = content;

        return sendMessage(roomId, msg);
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
        var json = MatrixClientManager.gson.toJson(new InviteData(userId));

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
        var escapedRoomIdOrAlias = roomIdOrAlias.replace("#", "%23"); // Need to escape the "#" or else the request will fail
        var uri = this.client.getURI("join/" + escapedRoomIdOrAlias, this.userId);
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

    // Internal method used to kick or ban a person as the code is quite similar
    private MatrixNetworkResult kickOrBan(String roomId, String userId, String reason, boolean isKick) throws MatrixNetworkException {
        var uri = this.client.getURI("rooms/" + roomId + "/" + (isKick ? "kick" : "ban"), this.userId);
        var json = MatrixClientManager.gson.toJson(new KickBanData(reason, userId));
        try {
            var response = this.client.sendRawPOSTRequest(uri, json);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, null);
                case 403: // No permission
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch (IOException | InterruptedException e) {
            throw new MatrixNetworkException(e);
        }
    }

    /**
     * Kicks a user from a room. The user must have sufficient permissions in the room or else the
     * request will fail.
     * @param roomId The full room ID of the room.
     * @param userId The full user ID of the user to be kicked.
     * @param reason A reason for the kick.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request
     */
    public MatrixNetworkResult kick(String roomId, String userId, String reason) throws MatrixNetworkException {
        return this.kickOrBan(roomId, userId, reason, true);
    }

    /**
     * Bans a user from a room. The user must have sufficient permissions in the room or else the
     * request will fail.
     * @param roomId The full room ID of the room.
     * @param userId The full user ID of the user to be banned.
     * @param reason A reason for the ban.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     * @throws MatrixNetworkException If there was an error while performing the network request
     */
    public MatrixNetworkResult ban(String roomId, String userId, String reason) throws MatrixNetworkException {
        return this.kickOrBan(roomId, userId, reason, false);
    }

    /**
     * Gets a map of members of a specific room.
     * @param roomId The full room ID of the room.
     * @return A {@link MatrixNetworkResult} object containing information about the results of the request, such as failure or success.
     *         The data will be stored in {@link MatrixNetworkResult#result}.
     * @throws MatrixNetworkException If there was an error while performing the network request
     * @see JoinedMembersData
     */
    public MatrixNetworkResult<JoinedMembersData> getRoomMembers(String roomId) throws MatrixNetworkException {
        var uri = this.client.getURI("rooms/" + roomId + "/joined_members", this.userId);
        try {
            var response = this.client.sendRawGETRequest(uri);
            switch (response.statusCode()) {
                case 200:
                    return new MatrixNetworkResult<>(true, response, MatrixClientManager.gson.fromJson(response.body(), JoinedMembersData.class));
                case 403: // Not a member of the room
                default:
                    return new MatrixNetworkResult<>(false, response, null);
            }
        } catch(IOException | InterruptedException e) {
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
            var response = this.client.sendRawPUTRequest(uri, MatrixClientManager.gson.toJson(new RoomAliasData(roomId)));
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
     * @see RoomAliasInfo
     */
    public MatrixNetworkResult<RoomAliasInfo> getRoomIdFromAlias(String alias) throws MatrixNetworkException {
        var escapedAlias = alias.replace("#", "%23"); // Need to escape the "#" or else the request will fail
        var uri = this.client.getURI("directory/room/" + escapedAlias, this.userId);
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
