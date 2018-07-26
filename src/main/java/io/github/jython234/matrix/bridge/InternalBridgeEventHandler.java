package io.github.jython234.matrix.bridge;

import io.github.jython234.matrix.bridge.event.EventHandler;
import io.github.jython234.matrix.bridge.event.EventHandlerMethod;
import io.github.jython234.matrix.bridge.event.core.BridgedRoomCreatedEvent;
import io.github.jython234.matrix.bridge.event.room.CanonicalRoomAliasChangedEvent;
import org.bson.Document;

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
        System.out.println("Bridge Room created event!");


        var doc = new Document();
        doc.put("id", event.roomId);
        doc.put("primaryAlias", event.roomAlias);

        var room = new MatrixRoom(event.roomId, bridge, doc);
        this.bridge.getDatabase().getMatrixRooms().insertOne(doc, (aVoid, throwable) -> {
            if(throwable != null) {
                this.bridge.getBridgeLogger().error("Failed to insert new room into database after BridgeRoomCreatedEvent!");
                this.bridge.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                synchronized (this.bridge.matrixRooms) {
                    this.bridge.matrixRooms.put(event.roomId, room);
                }
            }
        });
    }

    @EventHandlerMethod
    public void _onCanonicalRoomAliasCahngedEvent(CanonicalRoomAliasChangedEvent event) {
        System.out.println("Room canonical alias changed event!");

        event.room.updateData("primaryAlias", event.alias); // Update the data
    }
}
