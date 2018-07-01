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
package io.github.jython234.matrix.bridge.db.mongo;

import io.github.jython234.matrix.bridge.MatrixBridge;
import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.db.BridgeDatabase;
import io.github.jython234.matrix.bridge.db.Room;
import io.github.jython234.matrix.bridge.db.User;

import java.io.IOException;
import java.io.Serializable;

/**
 * MongoDB Database wrapper implementation.
 *
 * @author jython234
 * @see BridgeDatabase
 */
public class MongoDatabaseImpl extends BridgeDatabase {

    public MongoDatabaseImpl(MatrixBridge bridge, BridgeConfig.MongoDBInfo dbInfo) {
        super(bridge, "MongoDBImpl");
    }
    // TODO: Implementation

    @Override
    public boolean userExists(String id) {
        return false;
    }

    @Override
    public void putUser(User user) throws IOException {

    }

    @Override
    public User getUser(String id) throws IOException {
        return null;
    }

    @Override
    public void deleteUser(String id) throws IOException {

    }

    @Override
    public boolean roomExists(String id) {
        return false;
    }

    @Override
    public boolean roomExistsByMatrixId(String matrixId) {
        return false;
    }

    @Override
    public void putRoom(Room room) throws IOException {

    }

    @Override
    public Room getRoom(String id) throws IOException {
        return null;
    }

    @Override
    public Room getRoomByMatrixId(String matrixId) throws IOException {
        return null;
    }

    @Override
    public void deleteRoom(String id) throws IOException {

    }

    @Override
    protected void updateRoomMatrixId(Room room, String matrixId) throws IOException {

    }

    @Override
    protected void updateRoomDataField(Room room, String key, Serializable value) throws IOException {

    }

    @Override
    protected void deleteRoomDataField(Room room, String key) throws IOException {

    }

    @Override
    protected void updateUsersDataField(User user, String key, Serializable value) throws IOException {

    }

    @Override
    protected void deleteUsersDataField(User user, String key) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
