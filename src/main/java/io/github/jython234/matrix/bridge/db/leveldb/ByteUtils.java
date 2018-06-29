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
    /**
     * Database storage version. If it doesn't match our current one the database
     * will need to be upgraded.
     */
    public static final int DB_VERSION = 1;

    /**
     * Get the LevelDB key for a user, provided their ID.
     * @param id The RemoteUser's ID.
     * @return The LevelDB Key.
     */
    public static byte[] getUserKeyValue(String id) {
        var bytes = id.getBytes();
        var bb = ByteBuffer.allocate(bytes.length + 2);

        bb.putShort((short) bytes.length);
        bb.put(bytes);

        return bb.array();
    }

    public static byte[] serializeUser(User user) throws IOException {
        var baos = new ByteArrayOutputStream();
        var objos = new ObjectOutputStream(baos);

        objos.writeByte(user.type.getIntegerValue());
        objos.writeUTF(user.id);
        objos.writeUTF(user.getName());
        objos.writeObject(user.getAdditionalData());

        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static User deserializeUser(byte[] bytes, BridgeDatabase db) throws IOException {
        var bais = new ByteArrayInputStream(bytes);
        var objis = new ObjectInputStream(bais);

        var type = objis.readByte() == User.Type.MATRIX_USER.getIntegerValue() ? User.Type.MATRIX_USER : User.Type.REMOTE_USER;
        var id = objis.readUTF();
        var name = objis.readUTF();

        try {
            var data = (Map) objis.readObject();
            return new User(db, type, id, name, data);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException(e);
        }
    }
}
