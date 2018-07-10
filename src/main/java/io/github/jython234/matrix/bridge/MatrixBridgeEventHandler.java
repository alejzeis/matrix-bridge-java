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

import java.lang.reflect.InvocationTargetException;

/**
 * Processes events between the appservice and the bridge.
 *
 * @author jython234
 */
public class MatrixBridgeEventHandler implements EventHandler {
    private MatrixBridge bridge;

    public MatrixBridgeEventHandler(MatrixBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void onMatrixEvent(MatrixEvent matrixEvent) {
        if(this.bridge.eventHandlers.containsKey(matrixEvent.getClass())) {
            this.bridge.eventHandlers.get(matrixEvent.getClass()).forEach((method -> {
                try {
                    method.invoke(this.bridge, matrixEvent);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.bridge.getBridgeLogger().warn("Failed to process event \"" + matrixEvent.getType() + "\" " + matrixEvent.getClass().getName() + ": ");
                    this.bridge.getBridgeLogger().warn(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }));
        }
    }

    @Override
    public CreateRoomRequest onRoomAliasQueried(String s) {
        return this.bridge.onRoomAliasQueried(s);
    }

    @Override
    public void onRoomAliasCreated(String alias) {
        this.bridge.onRoomAliasCreated(alias);
    }

    @Override
    public CreateUserRequest onUserAliasQueried(String s) {
        return null;
    }

    @Override
    public void onUserProvisioned(String localpart) {

    }
}
