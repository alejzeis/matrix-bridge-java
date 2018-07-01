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
package io.github.jython234.matrix.bridge.db.leveldb;

import io.github.jython234.matrix.bridge.MatrixBridge;
import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.db.DatabaseException;
import io.github.jython234.matrix.bridge.db.BridgeDatabase;
import io.github.jython234.matrix.bridge.db.Room;
import io.github.jython234.matrix.bridge.db.User;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * LevelDB BridgeDatabase implementation.
 *
 * @author jython234
 * @see BridgeDatabase
 */
public class LevelDBDatabaseImpl extends BridgeDatabase {
    public static final byte[] DB_VERSION_MAGIC = new byte[] {(byte) 0xDB, (byte) 0xFE};
    /**
     * Database storage version. If it doesn't match our current one the database
     * will need to be upgraded.
     */
    public static final byte DB_VERSION = 2;

    private DB database;

    public LevelDBDatabaseImpl(MatrixBridge bridge, BridgeConfig.LevelDBInfo levelDbConfigInfo) {
        super(bridge, "LevelDBImpl");

        var options = new Options().createIfMissing(true)
                .cacheSize(levelDbConfigInfo.cacheSize)
                .compressionType(levelDbConfigInfo.compressionType);

        this.logger.info("Opening LevelDB database with " + levelDbConfigInfo.cacheSize + "MB cache size and Compression: " + levelDbConfigInfo.compressionType.name());
        try {
            this.database = factory.open(new File(levelDbConfigInfo.directory), options);

            // Check the version
            var version = this.database.get(DB_VERSION_MAGIC);
            if(version == null) {
                // Assume database was just created
                this.database.put(DB_VERSION_MAGIC, new byte[] {DB_VERSION});
            } else if(version[0] != DB_VERSION) {
                this.logger.error("Database version " + version[0] + " does not match ours! (" + DB_VERSION +")");
                this.logger.error("Either this matrix-bridge-java version is outdated, or the database was created with a previous version");
                this.logger.error("Repair needed!");
                throw new DatabaseException("Database version mismatch");
            }

        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }
    @Override
    public boolean userExists(String id) {
        // If the get call returns null then that user doesn't exist in the database.
        return this.database.get(ByteUtils.getUserKeyValue(id)) != null;
    }

    @Override
    public void putUser(User user) throws IOException {
        var data = ByteUtils.serializeUser(user);
        this.database.put(ByteUtils.getUserKeyValue(user.id), data);
    }

    @Override
    public User getUser(String id) throws IOException {
        var data = this.database.get(ByteUtils.getUserKeyValue(id));
        return data != null ? ByteUtils.deserializeUser(data, this) : null;
    }

    @Override
    public void deleteUser(String id) {
        this.database.delete(ByteUtils.getUserKeyValue(id));
    }

    @Override
    public boolean roomExists(String id) {
        return this.database.get(ByteUtils.getRoomKeyValue(id, false)) != null;
    }

    @Override
    public boolean roomExistsByMatrixId(String matrixId) {
        var key = this.database.get(ByteUtils.getRoomKeyValue(matrixId, true));
        if(key == null) return false;

        return this.database.get(key) != null;
    }

    @Override
    public void putRoom(Room room) throws IOException {
        this._putRoom(room, true);
    }

    protected void _putRoom(Room room, boolean doReverseMapping) throws IOException {
        var key = ByteUtils.getRoomKeyValue(room.id, false);

        this.database.put(key, ByteUtils.serializeRoom(room));
        if(doReverseMapping && room.getMatrixId() != null && !room.getMatrixId().equals("")) {
            // Add a reverse mapping with the key being the matrixId and the value as the normal id, so we can retrieve the actual Room data
            // if we have either the normal id or the matrixId
            this.database.put(ByteUtils.getRoomKeyValue(room.getMatrixId(), true), key);
        }
    }

    @Override
    public Room getRoom(String id) throws IOException {
        var data = this.database.get(ByteUtils.getRoomKeyValue(id, false));
        return data != null ? ByteUtils.deserializeRoom(data, this) : null;
    }

    @Override
    public Room getRoomByMatrixId(String matrixId) throws IOException {
        var key = this.database.get(ByteUtils.getRoomKeyValue(matrixId, true));
        if(key == null) return null;

        var data = this.database.get(key);
        return data != null ? ByteUtils.deserializeRoom(data, this) : null;
    }

    @Override
    public void deleteRoom(Room room) {
        this.database.delete(ByteUtils.getRoomKeyValue(room.id, false));
        this.database.delete(ByteUtils.getRoomKeyValue(room.getMatrixId(), true));
    }

    @Override
    public void deleteRoom(String id) throws IOException {
        Room room = this.getRoom(id); // We need to delete the reverse mapping too, so we need to know the matrix ID
        this.deleteRoom(room);
    }

    @Override
    protected void updateRoomMatrixId(Room room, String matrixId) throws IOException {
        this._putRoom(room, true); // LevelDB doesn't support any specific updating so we'll just overwrite the entry.
    }

    @Override
    protected void updateRoomDataField(Room room, String key, Serializable value) throws IOException {
        this._putRoom(room, false); // LevelDB doesn't support any specific updating so we'll just overwrite the entry.
    }

    @Override
    protected void deleteRoomDataField(Room room, String key) throws IOException {
        this._putRoom(room, false); // LevelDB doesn't support any specific updating so we'll just overwrite the entry.
    }

    @Override
    protected void updateUsersDataField(User user, String key, Serializable value) throws IOException {
        this.putUser(user); // LevelDB doesn't support any specific updating so we'll just overwrite the entry.
    }

    @Override
    protected void deleteUsersDataField(User user, String key) throws IOException {
        this.putUser(user); // LevelDB doesn't support any specific updating so we'll just overwrite the entry.
    }

    @Override
    public void close() throws IOException {
        this.database.close();
    }
}
