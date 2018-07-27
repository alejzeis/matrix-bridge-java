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
package io.github.jython234.matrix.bridge.event.core;

import io.github.jython234.matrix.bridge.event.Event;

/**
 * This event is thrown whenever a room in one of the appservice's exclusive zones
 * is created. It is thrown once a room is created in response to the {@link io.github.jython234.matrix.bridge.MatrixBridge#onRoomAliasQueried(String)}
 * method.
 *
 * The internal bridge EventHandler listens for this event and creates the necessary entries in the bridge database,
 * however you should probably listen for this event to too add mappings in the database from your remote room entry to
 * the newly bridged room.
 *
 * @author jython234
 */
public class BridgedRoomCreatedEvent extends Event {
    /**
     * The Matrix room alias of the room.
     *
     * Example: #myroom:localserver.net
     */
    public String roomAlias;
    /**
     * The Matrix room ID of the room.
     */
    public String roomId;

    public BridgedRoomCreatedEvent(String roomAlias, String roomId) {
        this.roomAlias = roomAlias;
        this.roomId = roomId;
    }
}
