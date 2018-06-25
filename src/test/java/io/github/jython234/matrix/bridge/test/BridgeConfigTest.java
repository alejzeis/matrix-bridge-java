package io.github.jython234.matrix.bridge.test;

import io.github.jython234.matrix.appservice.exception.KeyNotFoundException;
import io.github.jython234.matrix.bridge.configuration.BridgeConfig;
import io.github.jython234.matrix.bridge.configuration.BridgeConfigLoader;
import org.iq80.leveldb.CompressionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/// This test checks to see if the bridge configuration loader works correctly.
class BridgeConfigTest {
    private static ResourceLoader loader;

    // Test Constants
    private static String serverURL = "http://localhost:8008";
    private static String publicServerURL = "https://localhost";
    private static int appservicePort = 9000;

    // LevelDB Variant
    private static String levelDbPath = "/srv/matrix-bridge/db";
    private static boolean levelDbCompression = false;
    private static int levelDbCacheSize = 250;

    // MongoDB Variant
    private static String mongoURL = "mongo://localhost";
    private static String mongoDb = "matrixTestDB";

    @BeforeAll
    static void init() throws IOException {
        loader = new DefaultResourceLoader();
    }

    private void loadTestCommon(BridgeConfig config) {
        assertEquals(serverURL, config.getServerURL());
        assertEquals(publicServerURL, config.getPublicServerURL());
        assertEquals(appservicePort, config.getAppservicePort());

        assertNotNull(config.getDbInfo());
    }

    @Test
    @DisplayName("Loads a bridge configuration that uses LevelDB")
    void loadTestLevelDB() throws IOException, KeyNotFoundException {
        var config = BridgeConfigLoader.loadFromFile(loader.getResource("testConfigLevelDB.yml").getFile());

        loadTestCommon(config);
        assertEquals(BridgeConfig.DbInfo.DbType.LEVELDB, config.getDbInfo().type);

        var levelDbInfo = (BridgeConfig.LevelDBInfo) config.getDbInfo();
        assertEquals(levelDbPath, levelDbInfo.directory);
        assertEquals(levelDbCompression ? CompressionType.SNAPPY : CompressionType.NONE, levelDbInfo.compressionType);
        assertEquals(levelDbCacheSize, levelDbInfo.cacheSize);
    }

    @Test
    @DisplayName("Loads a bridge configuration that uses MongoDB")
    void loadTestMongoDB() throws IOException, KeyNotFoundException {
        var config = BridgeConfigLoader.loadFromFile(loader.getResource("testConfigMongoDB.yml").getFile());

        loadTestCommon(config);
        assertEquals(BridgeConfig.DbInfo.DbType.MONGO, config.getDbInfo().type);

        var mongoDbInfo = (BridgeConfig.MongoDBInfo) config.getDbInfo();
        assertEquals(mongoURL, mongoDbInfo.url);
        assertEquals(mongoDb, mongoDbInfo.database);
    }
}
