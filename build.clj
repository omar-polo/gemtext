(ns build
  (:require
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as d]))

(def repo "github.com/omar-polo/gemtext.git")
(def lib 'com.omarpolo/gemtext)
(def version (format "0.1.8"))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def pom-file (format "%s/META-INF/maven/%s/pom.xml" class-dir lib))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   version
                :basis     basis
                :src-dirs  ["src"]
                :scm       {:connection          (str "scm:git:git://" repo)
                            :developerConnection (str "scm:git:ssh://git@" repo)
                            :tag                 version
                            :url                 (str "https://" repo)}})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

;; NB: set CLOJARS_USERNAME and CLOJARS_PASSWORD
(defn deploy [_]
  (when-not (or (System/getenv "CLOJARS_USERNAME")
                (System/getenv "CLOJARS_PASWORD"))
    (throw (Exception. "Missing CLOJARS_USERNAME or PASSWORD!")))
  (jar nil)
  ;; deploy both locally *and* on clojars
  (doseq [place [:local :remote]]
    (d/deploy {:artifact       jar-file
               :pom-file       pom-file
               :installer      place
               :sign-releases? true})))
