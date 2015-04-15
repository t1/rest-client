/**
 * Immutable, fluent, strictly appendable client for RESTful web services.
 * <p/>
 * A REST call consists of these parts:
 * <ol>
 * <li>The (initial) URI template where the request goes to</li>
 * <li>The headers that should be sent</li>
 * <li>The body that should be sent</li>
 * <li>The method that should be sent</li>
 * <li>The type of body that is received</li>
 * <li>The chain of link rels to follow</li>
 * <li>The values to replace the templates with</li>
 * <li>The configuration used for the request (timeouts, etc.)</li>
 * </ol>
 * These parts have to be specified in this order. Only the last two are dynamic, i.e. it's a good practice to put
 * everything up to the chain of links to follow into constants.
 * <p/>
 * To build a REST call, you start with a {@link com.github.t1.rest.RestRequest RestRequest} and add part after part.
 * There are shortcuts, in case you don't need a part, i.e. you can skip adding headers and a body by calling
 * <code>get</code> directly on the uri template.
 */
package com.github.t1.rest;

