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

// This test checks to make sure all event handling is working properly
class EventHandlerTest {

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
