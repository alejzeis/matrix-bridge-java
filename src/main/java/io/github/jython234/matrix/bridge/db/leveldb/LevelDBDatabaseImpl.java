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

import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.db.DatabaseException;
import io.github.jython234.matrix.bridge.db.DatabaseWrapper;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

import java.io.File;
import java.io.IOException;

/**
 * LevelDB DatabaseWrapper implementation.
 *
 * @author jython234
 * @see DatabaseWrapper
 */
public class LevelDBDatabaseImpl implements DatabaseWrapper {
    private DB database;

    public LevelDBDatabaseImpl(BridgeConfig.LevelDBInfo levelDbConfigInfo) {
        Options options = new Options().createIfMissing(true)
                .cacheSize(levelDbConfigInfo.cacheSize)
                .compressionType(levelDbConfigInfo.compressionType);

        try {
            this.database = factory.open(new File(levelDbConfigInfo.directory), options);
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
    public void close() throws IOException {
        this.database.close();
    }
}
