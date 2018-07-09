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

import io.github.jython234.matrix.bridge.db.BridgeDatabase;
import io.github.jython234.matrix.bridge.db.Room;
import io.github.jython234.matrix.bridge.db.User;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Utilities for getting LevelDB keys and values in bytes.
 *
 * @author jython234
 */
public class ByteUtils {

    private static final byte KEY_TYPE_USER = 0;
    private static final byte KEY_TYPE_ROOM = 1;
    private static final byte KEY_TYPE_EXTRA = 2;

    private static final byte ROOM_KEY_TYPE_DATA = 10;
    private static final byte ROOM_KEY_TYPE_MAPPING = 11;

    private static byte[] getKeyValue(String id, byte type) {
        var bytes = id.getBytes();
        var bb = ByteBuffer.allocate(bytes.length + 3);

        bb.put(type);
        bb.putShort((short) bytes.length);
        bb.put(bytes);

        return bb.array();
    }

    /**
     * Get the LevelDB key for a user, provided their ID.
     * @param id The RemoteUser's ID.
     * @return The LevelDB Key.
     */
    public static byte[] getUserKeyValue(String id) {
        return getKeyValue(id, KEY_TYPE_USER);
    }

    public static byte[] getExtraKeyValue(String id) {
        return getKeyValue(id, KEY_TYPE_EXTRA);
    }

    public static byte[] getRoomKeyValue(String id, boolean isMatrixIdMapping) {
        var bytes = id.getBytes();
        var bb = ByteBuffer.allocate(bytes.length + 4);

        bb.put(KEY_TYPE_ROOM);
        bb.put(isMatrixIdMapping ? ROOM_KEY_TYPE_MAPPING : ROOM_KEY_TYPE_DATA);
        bb.putShort((short) bytes.length);
        bb.put(bytes);

        return bb.array();
    }

    public static byte[] serializeUser(User user) throws IOException {
        var baos = new ByteArrayOutputStream();
        var objos = new ObjectOutputStream(baos);

        objos.writeByte(user.type.getIntegerValue());
        objos.writeUTF(user.id);
        objos.writeObject(user.getAdditionalData());

        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static User deserializeUser(byte[] bytes, BridgeDatabase db) throws IOException {
        var bais = new ByteArrayInputStream(bytes);
        var objis = new ObjectInputStream(bais);

        var type = objis.readByte() == User.Type.MATRIX_USER.getIntegerValue() ? User.Type.MATRIX_USER : User.Type.REMOTE_USER;
        var id = objis.readUTF();

        try {
            var data = (Map) objis.readObject();
            return new User(db, type, id, data);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException(e);
        }
    }

    public static byte[] serializeRoom(Room room) throws IOException {
        var baos = new ByteArrayOutputStream();
        var objos = new ObjectOutputStream(baos);

        objos.writeUTF(room.id);
        objos.writeUTF(room.getMatrixId());
        objos.writeObject(room.getAdditionalData());

        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static Room deserializeRoom(byte[] bytes, BridgeDatabase db) throws IOException {
        var bais = new ByteArrayInputStream(bytes);
        var objis = new ObjectInputStream(bais);

        var id = objis.readUTF();
        var matrixId = objis.readUTF();

        try {
            var data = (Map) objis.readObject();
            return new Room(db, id, matrixId, data);
        } catch(ClassNotFoundException | ClassCastException e) {
            throw new IOException(e);
        }
    }

    public static byte[] serializeExtraData(Serializable data) throws IOException {
        var baos = new ByteArrayOutputStream();
        var objos = new ObjectOutputStream(baos);

        objos.writeObject(data);

        return baos.toByteArray();
    }

    public static Serializable deserializeExtraData(byte[] bytes) throws IOException {
        var bais = new ByteArrayInputStream(bytes);
        var objis = new ObjectInputStream(bais);

        try {
            return (Serializable) objis.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
