package io.github.jython234.matrix.bridge.network.registration;

public class UserRegisterRequest {
    public String type = "m.login.application_service";
    public String username;

    public UserRegisterRequest(String username) {
        this.username = username;
    }
}
