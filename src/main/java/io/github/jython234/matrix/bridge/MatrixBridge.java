package io.github.jython234.matrix.bridge;

import io.github.jython234.matrix.appservice.MatrixAppservice;
import io.github.jython234.matrix.appservice.event.MatrixEvent;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The base class for any Matrix bridge.
 *
 * @author jython234
 */
public abstract class MatrixBridge {
    @Getter private MatrixAppservice appservice;
    @Getter private Logger logger;
    protected Map<Class<? extends MatrixEvent>, List<Method>> eventHandlers;

    /**
     * Create a new instance with the specified information.
     * @param configDirectory Location where all the bridge's configuration files should be located.
     *                        Make sure the program has permissions to read and write in the directory.
     * @param serverURL The URL of the matrix homeserver.
     */
    // TODO: load serverURL from bridge configuration file
    public MatrixBridge(String configDirectory, String serverURL) {
        this.logger = LoggerFactory.getLogger("MatrixBridge");

        this.appservice = new MatrixAppservice(configDirectory + File.separator + "registration.yml", serverURL);
        this.appservice.setEventHandler(new MatrixBridgeEventHandler(this));

        this.eventHandlers = new ConcurrentHashMap<>();
        this.findEventHandlers();
    }

    private void findEventHandlers() {
        // Finds event handling methods in this instance's subclass with Reflection
        final var methods = this.getClass().getDeclaredMethods();
        for(var method: methods) {
            for(var annotation: method.getDeclaredAnnotations()) {
                if(annotation instanceof MatrixEventHandler
                        && method.getParameterTypes().length > 0
                        && MatrixEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {

                    // We found an event handler method! Now we add it to our map
                    final var type = method.getParameterTypes()[0].asSubclass(MatrixEvent.class);
                    if(this.eventHandlers.containsKey(type)) {
                        this.eventHandlers.get(type).add(method);
                    } else {
                        final List<Method> list = new CopyOnWriteArrayList<>();
                        list.add(method);
                        this.eventHandlers.put(type, list);
                    }
                }
            }
        }
    }

    /**
     * Start the bridge and it's underlying appservice.
     */
    public void start() {
        // TODO: appservice port
        this.appservice.run(new String[]{});
    }

    protected abstract void onStart();
    protected abstract void onStop();
}
