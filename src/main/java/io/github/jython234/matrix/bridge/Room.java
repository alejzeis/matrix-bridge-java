package io.github.jython234.matrix.bridge;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

/**
 * The base class for any Room, either Matrix or Remote.
 *
 * @author jython234
 */
public abstract class Room {
    protected final MatrixBridge bridge;
    public final String id;

    private Document data;

    public Room(String id, MatrixBridge bridge, Document data) {
        this.id = id;
        this.bridge = bridge;
        this.data = data;
    }

    public void updateData(String key, Object value) {
        this.updateData(key, value, (updateResult, throwable) -> { }); // Ignore callback
    }

    public void updateData(String key, Object value, SingleResultCallback<UpdateResult> callback) {
        this.getDatabaseData().put(key, value);
    }

    /**
     * This document stores data that is stored in the bridge database.
     *
     * <strong>NOTICE:</strong> If you are modifying data use {@link #updateData(String, Object)} or {@link #updateData(String, Object, SingleResultCallback)}
     * @return The Document containing the data.
     */
    public Document getDatabaseData() {
        return data;
    }
}
