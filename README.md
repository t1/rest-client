# REST Client

There are many http clients out there. This one is very similar to the client api of JAX-RS 2.0, but a little bit easier to use. Most notably, you don't have to specify the content type you can accept; this is derived from the available MessageBodyReaders for the type you want to get.

It's immutable, fluent, and strictly appendable:

* Immutable: You can derive new values from old without modifying the old.
* Fluent: You can chain the parts you need to set, so the code can be read easily.
* Strictly appendable: In addition to the immutability, things set can't be taken back or overwritten. Once you've set, e.g., a query parameter, you can't remove it or even change the path any more. This restriction enforces a cleaner code style, so you don't have to look around where this or that may get set.

