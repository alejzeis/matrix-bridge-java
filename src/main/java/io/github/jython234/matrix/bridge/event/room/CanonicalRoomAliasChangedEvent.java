package io.github.jython234.matrix.bridge.event.room;

import io.github.jython234.matrix.bridge.MatrixRoom;
import io.github.jython234.matrix.bridge.event.Event;

/**
 * This event is thrown whenever a Matrix room's canonical alias, or
 * the main alias, is changed.
 *
 * @author jython234
 */
public class CanonicalRoomAliasChangedEvent extends Event {
    /**
     * The Matrix Room which had it's canonical alias changed.
     */
    public MatrixRoom room;
    /**
     * The new canonical alias of the room.
     */
    public String alias;

    public CanonicalRoomAliasChangedEvent(MatrixRoom room, String alias) {
        this.room = room;
        this.alias = alias;
    }
}
