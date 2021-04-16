(ns gemtext.core-test
  (:require
   [clojure.test :refer :all]
   [gemtext.core :refer :all]))

(deftest gemtext-tests
  (testing "parsing"
    ;; some simple cases
    (let [s {"# header-1"                [[:header-1 "header-1"]]
             "## header-2"               [[:header-2 "header-2"]]
             "### header-3"              [[:header-3 "header-3"]]
             "=> something else"         [[:link "something" "else"]]
             "=>something else"          [[:link "something" "else"]]
             "=>/foo"                    [[:link "/foo" ""]]
             "= >/foo"                   [[:text "= >/foo"]]
             "```descr\ncode\ncode\n```" [[:pre "descr" "code\ncode\n"]]}]
      (doseq [i s]
        (let [[str ex] i]
          (is (= (parse str) ex)))))))

(comment
  (run-all-tests)
  (run-tests)
)
