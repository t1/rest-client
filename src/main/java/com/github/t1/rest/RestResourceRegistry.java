package com.github.t1.rest;

import java.util.List;

public interface RestResourceRegistry {
    /** Retrieve the resource for that alias */ 
    public RestResource get(String alias);

    /** The list of all registered aliases */
    public List<String> names();
}
