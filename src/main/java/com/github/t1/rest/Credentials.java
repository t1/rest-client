package com.github.t1.rest;

import lombok.Value;

@Value
public class Credentials {
    String userName;
    String password;

    @Override
    public String toString() {
        return "credentials"; // don't expose
    }
}
