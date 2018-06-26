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
package io.github.jython234.matrix.bridge.configuration;

import org.iq80.leveldb.CompressionType;

/**
 * Represents the bridge's YAML configuration file.
 * It is loaded using the {@link BridgeConfigLoader} class.
 *
 * @author jython234
 * @see BridgeConfigLoader
 */
public class BridgeConfig {
    /**
     * The matrix homeserver's URL. This URL is used
     * internally by the bridge to communicate with the homeserver.
     * It is never used in user-facing messages, therefore, one could use a
     * private IP address for this URL and a domain for the public facing URL.
     *
     * @see #publicServerURL
     */
    protected String serverURL;

    /**
     * The matrix homeserver's PUBLIC URL. This is used
     * when the bridge sends links or other user-facing messages.
     *
     * @see #serverURL
     */
    protected String publicServerURL;

    /**
     * The port we want the bridge to listen on.
     */
    protected int appservicePort;

    /**
     * Contains database information.
     */
    protected DbInfo dbInfo;


    /**
     * Represents the database information in the config file.
     * For each type there will be an extending class with specific
     * information pertaining to that database type.
     *
     * @author jython234
     * @see LevelDBInfo
     * @see MongoDBInfo
     */
    public static class DbInfo {
        public DbType type;

        /**
         * Represents a type of DatabaseWrapper backend to use
         */
        public enum DbType {
            LEVELDB,
            MONGO
        }
    }

    /**
     * Represents levelDB config information, such as the directory
     * containing the databse.
     *
     * @author jython234
     */
    public static class LevelDBInfo extends DbInfo {
        /**
         * The directory where the LevelDB database is stored in.
         */
        public String directory;
        /**
         * Type of compression for the database to use.
         */
        public CompressionType compressionType;
        /**
         * DatabaseWrapper memory cache size in megabytes.
         */
        public int cacheSize;
    }

    /**
     * Represents MongoDB config information, such as the URL and port.
     *
     * @author jython234
     */
    public static class MongoDBInfo extends DbInfo {
        /**
         * URL of the MongoDB database.
         */
        public String url;
        /**
         * The name of the database.
         */
        public String database;
    }

    /**
     * Get the matrix homeserver's URL.
     * @return The matrix homeserver's URL.
     */
    public String getServerURL() {
        return this.serverURL;
    }

    /**
     * Get the matrix homeserver's public URL.
     * @return The matrix homeserver's public URL.
     */
    public String getPublicServerURL() {
        return this.publicServerURL;
    }

    /**
     * Get the appservice bind port.
     * @return The appservice's bind port.
     */
    public int getAppservicePort() {
        return this.appservicePort;
    }

    /**
     * Get the database info.
     * @return DatabaseWrapper info.
     */
    public DbInfo getDbInfo() {
        return this.dbInfo;
    }
}
