package io.github.jython234.matrix.bridge.event.core;

import io.github.jython234.matrix.bridge.event.Event;

/**
 * This event is thrown whenever a room in one of the appservice's exclusive zones
 * is created. It is thrown once a room is created in response to the {@link io.github.jython234.matrix.bridge.MatrixBridge#onRoomAliasQueried(String)}
 * method.
 *
 * The internal bridge EventHandler listens for this event and creates the necessary entries in the bridge database,
 * however you may chose to listen to this event as-well in case you have any specific code to run.
 *
 * @author jython234
 */
public class BridgedRoomCreatedEvent extends Event {
    /**
     * The Matrix room alias of the room.
     *
     * Example: #myroom:localserver.net
     */
    public String roomAlias;
    /**
     * The Matrix room ID of the room.
     */
    public String roomId;

    public BridgedRoomCreatedEvent(String roomAlias, String roomId) {
        this.roomAlias = roomAlias;
        this.roomId = roomId;
    }
}
