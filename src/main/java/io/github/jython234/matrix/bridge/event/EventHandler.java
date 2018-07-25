package io.github.jython234.matrix.bridge.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a class that can handle events. The implementing class
 * will have methods marked with {@link EventHandlerMethod} accepting any
 * {@link Event}s.
 *
 * <strong>NOTICE:</strong> please make sure to call <code>super()</code> if you override the constructor, as
 * some critical code runs in the superclass's constructor.
 *
 * @author jython234
 */
public abstract class EventHandler {
    private final Map<Class<? extends Event>, Method> handlerMethods = new HashMap<>();

    public EventHandler() {
        this.findEventHandlers();
    }

    private void findEventHandlers() {
        // Finds event handling methods in this instance's subclass with Reflection
        final var methods = this.getClass().getDeclaredMethods();
        for(var method: methods) {
            for(var annotation: method.getDeclaredAnnotations()) {
                if(annotation instanceof EventHandlerMethod
                        && method.getParameterTypes().length > 0
                        && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {

                    // We found an event handler method! Now we add it to our map
                    final var type = method.getParameterTypes()[0].asSubclass(Event.class);
                    this.handlerMethods.put(type, method);
                }
            }
        }
    }

    void processEvent(Event event) throws InvocationTargetException, IllegalAccessException {
        if(handlerMethods.containsKey(event.getClass())) {
            // Invoke the method, passing the Event instance
            handlerMethods.get(event.getClass()).invoke(this, event);
        }
    }
}
