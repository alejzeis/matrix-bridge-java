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

import java.util.ArrayList;
import java.util.List;

/**
 * Shutdown handler thread for the bridge.
 *
 * @author jython234
 */
public class ShutdownHandler extends Thread {
    private MatrixBridge bridge;

    private final List<Runnable> shutdownTasks = new ArrayList<>();

    ShutdownHandler(MatrixBridge bridge) {
        this.bridge = bridge;

        this.setName("ShutdownThread");
    }

    /**
     * Registers a {@link Runnable} to be ran when the bridge shuts down. The shutdown handler is always
     * called when the JVM exits.
     * @param task The task to be ran on shutdown.
     */
    public void registerShutdownTask(Runnable task) {
        synchronized (this.shutdownTasks) {
            this.shutdownTasks.add(task);
        }
    }

    @Override
    public void run() {
        this.bridge.getBridgeLogger().info("Starting shutdown tasks...");

        // Run all the tasks
        this.shutdownTasks.forEach(Runnable::run);
    }
}
