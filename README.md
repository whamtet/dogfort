# Dog Fort

![Dog Fort](https://raw.github.com/bodil/dogfort/master/dogfort.jpg)

A rudimentary web server framework for ClojureScript on Node.js,
inspired by [Ring](https://github.com/mmcgrana/ring).

This is *experimental code*. If you build anything on this right now,
you're being stupid.

# Red Lobster

A toolkit for working asynchronously on Node in ClojureScript, which
sits at the heart of Dog Fort. Wraps Node's `EventEmitter` and
`Stream` types, and provides some useful abstractions; in particular,
promises.

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

There's also a macro that helps you write async code to realise a promise:

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
      #(print %)
      #(print "Error reading file!"))
```

# License

Copyright © 2012 Bodil Stokke

Distributed under the Eclipse Public License, the same as Clojure.
