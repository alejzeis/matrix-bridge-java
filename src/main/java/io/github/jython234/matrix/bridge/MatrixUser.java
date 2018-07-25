package io.github.jython234.matrix.bridge;

/**
 * Represents a Matrix user.
 *
 * @author jython234
 */
public class MatrixUser {
    private String id;

    public MatrixUser(String id) {
        this.id = id;
    }

    /**
     * Get this user's Matrix Id.
     * @return
     */
    public String getId() {
        return this.id;
    }
}
