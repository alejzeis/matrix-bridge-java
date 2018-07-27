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

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import io.github.jython234.matrix.bridge.MatrixBridge;
import org.bson.Document;

import java.io.Closeable;

/**
 * Represents the bridge's database to store user and room information.
 *
 * @author jython234
 */
public class BridgeDatabase implements Closeable {
    private MatrixBridge bridge;

    private MongoClient client;
    private MongoDatabase database;

    private MongoCollection<Document> matrixUsers;
    private MongoCollection<Document> remoteUsers;
    private MongoCollection<Document> matrixRooms;
    private MongoCollection<Document> remoteRooms;

    public BridgeDatabase(MatrixBridge bridge) {
        this.bridge = bridge;

        // Create the Mongo Client
        this.client = MongoClients.create(bridge.getConfig().getDbInfo().url);
        // Get the database
        this.database = this.client.getDatabase(bridge.getConfig().getDbInfo().database);

        this.matrixUsers = this.database.getCollection("matrixUsers");
        this.remoteUsers = this.database.getCollection("remoteUsers");
        this.matrixRooms = this.database.getCollection("matrixRooms");
        this.remoteRooms = this.database.getCollection("remoteRooms");
    }

    @Override
    public void close() {
        this.client.close();
    }

    public MongoCollection<Document> getMatrixUsers() {
        return matrixUsers;
    }

    public MongoCollection<Document> getRemoteUsers() {
        return remoteUsers;
    }

    public MongoCollection<Document> getMatrixRooms() {
        return matrixRooms;
    }

    public MongoCollection<Document> getRemoteRooms() {
        return remoteRooms;
    }
}
