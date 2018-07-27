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
package io.github.jython234.matrix.bridge.event;

import io.github.jython234.matrix.bridge.MatrixBridge;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The EventManager handles processing and throwing events.
 *
 * @author jython234
 */
public class EventManager {
    private MatrixBridge bridge;

    private final List<EventHandler> eventHandlers = new ArrayList<>();

    public EventManager (MatrixBridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Registers an EventHandler to the bridge. It will then receive
     * events.
     * @param handler The {@link EventHandler} to be registered.
     */
    public void registerEventHandler (EventHandler handler) {
        synchronized (this.eventHandlers) {
            this.bridge.getBridgeLogger().info("Registered EventHandler " + handler.getClass().getCanonicalName());
            this.eventHandlers.add(handler);
        }
    }

    /**
     * Removes an EventHandler from the bridge. It will then no longer
     * receive events.
     * @param handler The {@link EventHandler} to be unregistered.
     */
    public void unregisterEventHandler (EventHandler handler) {
        synchronized (this.eventHandlers) {
            this.eventHandlers.remove(handler);
            this.bridge.getBridgeLogger().info("Unregistered EventHandler " + handler.getClass().getCanonicalName());
        }
    }

    /**
     * Throws an event and sends it to every single EventHandler to be handled.
     * This executes in the current thread.
     * @param event The event to be handled.
     * @see #throwEventAsync(Event)
     */
    public void throwEvent(Event event) {
        eventHandlers.forEach(eventHandler -> {
            try {
                eventHandler.processEvent(event);
            } catch (InvocationTargetException e) {
                this.bridge.getBridgeLogger().error("Exception while attempting to process event of type " + event.getClass().getName());
                e.printStackTrace(System.err);
            } catch (IllegalAccessException e) {
                this.bridge.getBridgeLogger().error("IllegalAccessException while attempting to process event of type " + event.getClass().getName());
                e.printStackTrace(System.err);
            }
        });
    }

    /**
     * Async version of {@link #throwEvent(Event)}.
     *
     * Executes in a separate worker thread.
     * @param event The event to be handled.
     */
    public void throwEventAsync(Event event) {
        this.bridge.getAppservice().threadPoolTaskExecutor.submit(() -> this.throwEvent(event));
    }
}
