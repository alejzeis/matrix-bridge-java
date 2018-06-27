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

import io.github.jython234.matrix.appservice.exception.KeyNotFoundException;
import org.iq80.leveldb.CompressionType;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Class that combines methods needed to load the bridge's YAML configuration
 * file from the disk into memory, specifically load into the {@link BridgeConfig} class.
 *
 * @author jython234
 * @see BridgeConfig
 */
public class BridgeConfigLoader {

    /**
     * Load the bridge configuration from the specified YAML file location.
     *
     * @param location The location of the YAML file.
     * @return A new {@link BridgeConfig} instance containing all the YAML properties.
     * @throws FileNotFoundException If the YAML file was not found.
     * @throws KeyNotFoundException If there are missing keys or properties in the YAML file.
     */
    public static BridgeConfig loadFromFile(String location) throws FileNotFoundException, KeyNotFoundException {
        return loadFromFile(new File(location));
    }

    /**
     * Load the bridge configuration from the specified YAML file location.
     *
     * @param location The location of the YAML file.
     * @return A new {@link BridgeConfig} instance containing all the YAML properties.
     * @throws FileNotFoundException If the YAML file was not found.
     * @throws KeyNotFoundException If there are missing keys or properties in the YAML file.
     */
    public static BridgeConfig loadFromFile(File location) throws FileNotFoundException, KeyNotFoundException {
        var yaml = new Yaml();
        var config = new BridgeConfig();

        Map map = yaml.load(new FileInputStream(location));

        config.serverURL = (String) map.get("serverURL");
        config.publicServerURL = (String) map.get("publicServerURL");
        config.appservicePort = (int) map.get("appservicePort");

        if(config.serverURL == null || config.publicServerURL == null || config.appservicePort == 0) {
            throw new KeyNotFoundException("Failed to find all required keys in the YAML file!");
        }

        if(config.appservicePort < 0) {
            throw new RuntimeException("Appservice port must be positive!");
        }

        var dbInfoMap = (Map) map.get("db");
        if(dbInfoMap == null) {
            throw new KeyNotFoundException("Failed to find key: \"db\" in YAML file.");
        }

        var type = (String) dbInfoMap.get("type");
        if(type == null) throw new KeyNotFoundException("Failed to find key \"db.type\" in YAML file.");

        switch (type) {
            case "mongodb": {
                var info = new BridgeConfig.MongoDBInfo();
                info.type = BridgeConfig.DbInfo.DbType.MONGO;
                info.database = (String) dbInfoMap.get("database");
                info.url = (String) dbInfoMap.get("url");

                if (info.database == null || info.url == null) {
                    throw new RuntimeException("Failed to find required \"db\" keys in the YAML file.");
                }

                config.dbInfo = info;
                break;
            }
            case "leveldb": {
                var info = new BridgeConfig.LevelDBInfo();
                info.type = BridgeConfig.DbInfo.DbType.LEVELDB;
                info.directory = (String) dbInfoMap.get("directory");
                info.cacheSize = (int) dbInfoMap.get("cacheSize");
                info.compressionType = ((boolean) dbInfoMap.get("compression") ? CompressionType.SNAPPY : CompressionType.NONE);

                if (info.directory == null || info.cacheSize == 0) {
                    throw new KeyNotFoundException("Failed to find all required \"db\" keys in YAML file!");
                }

                if (info.cacheSize < 0) {
                    throw new RuntimeException("BridgeDatabase cache must be positive!");
                }

                config.dbInfo = info;
                break;
            }
            default:
                throw new RuntimeException("BridgeDatabase type must either be \"mongodb\" or \"leveldb\"");
        }

        return config;
    }
}
