package io.github.jython234.matrix.bridge.test;

import io.github.jython234.matrix.appservice.Util;
import io.github.jython234.matrix.appservice.event.TypingMatrixEvent;
import io.github.jython234.matrix.bridge.MatrixBridge;
import io.github.jython234.matrix.bridge.MatrixBridgeEventHandler;
import io.github.jython234.matrix.bridge.MatrixEventHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EventHandlerReflectionTest {

    @BeforeAll
    static void init() throws IOException {
        Util.copyResourceTo("testRegistration.yml", new File("./registration.yml"));
    }

    public static class DummyBridge extends MatrixBridge {

        private boolean success = false;

        DummyBridge(String configDirectory, String serverURL) {
            super(configDirectory, serverURL); // Superclass will search for event handler methods

            // Check to see if we have found the onTypingEvent successfully as an EventHandler method, and nothing else
            assertEquals(1, this.eventHandlers.size());
            assertTrue(this.eventHandlers.containsKey(TypingMatrixEvent.class));
            assertNotNull(this.eventHandlers.get(TypingMatrixEvent.class));
            assertEquals(1, this.eventHandlers.get(TypingMatrixEvent.class).size());
            assertEquals("onTypingEvent", this.eventHandlers.get(TypingMatrixEvent.class).get(0).getName());

            var handler = new MatrixBridgeEventHandler(this);
            var event = new TypingMatrixEvent();
            event.roomId = "test";

            handler.onMatrixEvent(event);

            // Success will be set to true if the method is successfully called
            assertTrue(success);
        }

        @Override
        protected void onStart() {

        }

        @Override
        protected void onStop() {

        }

        @MatrixEventHandler
        public void onTypingEvent(TypingMatrixEvent event) {
            // Our little dummy event handler to be found
            assertNotNull(event);
            assertEquals("test", event.roomId);

            success = true;
        }


        public void onTypingEvent2(TypingMatrixEvent event2) {
            // Another dummy event handler, except there is no annotation
            // it is EXPECTED NOT to be found
        }
    }

    @Test
    void test() {
        new DummyBridge(".", "http://localhost:8008");
    }
}
