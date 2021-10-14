(ns gemtext.core-test
  (:require
   [clojure.test :refer :all]
   [gemtext.core :refer :all])
  (:import
   (java.io BufferedReader StringReader)))

(def str->gmi-examples
  {"# header-1\n"                [[:header-1 "header-1"]]
   "## header-2\n"               [[:header-2 "header-2"]]
   "### header-3\n"              [[:header-3 "header-3"]]
   "=> something else\n"         [[:link "something" "else"]]
   "=> /foo\n"                   [[:link "/foo" ""]]
   "= >/foo\n"                   [[:text "= >/foo"]]
   "```descr\ncode\ncode\n```\n" [[:pre "descr" "code\ncode\n"]]
   "*foo\n* item\n"              [[:text "*foo"] [:item "item"]]
   "* item\n* item 2\n"          [[:item "item"] [:item "item 2"]]})

(deftest gemtext-tests
  (testing "can parse simple strings"
    (doseq [i str->gmi-examples]
      (let [[str ex] i]
        (is (= (parse str) ex)))))

  (testing "space is not required after the markers"
    (let [s {"#h"           [[:header-1 "h"]]
             "##h"          [[:header-2 "h"]]
             "###h"         [[:header-3 "h"]]
             ">quote"       [[:quote "quote"]]
             "=>/link text" [[:link "/link" "text"]]}]
      (doseq [i s]
        (let [[str ex] i]
          (is (= (parse str) ex))))))

  (testing "space is required after the item marker apparently"
    (is (= (parse "*foo") [[:text "*foo"]]))
    (is (= (parse "* item") [[:item "item"]])))

  (testing "doesn't get fooled by text lines that looks like other types"
    (let [s {" # header?"       [[:text " # header?"]]
             "= > broken link?" [[:text "= > broken link?"]]
             " * item?"         [[:text " * item?"]]
             " >wannabe quote?" [[:text " >wannabe quote?"]]}]
      (doseq [i s]
        (let [[str ex] i]
          (is (= (parse str) ex))))))

  (testing "doesn't mangle pre sections"
    (is (= (parse "```sh\n# a comment\necho\n```")
           [[:pre "sh" "# a comment\necho\n"]])))

  (testing "can parse sequences of strings"
    (is (= (parse (list "=> /1" "=> /2" "=> /3"))
           [[:link "/1" ""] [:link "/2" ""] [:link "/3" ""]])))

  (testing "can parse java readers"
    (is (= (parse (StringReader. "> something clever"))
           [[:quote "something clever"]])))

  (testing "can parse from buffered reades"
    (is (= (parse (BufferedReader. (StringReader. "# hello")))
           [[:header-1 "hello"]])))

  (testing "can unparse"
    (let [s {[[:header-1 "title"] [:text ""]] "# title\n\n"
             [[:quote "something clever"]]    "> something clever\n"
             [(repeat 3 [:item "n"])]         "* n\n* n\n* n\n"}]
      (doseq [i s]
        (let [[h ex] i]
          (is (= (unparse h) ex))))))

  (testing "(parse ∘ unparse) sometimes can be identity"
    (doseq [i str->gmi-examples]
      (let [[str _] i]
        (is (= str ((comp unparse parse) str)))))))

(comment
  (run-all-tests)
  (run-tests)
)
