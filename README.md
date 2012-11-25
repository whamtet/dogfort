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
nature of Node. See below for documentation on promises.

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

# Red Lobster

Red Lobster is a toolkit for working asynchronously on Node in
ClojureScript, and is the mechanism through which Dog Fort gets things
done. It wraps Node's `EventEmitter` and `Stream` types, and provides
some useful abstractions; in particular, promises.

## Promises

A Red Lobster promise is much like a promise in Clojure, except
instead of running in its own thread, it can be realised from async
code, and you can attach event listeners to it to respond to its
realisation.

```clojure
    (ns user
      (:require [redlobster.promise :as p]))

    (def my-promise (p/promise))

    (p/on-realised my-promise
      #(print (str "promise succeeded: " %))
      #(print (str "promise failed: " %)))

    (p/realise my-promise "cheezburger")
    ;; prints "promise succeeded: cheezburger"
```

### The Short Form

There's also a `promise` macro that helps you write async code to
realise a promise. The macro returns a new promise, and takes a set of
forms that it executes immediately, and makes two functions `realise`
and `realise-error` available inside the macro's scope for realising
the promise. This lets you conveniently realise a promise through
multiple levels of callbacks.

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [promise]]))

    (def fs (js/require "fs"))

    (defn read-file [path]
      (promise
        (.readFile fs path
          (fn [err data]
            (if err
              (realise-error err)
              (realise data))))))

    (def file-promise (read-file "/etc/passwd"))
    (p/on-realised file-promise
      #(print (str "File contents:\n" %))
      #(print "Error reading file!"))
```

### Dereferencing Promises

Promises can also, obvoiusly, be dereferenced, but, unlike Clojure
promises, this doesn't block until the promise has been realised.
Notice that dereferencing doesn't distinguish between success or error
states; you'll have to use the `failed?` function to determine whether
the promise failed.

```clojure
    (def my-promise (p/promise))
    @my-promise
    ; => :redlobster.promise/not-realised

    (p/realise my-promise "like a boss")
    @my-promise
    ; => "like a boss"
```

### Chaining Promises

A promise can also be chained to another promise, either through
simply calling `realise` with a new promise as the realised value,
which will automatically realise the promise with the new promise's
value once that promise realises, or through the `waitp` macro, which
takes a promise, a success handler and an error handler, and returns a
new promise bound to the original promise, realised through the same
`realise` function the `promise` macro makes available:

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [promise waitp]]))

    (defn read-file-or-default [path]
      (let [file-promise (read-file path)]
        (waitp file-promise
          #(realise %)
          #(realise "default content"))))
```

### Waiting For Promises

There's a `when-realised` macro which lets you create a promise that
waits for a list of other promises to finish before evaluating its
body and realising the new promise with the result of the evaluation.
This is useful when waiting for a number of async operations to
finish.

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [when-realised]]))

    (let [file-promise (read-file "/etc/passwd")]
      (when-realised [file-promise]
        (.write (.-stdout js/process) @file-promise)))
    ; writes the contents of /etc/passwd to stdout.
    ; returns a promise that will realise when the code has executed.
```

The `let` + `when-realised` construction above is a common pattern, so
there's a `let-realised` macro for combining the two. The example
above would have been better written like this:

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [let-realised]]))

    (let-realised
        [file-promise (read-file "/etc/passwd")]
      (.write (.-stdout js/process) @file-promise))
```

### Waiting And Chaining

Because `when-realised` and `let-realised` return promises that are
realised to the result of evaluating their bodies, and because you can
chain promises together by realising a promise with another promise,
you can easily create multi-step promises like this:

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [let-realised]]))

    (let-realised
        [filename-promise (read-file "/tmp/filename-inside.txt")]
      (let-realised
          [file-promise (read-file @filename-promise)]
        (.write (.-stdout js/process) @file-promise)))
    ; reads a filename from /tmp/filename-inside.txt, and then
    ; reads the contents of that file, printing the result to stdout.
```

### Wrapping Node Callbacks

A very common idiom in Node is the error/result callback. A function
takes a callback as its last argument, which in turn takes two
arguments: an error argument, which will be null upon success, and a
result argument. Callbacks start with handling any non-null error, and
proceed with dealing with the result if there was no error.

```javascript
    fs.readFile("/etc/passwd", function(error, result) {
      if (error) throw error;
      console.log(result);
    });
```

When we're using promises instead of callbacks, it's generally useful
to wrap constructs like these in a promise. That's easily accomplished
by using the `defer-node` macro. For instance, it lets us rewrite the
`read-file` function from the previous examples very succinctly:

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [let-realised defer-node]]))

    (def fs (js/require "fs"))

    (defn read-file [path]
      (defer-node (.readFile fs path)))
```

You can pass a function as a second argument to `defer-node`, which
will be applied to the result of the operation prior to realising the
promise. An excellent candidate for this would be `js->clj`, but for
the sake of example, let's make the `read-file` function more shouty.

```clojure
    (ns user
      (:require [redlobster.promise :as p]
                [clojure.string :as str])
      (:use-macros [redlobster.macros :only [let-realised defer-node]]))

    (def fs (js/require "fs"))

    (defn read-file [path]
      (defer-node (.readFile fs path) str/upper-case))
```

At this point you're probably thinking, "wait, what if I use that
function to transform the result into another promise?" Of course,
that's an excellent way of chaining together Node API operations;
let's rewrite the chaining example above using this technique.

```clojure
    (ns user
      (:require [redlobster.promise :as p])
      (:use-macros [redlobster.macros :only [defer-node]]))

    (def fs (js/require "fs"))

    (defer-node (.readFile fs "/tmp/filename-inside.txt)
      (fn [result] (defer-node (.readFile fs result)
        (fn [result] (.write (.-stdout js/process) result)))))
```

# License

Copyright © 2012 Bodil Stokke

Distributed under the Eclipse Public License, the same as Clojure.
