(ns user
  (:import (org.alexandrehd.persister MapPersister)))

(def m
  (let [m (java.util.HashMap.)]
    (.put m "A" 4.5)
    (.put m "B" 3.1)
    m))

(def my-saver (MapPersister. (java.io.File. "/tmp/grotty") 1))

;; No support yet for boxed numericals:
;;(.put m "A" 12)
;;(.put m "B" 6.7)

(.persist my-saver m)

(.unpersist my-saver)
