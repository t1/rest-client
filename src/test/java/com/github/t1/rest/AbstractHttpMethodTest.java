package com.github.t1.rest;

import ch.qos.logback.classic.*;
import org.junit.Before;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static ch.qos.logback.classic.Level.*;

public class AbstractHttpMethodTest {
    @Before
    public void before() {
        setLogLevel("org.apache.http.wire", DEBUG);
        setLogLevel("com.github.t1.rest", DEBUG);
    }

    private void setLogLevel(String loggerName, Level level) {
        ((Logger) LoggerFactory.getLogger(loggerName)).setLevel(level);
    }

    @Inject
    RestContext rest;
}
