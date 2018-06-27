package io.github.jython234.matrix.bridge.db;

import lombok.Getter;

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

    /**
     * The user's name (or displayname). This should be the value that will be displayed when
     * messages are sent.
     */
    @Getter private String name;

    /**
     * Any additional data to be stored in the database. This is ususally used by the actual
     * bridge implementation for specific data referring to the application being bridged.
     *
     * For example, storing profile picture information.
     */
    @Getter private Map<String, Serializable> additionalData;

    public User(Type type, String id) {
        this.type = type;
        this.id = id;
        this.additionalData = new ConcurrentHashMap<>();
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
