# gemtext

A Clojure library to parse text/gemini.  It uses a format similar to
hiccup to represent documents, and provides a function to convert
gemtext' hiccup to "usual" HTML-hiccup.

## Usage

```clojure
(require '[gemtext.core :as gemtext])
```

#### `parser`

`parser` is a transducer that takes text/gemini lines and produces
hiccup:

```clojure
user=> (transduce gemtext/parser conj [] '("# hello world"))
[[:header-1 "hello world"]]
```

#### `parse`

`parse` is function that parses the given *thing*.  It's a
multimethod, and default implementations are given for strings and
sequences (of string).

```clojure
user=> (gemtext/parse "some\nlines\nof\ntext")
[[:text "some"] [:text "lines"] [:text "of"] [:text "text"]]

user=> (gemtext/parse (repeat 3 "hello"))
[[:text "hello"] [:text "hello"] [:text "hello"]]
```

#### `unparse`

The complement of `parse`, given an hiccup-like data structure returns
a string:

```clojure
user=> (gemtext/unparse [[:link "/foo.gmi" "A link"]])
"=> /foo.gmi A link\n"
```

#### `to-hiccup`

`to-hiccup` converts gemtext hiccup to "classical" (i.e. HTML) hiccup:

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
