package io.github.jython234.matrix.bridge.network;

/**
 * Represents a payload sent over HTTP.
 *
 * @author jython234
 */
public class HTTPPayload {
    /**
     * The actual payload content.
     */
    public final String body;
    /**
     * The Content-Type of the payload.
     */
    public final String contentType;

    public HTTPPayload(String body, String contentType) {
        this.body = body;
        this.contentType = contentType;
    }
}
