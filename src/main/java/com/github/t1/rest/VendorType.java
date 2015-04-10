package com.github.t1.rest;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;

/**
 * Annotation for a pojo that is supposed to be marshalled as a vendor type, i.e. instead of a generic content type like
 * <code>application/json</code>, a specific type like <code>application/vnd.mytype+json</code> is used. Note that the
 * value from this annotation doesn't contain the prefix and suffix, so in that example, it's only <code>mytype</code>.
 * By default, the value is the fully qualified name of the class.
 */
@Retention(RUNTIME)
public @interface VendorType {
    public static final String USE_CLASS_NAME = "###use-class-name###";

    String value() default USE_CLASS_NAME;
}
