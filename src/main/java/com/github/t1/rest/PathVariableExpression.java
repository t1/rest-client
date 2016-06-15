package com.github.t1.rest;

import lombok.*;

import java.util.*;
import java.util.regex.*;

@Value
public class PathVariableExpression {
    public static List<String> variables(String string) {
        List<String> out = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{(?<name>.*?)\\}").matcher(string);
        while (matcher.find())
            out.add(matcher.group("name"));
        return out;
    }

    public static String replaceVariable(String string, String name, Object value) {
        return (string == null) ? null : string.replace("{" + name + "}", value.toString());
    }

    public static String checkNoSlashes(String path) {
        if (path.contains("/"))
            throw new IllegalArgumentException("path elements must not contain slashes: " + path);
        return path;
    }

    @NonNull String path;
    @NonNull String name;
    @NonNull Object value;

    public String resolve() { return path.replace("{" + var() + "}", val()); }

    private String var() { return hasPathVar("*" + name) ? ("*" + name) : name; }

    private String val() { return hasPathVar(name) ? checkNoSlashes(value.toString()) : value.toString(); }

    private boolean hasPathVar(String var) { return path.contains("{" + var + "}"); }
}
