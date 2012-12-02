# Dog Fort

![Dog Fort](https://raw.github.com/bodil/dogfort/master/dogfort.jpg)

A rudimentary web server framework for ClojureScript on Node.js,
inspired by [Ring](https://github.com/mmcgrana/ring).

This is *experimental code*. If you build anything on this right now,
you're being stupid.

## Usage

Dog Fort uses Ring's concept of handlers and adapters, the only
difference being that the handler should return a promise of a
response structure, not the response itself, due to the asynchronous
nature of Node. See [Red Lobster](https://github.com/bodil/redlobster)
for documentation on promises.

```clojure
    (ns user
      (:use [dogfort.http :only [run-http]])
      (:use-macros [redlobster.macros :only [promise]]))

    (defn handler [request]
      (promise {:status 200
                :headers {:content-type "text/html"}
                :body "<h1>This is Dog Fort</h1>"}))

    (run-http handler {:port 1337})
```

The body of a response can also be a Node stream. Here's an example
that serves a file directly from the file system using a Node `Stream`
object.

```clojure
    (ns user
      (:use [dogfort.http :only [run-http]])
      (:require [redlobster.stream :as stream])
      (:use-macros [redlobster.macros :only [promise]]))

    (defn handler [request]
      (promise {:status 200
                :headers {:content-type "text/plain"}
                :body (stream/slurp "README.md")}))

    (run-http handler {:port 1337})
```

## Routing

Dog Fort includes a request routing mechanism heavily inspired by
[Compojure](https://github.com/weavejester/compojure). It introduces
the `defroutes` macro for building handlers with routing.

```clojure
    (ns user
      (:use [dogfort.http :only [run-http]])
      (:require [dogfort.middleware.routing])
      (:use-macros [dogfort.middleware.routing-macros :only [defroutes GET]]))

    (defroutes app
      (GET "/hello/:name" [name]
        ["<h1>Hello " name "!</h1>"]))

    (run-http app {:port 1337})
```

The `defroutes` macro takes a symbol name, and a series of sub-handler
definitions, which are created using the `GET` macro and its
corresponding macros for other request methods: `POST`, `HEAD`, etc.

This macro takes a path expression in the Rails style, a vector of
variable bindings that should match the variables used in the path
expression, and a series of forms constituting the handler's body, and
should return a response as usual.

Notice, however, that routing sub-handlers don't need to return a
promise. For convenience, you can also return a response map directly,
or dispense with the map altogether and just return the response body,
either as a string, a sequence or a Node `Stream` object. The routing
middleware will automatically wrap it as appropriate, defaulting to a
`Content-Type` of `text/html` if you only provide the body. Note that
if you need to perform asynchronous calls, you will still have to
return a promise and realise it to a response map as usual.

# License

Copyright 2012 Bodil Stokke

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
