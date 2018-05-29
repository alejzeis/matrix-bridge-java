package io.github.jython234.matrix.bridge;

import java.lang.annotation.*;

/**
 * Flags a method as an event handler for a Matrix Event.
 *
 * To use this, simply create a method that takes a parameter of the
 * specific {@link io.github.jython234.matrix.appservice.event.MatrixEvent} you want to handle, and
 * mark it with this annotation. This must be done in your class that extends {@link MatrixBridge}.
 *
 * The method will be called whenever that Matrix Event is received.
 *
 * @author jython234
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MatrixEventHandler {
}
