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
package io.github.jython234.matrix.bridge;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;

/**
 * The base class for any User, either a Matrix User or a Remote User
 * (one on the remotely bridged application).
 *
 * @author jython234
 */
public abstract class User {
    protected final MatrixBridge bridge;
    public final String id;

    private Document data;

    public User(String id, MatrixBridge bridge, Document data) {
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
