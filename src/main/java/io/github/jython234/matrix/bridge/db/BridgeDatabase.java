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
package io.github.jython234.matrix.bridge.db;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

/**
 * Interface which represents a supported database (Mongo or leveldb)
 *
 * @author jython234
 */
public abstract class BridgeDatabase implements Closeable {

    /**
     * Checks if a user exists based on their ID. This ID is the same ID used in a bot matrix
     * user's localpart, or a normal matrix user's localpart. When writing your bridge, the ID
     * should be from/or generated from the bridged application user somehow.
     *
     * @param id The user's ID.
     * @return If the user exists or not.
     */
    public abstract boolean userExists(String id);

    /**
     * Checks if a user exists based on their ID.
     * @param user The user.
     * @return If the user exists or not.
     * @see #userExists(String)
     */
    public boolean userExists(User user) {
        return userExists(user.id);
    }

    /**
     * Puts a user into the database, overwriting one if it already exists. The User's
     * {@link User#id} is used as the key in the database.
     * @param user The User to be inserted.
     * @throws IOException If there is an error while attempting to insert the user into the database.
     */
    public abstract void putUser(User user) throws IOException;

    /**
     * Get a user from the database, returning <code>null</code> if it doesn't exist.
     * @param id The ID of the user.
     * @return The User if found, null if not.
     * @throws IOException If there was an error while attempting to get the user from the database.
     */
    public abstract User getUser(String id) throws IOException;

    /**
     * Deletes a user from the database, based on their id.
     * @param id The ID of the user to be deleted.
     * @throws IOException If there is an error while deleting the user.
     */
    public abstract void deleteUser(String id) throws IOException;

    /**
     * Deletes a user from the database, based on their {@link User#id}.
     * @param user The {@link User} to be deleted.
     * @throws IOException If there is an error while deleting the user.
     */
    public void deleteUser(User user) throws IOException {
        deleteUser(user.id);
    }

    /**
     * Utility method used by {@link User#updateName(String)} to update the user's name.
     * @param user The user to perform the operation on.
     * @param name The new name of the user.
     * @throws IOException If there is an error while updating the name.
     * @see User#updateName(String)
     */
    protected abstract void updateUsersName(User user, String name) throws IOException;

    /**
     * Updates a key,value pair of the user's additional data. Used by
     * {@link User#updateDataField(String, Serializable)}.
     * @param user The user to perform the operation on.
     * @param key The key of the data field.
     * @param value The value of the data field.
     * @throws IOException If there is an error while updating the data.
     * @see User#updateDataField(String, Serializable)
     */
    protected abstract void updateUsersDataField(User user, String key, Serializable value) throws IOException;

    /**
     * Deletes a key,value pair of the user's additional data. Used by
     * {@link User#deleteDataField(String)}.
     * @param user The user to perform the operation on.
     * @param key The key of the data field.
     * @throws IOException If there is an error while deleting the key.
     * @see User#deleteDataField(String)
     */
    protected abstract void deleteUsersDataField(User user, String key) throws IOException;
}
