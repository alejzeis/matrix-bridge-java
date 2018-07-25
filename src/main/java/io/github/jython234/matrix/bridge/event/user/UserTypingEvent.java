package io.github.jython234.matrix.bridge.event.user;

import io.github.jython234.matrix.bridge.MatrixUser;
import io.github.jython234.matrix.bridge.event.Event;

/**
 * This event is thrown whenever a Matrix user starts or stops typing.
 *
 * <strong>NOTICE:</strong> currently the Appservice does <strong>NOT</strong> receive typing
 * events from the homeserver, as that requires syncing. Do not expect this event to be fired until
 * further notice.
 *
 * @author jython234
 */
public class UserTypingEvent extends Event {
    /**
     * The user that was typing or not typing.
     */
    public MatrixUser user;

    /**
     * If the user was typing or not.
     */
    public boolean typing;

    public UserTypingEvent(MatrixUser user, boolean typing) {
        this.user = user;
        this.typing = typing;
    }
}
