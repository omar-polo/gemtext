# gemtext

[![Clojars Project](https://img.shields.io/clojars/v/com.omarpolo/gemtext.svg)](https://clojars.org/com.omarpolo/gemtext)

A Clojure library to parse text/gemini.  It uses a format similar to
hiccup to represent documents, and provides a function to convert
gemtext' hiccup to "usual" HTML-hiccup.

## Usage

```clojure
(require '[gemtext.core :as gemtext])
```

Follows the documentation for the exported functions.

`parser` is a transducer that takes text/gemini lines and produces
hiccup.  It's useful if you already have a pipeline and/or want to
parse a (possibly infinite) stream of text/gemini.

```clojure
user=> (transduce gemtext/parser conj [] '("# hello world"))
[[:header-1 "hello world"]]
```

`parse` is function that parses the given *thing*.  It's a
multimethod, and default implementations are given for strings,
sequences (of string) and java readers.

```clojure
user=> (gemtext/parse "some\nlines\nof\ntext")
[[:text "some"] [:text "lines"] [:text "of"] [:text "text"]]

user=> (gemtext/parse (repeat 3 "* test"))
[[:item "test"] [:item "test"] [:item "test"]]
```

`unparse` is the complement of `parse`: transforms a hiccup data
structure back to a string.

```clojure
user=> (gemtext/unparse [[:link "/foo" "A link"]])
"=> /foo A link\n"
```

`to-hiccup` converts gemtext hiccup to "classical" (i.e. HTML) hiccup,
so it's easier to integrate with other libraries, or to further
convert to HTML.

```clojure
user=> (gemtext/to-hiccup [[:header-1 "text/gemini"] [:text "..."]])
[[:h1 "text/gemini"] [:p "..."]]
```

## Caveats

`(comp gemtext/unparse gemtext/parse)` is not an identity function,
some bits of information are lost during the parsing; however the
output is fundamentally equivalent to the input:

```clojure
user=> ((comp gemtext/unparse gemtext/parse) "#hello world")
"# hello world\n"
```

## Development

Run the tests with

	./bin/kaocha

## License

Copyright © 2021 Omar Polo, all rights reserved.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
