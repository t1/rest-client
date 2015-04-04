# REST Client

Immutable, fluent, strictly appendable builder for clients of RESTful web services.

* Immutable: You can derive new values from old without modifying the old.
* Fluent: You can chain the parts you need to set, so the code can be read easily.
* Strictly appendable: Once you've set, e.g., the query parameters, you can't change the path any more. This may be a restriction in some cases, but enforces a cleaner code style, as you don't have to look around where this or that may get set.

