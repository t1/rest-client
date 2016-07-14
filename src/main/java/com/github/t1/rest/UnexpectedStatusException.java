package com.github.t1.rest;

import lombok.Getter;

import javax.ws.rs.core.Response.StatusType;
import java.util.List;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.*;

@Getter
public class UnexpectedStatusException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final StatusType actual;
    private final Headers headers;
    private String body;
    private final List<StatusType> expected;

    public UnexpectedStatusException(StatusType actual, Headers headers, StatusType... expected) {
        this(actual, headers, asList(expected));
    }

    public UnexpectedStatusException(StatusType actual, Headers headers, String body, StatusType... expected) {
        this(actual, headers, body, asList(expected));
    }

    public UnexpectedStatusException(StatusType actual, Headers headers, List<StatusType> expected) {
        this(actual, headers, null, expected);
    }

    public UnexpectedStatusException(StatusType actual, Headers headers, String body, List<StatusType> expected) {
        this.actual = actual;
        this.headers = headers;
        this.body = body;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "expected status " + toString(expected) + " but got [" + toString(actual) + "]\n"
                + headers.toListString() + ((body == null) ? "" : "\n" + body);
    }

    private static String toString(List<StatusType> statusTypes) {
        return statusTypes.stream().map(UnexpectedStatusException::toString).collect(joining(", ", "[", "]"));
    }

    private static String toString(StatusType status) {
        return status.getStatusCode() + " " + status.getReasonPhrase();
    }
}
