(ns gemtext.core
  (:require
   [clojure.string :as str]
   [clojure.walk :as walk])
  (:import
   (java.io BufferedReader Reader)))

(defn- line-type [s]
  (condp #(str/starts-with? %2 %1) s
    "###" [:header-3 (subs s 3)]
    "##"  [:header-2 (subs s 2)]
    "#"   [:header-1 (subs s 1)]
    "*"   [:item     (subs s 1)]
    ">"   [:quote    (subs s 1)]
    "=>"  [:link     (subs s 2)]
    "```" [:toggle   (subs s 3)]
    [:text s]))

(defn- fix [type l]
  (case type
    :link (let [spaces?   #{\space \tab}
                nonblank? (complement spaces?)]
            (->> (seq l)
                 (drop-while spaces?)
                 (split-with nonblank?)
                 (apply #(vector :link
                                 (apply str %1)
                                 (apply str (drop-while spaces? %2))))))
    :text [:text l]
    [type (str/triml l)]))

(defn parser
  "Transducer to parse text/gemini into something hiccup-like.
  Produces items in the from [:type string] or [:type attr string]."
  [rf]
  (let [acc   (volatile! [])
        flush #(let [a @acc]            ; sometimes I wish clojure
                 (vreset! acc [])       ; had a prog1 macro
                 [:pre (first a) (apply str (rest a))])]
    (fn
      ([] (rf))
      ([res] (if-not (empty? @acc)
               (rf res (flush))
               (rf res)))
      ([res i]
       (let [[type line]   (line-type i)
             toggle?       (= type :toggle)
             accumulating? (not (empty? @acc))]
         (cond
           (and toggle? accumulating?) (rf res (flush))
           (or toggle? accumulating?)  (do (vswap! acc conj (if accumulating?
                                                              i
                                                              line))
                                           (when accumulating?
                                             (vswap! acc conj "\n"))
                                           res)
           :else                       (rf res (fix type line))))))))

#?(:clj (do
          (defmulti parse
            "Parse text/gemini."
            {:arglists '([x])}
            type)

          (defmethod parse clojure.lang.Sequential [s]
            (transduce parser conj [] s))

          (defmethod parse String [s]
            (parse (str/split-lines s)))

          (defmethod parse BufferedReader [r]
            (parse (line-seq r)))

          (defmethod parse Reader [r]
            (parse (BufferedReader. r))))

   :cljs (do
           (defmulti parse
             "Parse text/gemini."
             {:arglists '([x])}
             type)

           (defmethod parse cljs.core/PersistentVector [s]
             (transduce parser conj [] s))

           (defmethod parse cljs.core/List [l]
             (transduce parser conj [] l))

           (defmethod parse js/String [s]
             (parse (str/split-lines s)))))

(defn- unparse1 [[type a b]]
  (case type
    :pre      (str "```" (when b a) "\n" (or b a) "```")
    :header-1 (str "# " a)
    :header-2 (str "## " a)
    :header-3 (str "### " a)
    :item     (str "* " a)
    :quote    (str "> " a)
    :link     (if (and b (not= b ""))
                (str "=> " a " " b)
                (str "=> " a))
    :text     a
    (throw (ex-info "invalid gemtext-hiccup type" {:type type
                                                   :params [a b]}))))

(defn unparse
  "Unparse into a text/gemini string."
  [x]
  (let [acc (volatile! [])]
    (walk/prewalk
     (fn [t]
       (cond (nil? t)         nil
             (or (seq? t)
                 (vector? t)) (if-not (keyword? (first t))
                                t
                                (do
                                  (vswap! acc conj (unparse1 t))
                                  (vswap! acc conj "\n")
                                  nil))))
     x)
    (apply str @acc)))

(defn- html-escape [str]
  (str/escape str {\< "&lt;"
                   \> "&gt;"
                   \& "&amp;"}))

(defn- flat [doc]
  (let [v (volatile! [])]
    (walk/prewalk (fn [t]
                    (cond (nil? t) nil
                          (or (seq? t) (vector? t))
                          (if-not (keyword? (first t))
                            t
                            (do (vswap! v conj t)
                                nil))))
                  doc)
    @v))

(defn- to-hiccup1 [[type a b]]
  (case type
    :pre      [:pre {:data-descr a} (html-escape b)]
    :header-1 [:h1 (html-escape a)]
    :header-2 [:h2 (html-escape a)]
    :header-3 [:h3 (html-escape a)]
    :quote    [:blockquote (html-escape a)]
    :link     [:a {:href a} (html-escape b)]
    :text     [:p a]
    (throw (ex-info "invalid gemtext-hiccup type" {:type type
                                                   :params [a b]}))))

(defn- to-hiccup2 [s]
  (if (= :item
         (-> s first first))
    (list (apply vector :ul (map (fn [[_ s]]
                                   [:li (html-escape s)])
                                 s)))
    (map to-hiccup1 s)))

(defn to-hiccup
  "Convert parsed gemtext into hiccup-like HTML."
  [t]
  (->> (flat t)
       (partition-by #(= (first %) :item))
       (map to-hiccup2)
       (mapcat identity)))

(comment
  (->> "list:\n* uno\n*due\n* tre\naltro\n*quattro"
       parse
       flat
       (partition-by #(= (first %) :item))
       (map to-hiccup2)
       (mapcat identity)
       )

  (to-hiccup
   (parse
    "list:\n* uno\n*due\n* tre\naltro\n*quattro\n=> /foo.html bar")
   
   )

  (flat (list (parse "hello world\n* item\n\n# title\n\n```\nhello\n````")
              (parse "hello\n* hee\n")))
  (parse '("hello" "world" "how"))
  (unparse (parse "```\nhello\nfooo\n```"))
  (unparse1 [:text "foo"])
  (parse "=> /foo hello there\n* hello\n*there\n=>/foo/bar bazzz!!\n")

  (parse (repeat 3 "hello"))
  (unparse [[:link "/index.gmi" "Home"] [:text "foo"]])

  ((comp unparse parse) "#hello world")
  )
