(ns user
  (:import (org.alexandrehd.presetter SpilledSaver)))

(def my-saver (SpilledSaver. (java.io.File. "/tmp/grotty") 3))

(def m (java.util.HashMap.))

;; No support yet for boxed numericals:
;;(.put m "A" 12)
;;(.put m "B" 6.7)

(.put m "MyString" "Hello World")

(.persist my-saver m)
