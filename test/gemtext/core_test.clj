(ns gemtext.core-test
  (:require
   [clojure.test :refer :all]
   [gemtext.core :refer :all])
  (:import
   (java.io BufferedReader StringReader)))

(deftest gemtext-tests
  (testing "can parse simple strings"
    (let [s {"# header-1"                [[:header-1 "header-1"]]
             "## header-2"               [[:header-2 "header-2"]]
             "### header-3"              [[:header-3 "header-3"]]
             "=> something else"         [[:link "something" "else"]]
             "=>something else"          [[:link "something" "else"]]
             "=>/foo"                    [[:link "/foo" ""]]
             "= >/foo"                   [[:text "= >/foo"]]
             "```descr\ncode\ncode\n```" [[:pre "descr" "code\ncode\n"]]
             "* item\n* item 2"          [[:item "item"] [:item "item 2"]]}]
      (doseq [i s]
        (let [[str ex] i]
          (is (= (parse str) ex))))))

  (testing "can parse sequences of strings"
    (is (= (parse (list "=> /1" "=> /2" "=> /3"))
           [[:link "/1" ""] [:link "/2" ""] [:link "/3" ""]])))

  (testing "can parse java readers"
    (is (= (parse (StringReader. "> something clever"))
           [[:quote "something clever"]])))

  (testing "can parse from buffered reades"
    (is (= (parse (BufferedReader. (StringReader. "# hello")))
           [[:header-1 "hello"]]))))

(comment
  (run-all-tests)
  (run-tests)
)
