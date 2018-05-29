package io.github.jython234.matrix.bridge;

import io.github.jython234.matrix.appservice.event.EventHandler;
import io.github.jython234.matrix.appservice.event.MatrixEvent;
import io.github.jython234.matrix.appservice.network.CreateRoomRequest;
import io.github.jython234.matrix.appservice.network.CreateUserRequest;

/**
 * Processes events between the appservice and the bridge.
 *
 * @author jython234
 */
class MatrixBridgeEventHandler implements EventHandler {
    private MatrixBridge bridge;

    MatrixBridgeEventHandler(MatrixBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void onMatrixEvent(MatrixEvent matrixEvent) {

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
