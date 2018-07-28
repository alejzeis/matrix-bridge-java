package io.github.jython234.matrix.bridge.network.request.registration;

/**
 * Internal GSON Request class for registering users.
 *
 * @author jython234
 */
public class RegisterUserRequest {
    public String type = "m.login.application_service";
    public String username;

    public RegisterUserRequest(String username) {
        this.username = username;
    }
}
