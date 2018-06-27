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
package io.github.jython234.matrix.bridge.test;

import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.db.DatabaseWrapper;
import io.github.jython234.matrix.bridge.db.User;
import io.github.jython234.matrix.bridge.db.leveldb.LevelDBDatabaseImpl;
import org.apache.commons.io.FileUtils;
import org.iq80.leveldb.CompressionType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// Contains tests for the databases.
class DatabaseLevelDBTest {
    private static File databaseDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "matrix-bridge-java-testdb");
    private static DatabaseWrapper db;

    // Test Constants
    private static User testUser1 = new User(User.Type.MATRIX_USER, "@i-am-a-matrix_user:localhost");
    private static User testUser2 = new User(User.Type.REMOTE_USER, "41235901732894127341243453456");


    @BeforeAll
    static void init() throws IOException {
        if(databaseDir.exists() && databaseDir.isDirectory()) {
            FileUtils.deleteDirectory(databaseDir); // Need to use commons-io as the directory isn't empty
        }

        var dbInfo = new BridgeConfig.LevelDBInfo();
        dbInfo.directory = databaseDir.getAbsolutePath();
        dbInfo.cacheSize = 128;
        dbInfo.compressionType = CompressionType.NONE;

        db = new LevelDBDatabaseImpl(dbInfo);
    }

    @Test
    @DisplayName("Tests if putting users in the database and deleting them works correctly")
    void testCreationDeletion() throws IOException {
        db.putUser(testUser1);
        db.putUser(testUser2);

        assertTrue(db.userExists(testUser1));
        assertTrue(db.userExists(testUser2));

        db.deleteUser(testUser1);
        db.deleteUser(testUser2);

        assertFalse(db.userExists(testUser1));
        assertFalse(db.userExists(testUser2));
    }

    @Test
    @DisplayName("Checks if a user exists or not in the database")
    void testUserExists() {
        // TODO
    }

    @AfterAll
    static void deinit() throws IOException {
        FileUtils.deleteDirectory(databaseDir); // Need to use commons-io as the directory isn't empty
    }
}
