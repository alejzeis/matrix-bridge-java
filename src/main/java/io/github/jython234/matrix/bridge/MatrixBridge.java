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
import io.github.jython234.matrix.appservice.exception.KeyNotFoundException;
import io.github.jython234.matrix.appservice.network.CreateRoomRequest;
import io.github.jython234.matrix.appservice.network.CreateUserRequest;
import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.configuration.BridgeConfigLoader;
import io.github.jython234.matrix.bridge.event.EventManager;
import io.github.jython234.matrix.bridge.network.MatrixClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The base class for any Matrix bridge.
 *
 * @author jython234
 */
public abstract class MatrixBridge {
    public static final String SOFTWARE = "matrix-bridge-java";
    public static final String VERSION = "2.0.0-SNAPSHOT";

    private MatrixAppservice appservice;

    private Logger logger;
    private String configDirectory;
    private BridgeConfig config;

    private EventManager eventManager;
    private ShutdownHandler shutdownHandler;

    private MatrixClientManager clientManager;

    /**
     * Create a new instance with the specified information.
     * @param configDirectory Location where all the bridge's configuration files should be located.
     *                        Make sure the program has permissions to read and write in the directory.
     */
    public MatrixBridge(String configDirectory) {
        this.logger = LoggerFactory.getLogger("MatrixBridge");
        this.configDirectory = configDirectory;
        this.loadConfig();

        this.appservice = new MatrixAppservice(configDirectory + File.separator + "registration.yml", this.config.getServerURL());
        this.appservice.setEventHandler(new AppserviceEventHandler(this));

        this.eventManager = new EventManager(this);
        this.eventManager.registerEventHandler(new InternalBridgeEventHandler(this));

        this.clientManager = new MatrixClientManager(this);
        this.shutdownHandler = new ShutdownHandler(this);
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

    /**
     * Start the bridge and it's underlying appservice.
     */
    public void start() {
        this.logger.info("Starting " + SOFTWARE + " v" + VERSION +"...");

        // Code to run when the VM shuts down
        Runtime.getRuntime().addShutdownHook(shutdownHandler);

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
     * If the room ends up being created, then a {@link io.github.jython234.matrix.bridge.event.core.BridgedRoomCreatedEvent} event will be thrown.
     *
     * <strong>NOTICE: </strong> if you chose to create the room make sure {@link CreateRoomRequest#roomAliasName} is the room's <strong>localpart</strong>,
     * which can be determined by using {@link Util#getLocalpart(String)}
     *
     * @param alias The room's alias.
     * @return A {@link CreateRoomRequest} instance or <code>null</code>. If you want the room to be created, and the user
     *         allowed to join, then return a {@link CreateRoomRequest} instance. If you don't want the room to be created,
     *         then return <code>null</code>.
     */
    protected CreateRoomRequest onRoomAliasQueried(String alias) {
        return null; // Do not create room by default
    }

    /**
     * This method is called whenever someone attempts to query a user that resides in the application service's
     * exclusive user space, and the user doesn't exist.
     * @param userId The user's matrix User ID.
     * @return A {@link CreateUserRequest} instance or <code>null</code> If you want the user to be created, return a {@link CreateUserRequest} instance.
     *          If you do not want the user created, then simply return <code>null</code>.
     */
    protected CreateUserRequest onUserQueried(String userId) {
        return null; // Do not create by default
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

    public MatrixClientManager getClientManager() {
        return clientManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ShutdownHandler getShutdownHandler() {
        return this.shutdownHandler;
    }
}
