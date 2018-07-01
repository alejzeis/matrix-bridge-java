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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a bridged room between Matrix and a remote application.
 *
 * @author jython234
 */
public class Room implements Serializable {
    /**
     * The ID of the room. This can't be changed and is always final.
     * This usually should be an ID from the remote application.
     */
    public final String id;

    /**
     * The Matrix room ID of this room.
     */
    private String matrixId;

    protected transient final BridgeDatabase database;

    private Map<String, Serializable> additionalData;

    public Room(BridgeDatabase db, String id) {
        this.database = db;
        this.id = id;
        this.additionalData = new ConcurrentHashMap<>();
    }

    public Room(BridgeDatabase db, String id, String matrixId) {
        this.database = db;
        this.id = id;
        this.matrixId = matrixId;
        this.additionalData = new ConcurrentHashMap<>();
    }

    public Room(BridgeDatabase db, String id, String matrixId, Map<String, Serializable> additionalData) {
        this.database = db;
        this.id = id;
        this.matrixId = matrixId;
        this.additionalData = additionalData;
    }

    public synchronized void updateMatrixId(String newId) {
        var old = this.matrixId;

        this.matrixId = newId;
        try {
            this.database.updateRoomMatrixId(this, newId);
        } catch (IOException e) {
            this.matrixId = old; // Revert as the database probably didn't save our new value
            throw new DatabaseException(e);
        }
    }

    /**
     * Update a data field for this room entry.
     * @param key The key of the field.
     * @param value The value.
     */
    public synchronized void updateDataField(String key, Serializable value) {
        Serializable oldValue = this.additionalData.get(key);

        this.additionalData.put(key, value);
        try {
            this.database.updateRoomDataField(this, key, value);
        } catch (IOException e) {
            this.additionalData.put(key, oldValue); // Revert to old value as the database probably didn't update or save correctly.
            throw new DatabaseException(e);
        }
    }

    /**
     * Delete a data field for this room entry.
     * @param key The key of the field.
     * @return The value of the field that was removed.
     */
    public synchronized Serializable deleteDataField(String key) {
        Serializable value = this.additionalData.remove(key);

        try {
            this.database.deleteRoomDataField(this, key);
        } catch (IOException e) {
            this.additionalData.put(key, value); // Re-insert the value as the database probably didn't successfully delete the pair.
            throw new DatabaseException(e);
        }

        return value;
    }

    public String getMatrixId() {
        return this.matrixId;
    }

    /**
     * <strong>NOTICE: Modifying this directly is not recommended!</strong> Please use the helper method
     * {@link #updateDataField(String, Serializable)} instead.
     *
     * If you do end up modifying this directly, you'll have to update the whole user in the database by
     * calling {@link BridgeDatabase#putUser(User)}, even if it already exists as it will overwrite it, inserting your new
     * changes. It's recommended to use the helper method instead.
     *
     *
     * Any additional data to be stored in the database. This is ususally used by the actual
     * bridge implementation for specific data referring to the application being bridged.
     *
     * For example, storing profile picture information.
     *
     * @see #updateDataField(String, Serializable)
     * @see BridgeDatabase#putUser(User)
     * @return The additional data map for this user.
     */
    public Map<String, Serializable> getAdditionalData() {
        return this.additionalData;
    }
}
