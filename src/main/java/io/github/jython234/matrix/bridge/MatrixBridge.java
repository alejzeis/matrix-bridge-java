/*
 * Copyright © 2018, jython234
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.github.jython234.matrix.bridge;

import io.github.jython234.matrix.appservice.MatrixAppservice;
import io.github.jython234.matrix.appservice.Util;
import io.github.jython234.matrix.appservice.event.MatrixEvent;
import io.github.jython234.matrix.appservice.exception.KeyNotFoundException;
import io.github.jython234.matrix.appservice.network.CreateRoomRequest;
import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.configuration.BridgeConfigLoader;
import io.github.jython234.matrix.bridge.db.BridgeDatabase;
import io.github.jython234.matrix.bridge.db.leveldb.LevelDBDatabaseImpl;
import io.github.jython234.matrix.bridge.db.mongo.MongoDatabaseImpl;
import io.github.jython234.matrix.bridge.network.MatrixClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    public static final String SOFTWARE = "matrix-bridge-java";
    public static final String VERSION = "1.4.5-SNAPSHOT";

    private MatrixAppservice appservice;

    private Logger logger;
    private String configDirectory;
    private BridgeConfig config;

    private BridgeDatabase database;

    protected Map<Class<? extends MatrixEvent>, List<Method>> eventHandlers;

    private MatrixClientManager clientManager;

    /**
     * Create a new instance with the specified information.
     * @param configDirectory Location where all the bridge's configuration files should be located.
     *                        Make sure the program has permissions to read and write in the directory.
     */
    public MatrixBridge(String configDirectory) {
        this(configDirectory, null);
    }

    /**
     * Create a new instance with the specified information.
     * @param configDirectory Location where all the bridge's configuration files should be located.
     *                        Make sure the program has permissions to read and write in the directory.
     * @param eventHandler A custom Matrix Event handler to receive events directly from the appservice.
     *                     In most cases, this is not needed, so please use the other constructor instead.
     * @see MatrixBridgeEventHandler
     * @see io.github.jython234.matrix.appservice.event.EventHandler
     */
    public MatrixBridge(String configDirectory, MatrixBridgeEventHandler eventHandler) {
        this.logger = LoggerFactory.getLogger("MatrixBridge");
        this.configDirectory = configDirectory;
        this.loadConfig();

        this.appservice = new MatrixAppservice(configDirectory + File.separator + "registration.yml", this.config.getServerURL());
        this.appservice.setEventHandler(eventHandler == null ? new MatrixBridgeEventHandler(this) : eventHandler);

        this.eventHandlers = new ConcurrentHashMap<>();
        this.findEventHandlers();

        this.setupDatabase();

        this.clientManager = new MatrixClientManager(this);
    }

    private void loadConfig() {
        File configLocation = new File(this.configDirectory + File.separator + "bridge.yml");
        this.logger.info("Loading configuration: " + configLocation.getAbsolutePath());

        if(!configLocation.exists()) {
            this.logger.warn("Configuration does not exist, copying default.");

            try {
                Util.copyResourceTo("defaultConfig.yml", configLocation);
                this.logger.info("Default configuration copied. It is recommended you change the default values.");
            } catch (IOException e) {
                this.logger.error("Failed to copy default config! IOException");
                e.printStackTrace();
                System.exit(1);
            }
        }

        try {
            this.config = BridgeConfigLoader.loadFromFile(configLocation);
            this.logger.info("Configuration loaded.");
        } catch (FileNotFoundException e) {
            this.logger.error("Configuration file not found! " + configLocation.getPath());
            e.printStackTrace();
            System.exit(1);
        } catch (KeyNotFoundException e) {
            this.logger.error("YAML configuration file invalid! Missing key(s)!");
            e.printStackTrace();
            System.exit(1);
        }
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

    private void setupDatabase() {
        switch (this.config.getDbInfo().type) {
            case MONGO:
                this.database = new MongoDatabaseImpl(this, (BridgeConfig.MongoDBInfo) this.config.getDbInfo());
                break;
            case LEVELDB:
                this.database = new LevelDBDatabaseImpl(this, (BridgeConfig.LevelDBInfo) this.config.getDbInfo());
                break;
        }
    }

    /**
     * Start the bridge and it's underlying appservice.
     */
    public void start() {
        this.logger.info("Starting " + SOFTWARE + " v" + VERSION +"...");

        // Code to run when the VM shuts down
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                this.setName("ShutdownThread");

                logger.info("Running shutdown hook!");
                try {
                    database.close();
                    logger.info("Closed database");
                } catch (IOException e) {
                    logger.warn("Failed to close database on exit!");
                    logger.error("IOException: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    onStop();
                }
            }
        });

        this.onStart();
        this.appservice.run(new String[]{"--server.port=" + this.config.getAppservicePort()});
    }

    /**
     * Called when the bridge is about to start.
     */
    protected abstract void onStart();

    /**
     * Called when the bridge is about to stop.
     */
    protected abstract void onStop();

    /**
     * This method is called whenever someone attempts to join a room that has it's
     * alias reserved to this bridge, and it doesn't exist.
     *
     * For example, the bridge may have the <code>#bridge_*.*</code> room alias space reserved, and
     * a user may attempt to join a room with the address <code>#bridge_myroom</code>. This method would
     * then be called if the room doesn't exist.
     *
     * If the room ends up being created, then the {@link MatrixBridge#onRoomAliasCreated(String, String)} method will be called.
     * @param alias The room's alias.
     * @return A {@link CreateRoomRequest} instance or <code>null</code>. If you want the room to be created, and the user
     *         allowed to join, then return a {@link CreateRoomRequest} instance. If you don't want the room to be created,
     *         then return <code>null</code>.
     * @see MatrixBridge#onRoomAliasCreated(String, String)
     */
    protected CreateRoomRequest onRoomAliasQueried(String alias) {
        return null; // Do not create room by default
    }

    /**
     * This method is called whenever a room is created after the {@link MatrixBridge#onRoomAliasQueried(String)} method.
     * @param alias The room's alias.
     * @param id The room's matrix ID.
     */
    protected void onRoomAliasCreated(String alias, String id) {
        // Stub to be overridden
    }

    public Logger getBridgeLogger() {
        return this.logger;
    }

    public BridgeConfig getConfig() {
        return this.config;
    }

    public MatrixAppservice getAppservice() {
        return this.appservice;
    }

    public BridgeDatabase getDatabase() {
        return this.database;
    }

    public MatrixClientManager getClientManager() {
        return clientManager;
    }
}
