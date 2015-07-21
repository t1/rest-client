package com.github.t1.rest;

import static java.util.Arrays.*;

import java.util.List;

import javax.ws.rs.core.Response.StatusType;

import lombok.Getter;

@Getter
public class UnexpectedStatusException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final StatusType actual;
    private final Headers headers;
    private final List<StatusType> expected;

    public UnexpectedStatusException(StatusType actual, Headers headers, StatusType... expected) {
        this(actual, headers, asList(expected));
    }

    public UnexpectedStatusException(StatusType actual, Headers headers, List<StatusType> expected) {
        this.actual = actual;
        this.headers = headers;
        this.expected = expected;
    }

    @Override
    public String getMessage() {
        return "expected status " + toString(expected) + " but got " + toString(actual) + "\n" + headers.toListString();
    }

    private static String toString(List<StatusType> statusTypes) {
        if (statusTypes.size() == 1)
            return toString(statusTypes.get(0));
        StringBuilder out = new StringBuilder();
        for (StatusType statusType : statusTypes) {
            out.append(out.length() == 0 ? "[" : ",");
            out.append(toString(statusType));
        }
        out.append("]");
        return out.toString();
    }

    private static String toString(StatusType status) {
        return status.getStatusCode() + " " + status.getReasonPhrase();
    }
}
