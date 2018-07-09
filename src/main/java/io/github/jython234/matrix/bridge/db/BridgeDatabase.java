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
package io.github.jython234.matrix.bridge.db;

import io.github.jython234.matrix.bridge.MatrixBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

/**
 * Interface which represents a supported database (Mongo or leveldb). This
 * is used to store User/Room data. It can also store additional extra data
 * that the appservice implementation (your code) might need.
 *
 * @author jython234
 * @see Room
 * @see User
 * @see #putExtraData(String, Serializable)
 */
public abstract class BridgeDatabase implements Closeable {
    protected final MatrixBridge bridge;
    protected final Logger logger;

    protected BridgeDatabase(MatrixBridge bridge, String providerName) {
        this.bridge = bridge;
        this.logger = LoggerFactory.getLogger("BridgeDatabase-" + providerName);
    }

    /**
     * Checks if a user exists based on their ID. This ID is the same ID used in a bot matrix
     * user's localpart, or a normal matrix user's localpart. When writing your bridge, the ID
     * should be from/or generated from the bridged application user somehow.
     *
     * @param id The user's ID.
     * @return If the user exists or not.
     */
    public abstract boolean userExists(String id);

    /**
     * Checks if a user exists based on their ID.
     * @param user The user.
     * @return If the user exists or not.
     * @see #userExists(String)
     */
    public boolean userExists(User user) {
        return userExists(user.id);
    }

    /**
     * Puts a user into the database, overwriting one if it already exists. The User's
     * {@link User#id} is used as the key in the database.
     * @param user The User to be inserted.
     * @throws IOException If there is an error while attempting to insert the user into the database.
     */
    public abstract void putUser(User user) throws IOException;

    /**
     * Get a user from the database, returning <code>null</code> if it doesn't exist.
     * @param id The ID of the user.
     * @return The User if found, null if not.
     * @throws IOException If there was an error while attempting to get the user from the database.
     */
    public abstract User getUser(String id) throws IOException;

    /**
     * Deletes a user from the database, based on their id.
     * @param id The ID of the user to be deleted.
     * @throws IOException If there is an error while deleting the user.
     */
    public abstract void deleteUser(String id) throws IOException;

    /**
     * Deletes a user from the database, based on their {@link User#id}.
     * @param user The {@link User} to be deleted.
     * @throws IOException If there is an error while deleting the user.
     */
    public void deleteUser(User user) throws IOException {
        deleteUser(user.id);
    }

    /**
     * Check if a room entry exists with the given ID.
     * @param id The ID of the room.
     * @return If the room exists or not.
     * @see Room#id
     */
    public abstract boolean roomExists(String id);

    /**
     * Check if the room entry exists in the database.
     * @param room The Room to check.
     * @return If it exists or not.
     */
    public boolean roomExists(Room room) {
        return this.roomExists(room.id);
    }

    /**
     * Check if a room entry exists with the given matrix ID.
     * @param matrixId The matrix ID of the room entry.
     * @return If the room exists or not.
     * @see Room#matrixId
     */
    public abstract boolean roomExistsByMatrixId(String matrixId);

    /**
     * Puts a Room entry into the database, overwriting one if it already exists.
     * @param room The {@link Room} to be inserted.
     * @throws IOException If there is an error while attempting to insert the room into the database.
     */
    public abstract void putRoom(Room room) throws IOException;

    /**
     * Get a {@link Room} from the database, returning <code>null</code> if it doesn't exist.
     * @param id The ID of the room (not matrix ID).
     * @return The Room entry if found, null if not.
     * @throws IOException If there was an error while attempting to get the room from the database.
     */
    public abstract Room getRoom(String id) throws IOException;

    /**
     * Get a {@link Room} from the database, by matrix ID, returning <code>null</code> if it doesn't exist.
     * @param matrixId The matrix ID of the room ({@link Room#matrixId}).
     * @return The Room entry if found, null if not.
     * @throws IOException If there was an error while attempting to get the room from the database.
     */
    public abstract Room getRoomByMatrixId(String matrixId) throws IOException;

    /**
     * Deletes a room from the database, based on the ID.
     * @param id The ID of the room to be deleted.
     * @throws IOException If there is an error while deleting the room.
     */
    public abstract void deleteRoom(String id) throws IOException;

    /**
     * Deletes a {@link Room} from the database, based on its {@link Room#id}.
     * @param room The {@link Room} to be deleted.
     * @throws IOException If there is an error while deleting the room.
     */
    public void deleteRoom(Room room) throws IOException {
        deleteRoom(room.id);
    }

    /**
     * Updates a {@link Room#matrixId}. Used by {@link Room#updateMatrixId(String)}
     * @param room The {@link Room} to perform the update on.
     * @param matrixId The new matrix ID.
     * @throws IOException If there is an error while updating the value.
     */
    protected abstract void updateRoomMatrixId(Room room, String matrixId) throws IOException;

    /**
     * Updates a key,value pair of the room's {@link Room#additionalData}. Used by
     * {@link Room#updateDataField(String, Serializable)}
     * @param room The room to perform the update on.
     * @param key The key of the data field.
     * @param value The value of the field.
     * @throws IOException If there is an error while updating the data.
     * @see Room#updateDataField(String, Serializable)
     */
    protected abstract void updateRoomDataField(Room room, String key, Serializable value) throws IOException;

    /**
     * Deletes a key,value pair of the user's additional data. Used by
     * {@link Room#deleteDataField(String)}
     * @param room The room to perform the deletion on.
     * @param key The key of the field to be deleted.
     * @throws IOException If there is an error while deleting the data.
     * @see Room#deleteDataField(String)
     */
    protected abstract void deleteRoomDataField(Room room, String key) throws IOException;

    /**
     * Updates a key,value pair of the user's additional data. Used by
     * {@link User#updateDataField(String, Serializable)}.
     * @param user The user to perform the operation on.
     * @param key The key of the data field.
     * @param value The value of the data field.
     * @throws IOException If there is an error while updating the data.
     * @see User#updateDataField(String, Serializable)
     */
    protected abstract void updateUsersDataField(User user, String key, Serializable value) throws IOException;

    /**
     * Deletes a key,value pair of the user's additional data. Used by
     * {@link User#deleteDataField(String)}.
     * @param user The user to perform the operation on.
     * @param key The key of the data field.
     * @throws IOException If there is an error while deleting the key.
     * @see User#deleteDataField(String)
     */
    protected abstract void deleteUsersDataField(User user, String key) throws IOException;

    /**
     * Puts/updates some extra data into the database, usually used by the implementing appservice to store
     * data that doesn't belong to a user or a room.
     *
     * The database implementation must somehow seperate this data
     * from the rooms/user data, as we don't want conflicts with the keys.
     *
     * @param key The key of the data.
     * @param value The value.
     * @throws IOException If there is an error while putting the extra data in.
     */
    public abstract void putExtraData(String key, Serializable value) throws IOException;

    /**
     * Gets some extra data from the database. Returns <code>null</code> if it doesn't exist.
     * @param key The key of the data.
     * @return The value pair of the data, or <code>null</code> if not found.
     * @throws IOException If there is an error while retrieving the data.
     * @see #putExtraData(String, Serializable)
     */
    public abstract Serializable getExtraData(String key) throws IOException;

    /**
     * Deletes some extra data from the database.
     * @param key The key of the data.
     * @throws IOException If there is an error while deleting the extra data.
     * @see #putExtraData(String, Serializable)
     */
    public abstract void deleteExtraData(String key) throws IOException;
}
