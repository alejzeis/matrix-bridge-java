package io.github.jython234.matrix.bridge.event.core;

import io.github.jython234.matrix.appservice.event.MatrixEvent;
import io.github.jython234.matrix.bridge.event.Event;

/**
 * Represents a Pure Matrix event directly from the Appservice.
 * This class solely contains a {@link io.github.jython234.matrix.appservice.event.MatrixEvent}
 * which is your job to parse.
 *
 * It's recommended to use one of the implemented events part of the other packages before
 * parsing events directly from the Appservice.
 *
 * @author jython234
 */
public class PureMatrixEvent extends Event {
    /**
     * The pure event directly from the Appservice.
     */
    public MatrixEvent matrixEvent;

    public PureMatrixEvent(MatrixEvent event) {
        this.matrixEvent = event;
    }
}
