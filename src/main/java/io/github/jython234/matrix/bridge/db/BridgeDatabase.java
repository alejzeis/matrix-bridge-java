package io.github.jython234.matrix.bridge.db;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import io.github.jython234.matrix.bridge.MatrixBridge;
import org.bson.Document;

import java.io.Closeable;
import java.io.IOException;

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
