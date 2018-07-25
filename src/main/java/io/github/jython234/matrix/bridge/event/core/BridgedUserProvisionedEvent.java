package io.github.jython234.matrix.bridge.event.core;

import io.github.jython234.matrix.bridge.event.Event;

/**
 * This event is thrown whenever the homeserver provisions a user in response to the
 * {@link io.github.jython234.matrix.bridge.MatrixBridge#onUserQueried(String)} method. If a user is created
 * in response to the event, then this event will be fired.
 *
 * @author jython234
 */
public class BridgedUserProvisionedEvent extends Event {
    /**
     * The localpart of the user that was created.
     */
    public String userLocalpart;

    public BridgedUserProvisionedEvent(String userLocalpart) {
        this.userLocalpart = userLocalpart;
    }
}
