package io.github.jython234.matrix.bridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Shutdown handler thread for the bridge.
 *
 * @author jython234
 */
public class ShutdownHandler extends Thread {
    private MatrixBridge bridge;

    private final List<Runnable> shutdownTasks = new ArrayList<>();

    ShutdownHandler(MatrixBridge bridge) {
        this.bridge = bridge;

        this.setName("ShutdownThread");
    }

    /**
     * Registers a {@link Runnable} to be ran when the bridge shuts down. The shutdown handler is always
     * called when the JVM exits.
     * @param task The task to be ran on shutdown.
     */
    public void registerShutdownTask(Runnable task) {
        synchronized (this.shutdownTasks) {
            this.shutdownTasks.add(task);
        }
    }

    @Override
    public void run() {
        this.bridge.getBridgeLogger().info("Starting shutdown tasks...");

        // Run all the tasks
        this.shutdownTasks.forEach(Runnable::run);
    }
}
