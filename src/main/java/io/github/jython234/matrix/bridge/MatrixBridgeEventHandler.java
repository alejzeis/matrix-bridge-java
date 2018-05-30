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
public final class MatrixBridgeEventHandler implements EventHandler {
    private MatrixBridge bridge;

    public MatrixBridgeEventHandler(MatrixBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void onMatrixEvent(MatrixEvent matrixEvent) {
        if(this.bridge.eventHandlers.containsKey(matrixEvent.getClass())) {
            this.bridge.eventHandlers.get(matrixEvent.getClass()).forEach((method -> {
                try {
                    method.invoke(matrixEvent);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.bridge.getLogger().warn("Failed to process event \"" + matrixEvent.getType() + "\" " + matrixEvent.getClass().getName() + ": ");
                    this.bridge.getLogger().warn(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            }));
        }
    }

    @Override
    public CreateRoomRequest onRoomAliasQueried(String s) {
        return null;
    }

    @Override
    public CreateUserRequest onUserAliasQueried(String s) {
        return null;
    }
}
