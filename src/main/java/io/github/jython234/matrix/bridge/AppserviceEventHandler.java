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

import io.github.jython234.matrix.appservice.event.EventHandler;
import io.github.jython234.matrix.appservice.event.MatrixEvent;
import io.github.jython234.matrix.appservice.network.CreateRoomRequest;
import io.github.jython234.matrix.appservice.network.CreateUserRequest;
import io.github.jython234.matrix.bridge.event.core.BridgedRoomCreatedEvent;
import io.github.jython234.matrix.bridge.event.core.BridgedUserProvisionedEvent;

/**
 * Processes events between the appservice and the bridge.
 *
 * @author jython234
 */
class AppserviceEventHandler implements EventHandler {
    private MatrixBridge bridge;

    AppserviceEventHandler(MatrixBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void onMatrixEvent(MatrixEvent matrixEvent) {
        // TODO: translate events

    }

    @Override
    public CreateRoomRequest onRoomAliasQueried(String s) {
        return this.bridge.onRoomAliasQueried(s);
    }

    @Override
    public void onRoomAliasCreated(String alias, String id) {
        this.bridge.getEventManager().throwEvent(new BridgedRoomCreatedEvent(alias, id));
    }

    @Override
    public CreateUserRequest onUserAliasQueried(String userId) {
        return this.bridge.onUserQueried(userId);
    }

    @Override
    public void onUserProvisioned(String localpart) {
        this.bridge.getEventManager().throwEvent(new BridgedUserProvisionedEvent(localpart));
    }
}
