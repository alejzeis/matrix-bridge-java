package io.github.jython234.matrix.bridge.event.core;

import io.github.jython234.matrix.bridge.MatrixUser;
import io.github.jython234.matrix.bridge.event.Event;

/**
 * An event thrown whenever a Matrix User was registered on the homeserver via
 * the means of {@link io.github.jython234.matrix.bridge.MatrixBridge#registerMatrixUser(String)}
 *
 * @author jython234
 */
public class MatrixUserRegisteredEvent extends Event {
    /**
     * The user that was registered.
     */
    public final MatrixUser user;

    public MatrixUserRegisteredEvent(MatrixUser user) {
        this.user = user;
    }
}
