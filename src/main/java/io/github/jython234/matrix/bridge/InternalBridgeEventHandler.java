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

import io.github.jython234.matrix.bridge.event.EventHandler;
import io.github.jython234.matrix.bridge.event.EventHandlerMethod;
import io.github.jython234.matrix.bridge.event.core.BridgedRoomCreatedEvent;
import io.github.jython234.matrix.bridge.event.core.PureMatrixEvent;
import io.github.jython234.matrix.bridge.event.room.CanonicalRoomAliasChangedEvent;
import org.bson.Document;

/**
 * An internal bridge Event listener to handle some specific
 * events.
 *
 * @author jython234
 */
class InternalBridgeEventHandler extends EventHandler {
    private MatrixBridge bridge;

    InternalBridgeEventHandler(MatrixBridge bridge) {
        super();

        this.bridge = bridge;
    }

    @EventHandlerMethod
    public void _onPureMatrixEvent(PureMatrixEvent event) {
        // TODO: translate events to their simple counterparts
    }

    @EventHandlerMethod
    public void _onBridgeRoomCreatedEvent(BridgedRoomCreatedEvent event) {
        System.out.println("Bridge Room created event!");


        var doc = new Document();
        doc.put("id", event.roomId);
        doc.put("primaryAlias", event.roomAlias);

        var room = new MatrixRoom(event.roomId, bridge, doc);
        this.bridge.getDatabase().getMatrixRooms().insertOne(doc, (aVoid, throwable) -> {
            if(throwable != null) {
                this.bridge.getBridgeLogger().error("Failed to insert new room into database after BridgeRoomCreatedEvent!");
                this.bridge.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                synchronized (this.bridge.matrixRooms) {
                    this.bridge.matrixRooms.put(event.roomId, room);
                }
            }
        });
    }

    @EventHandlerMethod
    public void _onCanonicalRoomAliasCahngedEvent(CanonicalRoomAliasChangedEvent event) {
        System.out.println("Room canonical alias changed event!");

        event.room.updateData("primaryAlias", event.alias); // Update the data
    }
}
