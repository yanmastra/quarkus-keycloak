package io.yanmastra.authentication.it;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestPayload {
    public String id;
    public String name;
    public String email;
    public String phone;

    @Override
    public String toString() {
        return "TestPayload{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
