package io.github.jython234.matrix.bridge;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

public class RemoteUser extends User {

    RemoteUser(String id, MatrixBridge bridge, Document data) {
        super(id, bridge, data);
    }

    @Override
    public void updateData(String key, Object value, SingleResultCallback<UpdateResult> callback) {
        super.updateData(key, value, callback);

        this.bridge.getDatabase().getRemoteUsers().updateOne(Filters.eq("id", this.id), Updates.set(key, value), callback);
    }
}
