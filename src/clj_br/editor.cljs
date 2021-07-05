(ns clj-br.editor
  (:require
   [reagent.dom :as rdom]))


(defn ^:dev/after-load main []
  (rdom/render
    [:div "AEE"]
    (js/document.getElementById "codemirror")))
