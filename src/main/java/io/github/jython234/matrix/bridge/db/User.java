package io.github.jython234.matrix.bridge.db;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a user between the two bridged applications. It is either
 *
 * @author jython234
 */
public class User implements Serializable {
    /**
     * The ID of the user. This can not be changed and is always final.
     */
    public final String id;

    /**
     * The type of the user.
     */
    public final Type type;

    @Getter private transient final BridgeDatabase database;

    /**
     * The user's name (or displayname). This should be the value that will be displayed when
     * messages are sent.
     */
    @Getter private String name;

    /**
     * <strong>NOTICE: Modifying this directly is not recommended!</strong> Please use the helper method
     * {@link #updateDataField(String, Serializable)} instead.
     *
     * If you do end up modifying this directly, you'll have to update the whole user in the database by
     * calling {@link BridgeDatabase#putUser(User)}, even if it already exists as it will overwrite it, inserting your new
     * changes. It's recommended to use the helper method instead.
     *
     * Any additional data to be stored in the database. This is ususally used by the actual
     * bridge implementation for specific data referring to the application being bridged.
     *
     * For example, storing profile picture information.
     *
     * @see #updateDataField(String, Serializable)
     * @see BridgeDatabase#putUser(User)
     */
    @Getter private Map<String, Serializable> additionalData;

    public User(BridgeDatabase db, Type type, String id) {
        this.database = db;
        this.type = type;
        this.id = id;
        this.additionalData = new ConcurrentHashMap<>();
    }

    /**
     * Updates this user's name to a new name.
     * @param newName The user's new name.
     * @throws DatabaseException If there is an error while updating the name. This is a {@link RuntimeException} based exception.
     */
    public synchronized void updateName(@NonNull String newName) {
        String oldName = this.name;

        this.name = newName;
        try {
            this.database.updateUsersName(this, newName);
        } catch (IOException e) {
            this.name = oldName; // Revert to old name as the database probably didn't update or save correctly.
            throw new DatabaseException(e);
        }
    }

    /**
     * Update a data field for this user.
     * @param key The key of the field.
     * @param value The value.
     */
    public synchronized void updateDataField(String key, Serializable value) {
        Serializable oldValue = this.additionalData.get(key);

        this.additionalData.put(key, value);
        try {
            this.database.updateUsersDataField(this, key, value);
        } catch (IOException e) {
            this.additionalData.put(key, oldValue); // Revert to old value as the database probably didn't update or save correctly.
            throw new DatabaseException(e);
        }
    }

    /**
     * Delete a data field for this user.
     * @param key The key of the field.
     * @return The value of the field that was removed.
     */
    public synchronized Serializable deleteDataField(String key) {
        Serializable value = this.additionalData.remove(key);

        try {
            this.database.deleteUsersDataField(this, key);
        } catch (IOException e) {
            this.additionalData.put(key, value); // Re-insert the value as the database probably didn't successfully delete the pair.
            throw new DatabaseException(e);
        }

        return value;
    }

    /**
     * Represents the type of a {@link User}. It can either be a Matrix User, in which
     * the user is on Matrix and is being bridged to the remote application, or a Remote User, in which
     * the user is on the remote application and is being bridged to Matrix.
     *
     * @author jython234
     */
    public enum Type {
        /**
         * Represents a Matrix User being bridged to the remote application.
         */
        MATRIX_USER(0),
        /**
         * Represents a Remote User being bridged to Matrix.
         */
        REMOTE_USER(1);

        /**
         * The value of this enum as an integer.
         */
        final int integerValue;

        Type(int integerValue) {
            this.integerValue = integerValue;
        }
    }
}
