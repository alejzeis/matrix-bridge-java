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
package io.github.jython234.matrix.bridge.network.room;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the power levels in a certain
 * Matrix room.
 *
 * @author jython234
 */
public class PowerLevelsData {
    /**
     * Represents the minimum level needed to ban users.
     */
    public int ban = 50;
    /**
     * Represents the minimum level needed to send message
     * events.
     */
    @SerializedName("events_default")
    public int sendMessages = 0;
    /**
     * Represents the minimum level needed to invite users.
     */
    public int invite = 0;
    /**
     * Represents the minimum level needed to kick users.
     */
    public int kick = 50;
    /**
     * Represents the minimum level needed to redact other
     * users events. If a user doesn't meet this requirement they
     * can still redact their own messages/events.
     */
    @SerializedName("redact")
    public int redactOthers = 50;
    /**
     * Represents the minimum level needed to send state events.
     */
    @SerializedName("state_default")
    public int stateDefault = 50;
    /**
     * A Map of levels for specific users. Each key is a full matrix user ID, and the value
     * is their power level for the room.
     */
    public Map<String, Integer> users = new HashMap<>();
    /**
     * A Map of levels for specific events. Each key is a matrix event type, such as "m.room.power_levels", and
     * the value is the minimum power level needed to send that event.
     */
    public Map<String, Integer> events = new HashMap<>();
    /**
     * The default power level for users in the room.
     */
    @SerializedName("users_default")
    public int usersDefault = 0;
}
