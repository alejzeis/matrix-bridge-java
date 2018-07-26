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

import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import io.github.jython234.matrix.appservice.MatrixAppservice;
import io.github.jython234.matrix.appservice.Util;
import io.github.jython234.matrix.appservice.exception.KeyNotFoundException;
import io.github.jython234.matrix.appservice.network.CreateRoomRequest;
import io.github.jython234.matrix.appservice.network.CreateUserRequest;
import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.configuration.BridgeConfigLoader;
import io.github.jython234.matrix.bridge.db.BridgeDatabase;
import io.github.jython234.matrix.bridge.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * The base class for any Matrix bridge. Implementing bridges should extend this class.
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
    private BridgeDatabase database;

    private EventManager eventManager;
    private ShutdownHandler shutdownHandler;

    final Map<String, MatrixRoom> matrixRooms = new HashMap<>();
    final Map<String, MatrixUser> matrixUsers = new HashMap<>();

    private final Map<String, RemoteRoom> remoteRooms = new HashMap<>();
    private final Map<String, RemoteUser> remoteUsers = new HashMap<>();

    /**
     * Create a new instance with the specified information.
     * @param configDirectory Location where all the bridge's configuration files should be located.
     *                        Make sure the program has permissions to read and write in the directory.
     */
    public MatrixBridge(String configDirectory) {
        this.logger = LoggerFactory.getLogger("Bridge");
        this.configDirectory = configDirectory;
        this.loadConfig();

        this.database = new BridgeDatabase(this);

        this.appservice = new MatrixAppservice(configDirectory + File.separator + "registration.yml", this.config.getServerURL());
        this.appservice.setEventHandler(new AppserviceEventHandler(this));

        this.eventManager = new EventManager(this);
        this.eventManager.registerEventHandler(new InternalBridgeEventHandler(this));

        this.shutdownHandler = new ShutdownHandler(this);

        this.syncRemote();
        this.syncMatrix();
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

    private void syncRemote() {
        // All we have to do is just find every room and user in the database and add it to the map

        this.database.getRemoteUsers().find().forEach(document -> {
            if(!document.containsKey("id")) {
                this.logger.warn("Found document in RemoteUsers collection that does not have an \"id\", skipping...");

            } else {
                var id = document.get("id", String.class);
                var user = new RemoteUser(id, this, document);

                synchronized (this.remoteUsers) {
                    this.remoteUsers.put(id, user);
                }
            }
        }, (result, throwable) -> {
            if(throwable != null) {
                this.getBridgeLogger().error("Error while attempting to retrieve RemoteUsers from database!");
                this.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });

        this.database.getRemoteRooms().find().forEach(document -> {
            if(!document.containsKey("id")) {
                this.logger.warn("Found document in RemoteRooms collection that does not have an \"id\", skipping...");

            } else {
                var id = document.get("id", String.class);
                var room = new RemoteRoom(id, this, document);

                synchronized (this.remoteRooms) {
                    this.remoteRooms.put(id, room);
                }
            }
        }, (result, throwable) -> {
            if(throwable != null) {
                this.getBridgeLogger().error("Error while attempting to retrieve RemoteRooms from database!");
                this.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });
    }

    private void syncMatrix() {
        // First get the rooms and users from the database

        this.database.getMatrixUsers().find().forEach(document -> {
            if(!document.containsKey("id")) {
                this.logger.warn("Found document in MatrixUsers collection that does not have an \"id\", skipping...");

            } else {
                var id = document.get("id", String.class);
                var user = new MatrixUser(id, this, document);

                synchronized (this.matrixUsers) {
                    this.matrixUsers.put(id, user);
                }
            }
        }, (result, throwable) -> {
            if(throwable != null) {
                this.getBridgeLogger().error("Error while attempting to retrieve MatrixUsers from database!");
                this.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });

        this.database.getMatrixRooms().find().forEach(document -> {
            if(!document.containsKey("id")) {
                this.logger.warn("Found document in MatrixRooms collection that does not have an \"id\", skipping...");

            } else {
                var id = document.get("id", String.class);
                var room = new MatrixRoom(id, this, document);

                synchronized (this.matrixRooms) {
                    this.matrixRooms.put(id, room);
                }
            }
        }, (result, throwable) -> {
            if(throwable != null) {
                this.getBridgeLogger().error("Error while attempting to retrieve MatrixRooms from database!");
                this.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });

        // TODO: Query homeserver for data and check
    }

    public final MatrixUser getMatrixUser(String userId) {
        synchronized (this.matrixUsers) {
            return this.matrixUsers.get(userId);
        }
    }

    public final MatrixRoom getMatrixRoom(String roomId) {
        synchronized (this.matrixRooms) {
            return this.matrixRooms.get(roomId);
        }
    }

    public final RemoteUser getRemoteUser(String id) {
        synchronized (this.remoteUsers) {
            return this.remoteUsers.get(id);
        }
    }

    public final CompletableFuture addRemoteUser(RemoteUser user) {
        var future = new CompletableFuture<>();

        this.database.getRemoteUsers().insertOne(user.getDatabaseData(), (aVoid, throwable) -> {
            if(throwable != null) {
                this.getBridgeLogger().error("Failed to add RemoteUser to database.");
                this.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                synchronized (this.remoteUsers) {
                    this.remoteUsers.put(user.id, user);
                }

                future.complete(null);
            }
        });

        return future;
    }

    public final RemoteRoom getRemoteRoom(String id) {
        synchronized (this.remoteRooms) {
            return this.remoteRooms.get(id);
        }
    }

    public final CompletableFuture addRemoteRoom(RemoteRoom room) {
        var future = new CompletableFuture<>();

        this.database.getRemoteRooms().insertOne(room.getDatabaseData(), (aVoid, throwable) -> {
            if(throwable != null) {
                this.getBridgeLogger().error("Failed to add RemoteRoom to database.");
                this.getBridgeLogger().error(throwable.getClass().getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            } else {
                synchronized (this.remoteRooms) {
                    this.remoteRooms.put(room.id, room);
                }

                future.complete(null);
            }
        });

        return future;
    }

    public final void deleteRemoteRoom(RemoteRoom room, SingleResultCallback<DeleteResult> resultCallback) {
        this.database.getRemoteRooms().deleteOne(Filters.eq("id", room.id), resultCallback);
    }

    /**
     * Start the bridge and it's underlying appservice.
     */
    public final void start() {
        this.logger.info("Running " + SOFTWARE + " v" + VERSION +"...");

        // Close the database on shutdown
        this.shutdownHandler.registerShutdownTask(() -> this.database.close());

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

    public final Logger getBridgeLogger() {
        return this.logger;
    }

    public final BridgeConfig getConfig() {
        return this.config;
    }

    public final MatrixAppservice getAppservice() {
        return this.appservice;
    }

    public final EventManager getEventManager() {
        return eventManager;
    }

    public final ShutdownHandler getShutdownHandler() {
        return this.shutdownHandler;
    }

    public final BridgeDatabase getDatabase() {
        return database;
    }
}
