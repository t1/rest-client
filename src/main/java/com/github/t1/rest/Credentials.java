package com.github.t1.rest;

import javax.annotation.concurrent.Immutable;

import lombok.Value;

@Immutable
@Value
public class Credentials {
    String userName;
    String password;

    @Override
    public String toString() {
        return "credentials"; // don't expose
    }
}
