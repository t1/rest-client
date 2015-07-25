# REST Client

There are many http clients out there. This yet another one.

Internally, it uses Apache http-client.

## It's boring

You don't have to think about thrilling details, and don't have to write much code. Boring.

## It's incomplete

It only supports GET requests, yet. Admittedly the most common use-case, but not usable for most real-world applications. And it's just synchronous.

## It's slow

I haven't measured performance or memory consumption. That alone suggests it's not fast. It's quite a thin layer, but still a layer: It can't be any faster than the Apache http-client it's based on. And when it comes to big objects and streaming, it's even much slower and consumes much more memory, as the body is always buffered.

## It's not production-ready

It's still 1.0.0-SNAPSHOT. The API can change in incompatible ways any time. Any commit can be completely broken. You better not rely on it.

# Principles

And it's based on the very same principles as the client api of JAX-RS 2.0, but a little bit easier to use. Most notably, you don't have to specify the content type you can accept; this is derived from the available MessageBodyReaders for the type you want to get.

## Build forward, link backward

It's immutable, fluent, and strictly appendable:

* Immutable: You can derive new values from old without modifying the old.
* Fluent: You can chain the parts you need to set, so the code can be read easily.
* Strictly appendable: In addition to the immutability, things set can't be taken back or overwritten. Once you've set, e.g., a query parameter, you can't remove it or even change the path any more. This restriction enforces a cleaner code style, so you don't have to look around where this or that may get set.
