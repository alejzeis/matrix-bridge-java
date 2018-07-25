package io.github.jython234.matrix.bridge;

import io.github.jython234.matrix.bridge.event.EventHandler;
import io.github.jython234.matrix.bridge.event.EventHandlerMethod;
import io.github.jython234.matrix.bridge.event.core.BridgedRoomCreatedEvent;

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
    public void _onBridgeRoomCreatedEvent(BridgedRoomCreatedEvent event) {
        // TODO: add room to database and such
    }
}
