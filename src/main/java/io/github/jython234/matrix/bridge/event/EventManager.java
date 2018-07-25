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
