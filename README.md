# gemtext

[![Clojars
Project](https://img.shields.io/clojars/v/com.omarpolo/gemtext.svg)](https://clojars.org/com.omarpolo/gemtext)
[![cljdoc badge](https://cljdoc.org/badge/com.omarpolo/gemtext)](https://cljdoc.org/d/com.omarpolo/gemtext/CURRENT)



A Clojure library to parse text/gemini.  It uses a format similar to
hiccup to represent documents, and provides a function to convert
gemtext' hiccup to "usual" HTML-hiccup.

## Usage

```clojure
(require '[gemtext.core :as gemtext])
```

This library uses a "hiccup"-like notation, where a parsed document is
represented as a sequence of vector in the form `[tag body]` or `[tag
attr body]`.  Unlike the usual hiccup, `attr` is always a string and
not a map, since text/gemini doesn't have the concept of "attributes."
Every vector represents one line of the original input, except for the
`:pre` which has as body the whole preformatted block.

The supported tags are:

 - `:header-1` to `header-3` for the headings
 - `:item` for the item-line
 - `:quote` for the quotation line
 - `:link` for the hyperlinks facility
 - `:pre` for the preformatted blocks
 - `:text` for the plain lines

Yes, "preformatted blocks" is a bit of a stretch, since the
text/gemini specification calls them "toggle markers", but I believe
it's more intuitive to consider them as a block, even if slightly
improper.

Only `:pre` and `:link` have the two-airity array, i.e. `[:pre "descr"
"content"]` and `[:link "url" "descr"]`, all the other "tags" have
only a single body.

> Why don't re-use the same HTML tag?  i.e. :p, :h1, :li etc

text/gemini is not 1-to-1 with HTML.  For example, what HTML calls a
"list item" is not exactly a "item line" in text/gemini.  Using
different names ensures that we don't end up mistaking one kind of
document with the other.  Said that, I'm a bit tempted to rename
`:header-1` to `:h1`...

## Documentation

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
