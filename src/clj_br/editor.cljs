(ns clj-br.editor
  (:require
   ["@codemirror/closebrackets" :refer [closeBrackets]]
   ["@codemirror/fold" :as fold]
   ["@codemirror/gutter" :refer [lineNumbers]]
   ["@codemirror/highlight" :as highlight]
   ["@codemirror/history" :refer [history historyKeymap]]
   ["@codemirror/state" :refer [EditorState]]
   ["@codemirror/view" :as view :refer [EditorView]]
   ["lezer" :as lezer]
   ["lezer-generator" :as lg]
   ["lezer-tree" :as lz-tree]
   [clojure.string :as str]
   [nextjournal.clojure-mode :as cm-clj]
   [nextjournal.clojure-mode.extensions.close-brackets :as close-brackets]
   [nextjournal.clojure-mode.extensions.formatting :as format]
   [nextjournal.clojure-mode.extensions.selection-history :as sel-history]
   [nextjournal.clojure-mode.keymap :as keymap]
   [nextjournal.clojure-mode.live-grammar :as live-grammar]
   [nextjournal.clojure-mode.node :as n]
   [nextjournal.clojure-mode.selections :as sel]
   [nextjournal.clojure-mode.test-utils :as test-utils]
   [reagent.core :as r]
   [reagent.dom :as rdom]))

(def theme
  (.theme EditorView
    (clj->js
      {".cm-content"             {:padding     "10px 0"
                                  :text-align "left"}
       ".cm-line"                {:line-height "1.2"
                                  :font-size   "14px"
                                  :font-family "var(--code-font)"}
       ".cm-matchingBracket"     {:border-bottom "1px solid var(--teal-color)"
                                  :color         "inherit"}
       ".cm-lineNumbers" {:padding "0 5px 0 1px"}})))

(defonce extensions
  #js[theme
      (history)
      highlight/defaultHighlightStyle
      (view/drawSelection)
      (lineNumbers)
      (fold/foldGutter)
      (.. EditorState -allowMultipleSelections (of true))
      (if false
        ;; use live-reloading grammar
        #js[(cm-clj/syntax live-grammar/parser)
            (.slice cm-clj/default-extensions 1)]
        cm-clj/default-extensions)
      (.of view/keymap cm-clj/complete-keymap)
      (.of view/keymap historyKeymap)])

(defn editor [source]
  (r/with-let [!view (r/atom nil)
               mount! (fn [el]
                        (when el
                          (reset! !view
                            (new EditorView
                              (clj->js
                                {:state  (test-utils/make-state #js [extensions] source)
                                 :parent el})))))]
    [:div
     [:div {:class "rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"
            :ref   mount!
            :style {:max-height 410}}]]))

(defn samples []
  (into [:<>]
    (for [source ["(comment
  (fizz-buzz 1)
  (fizz-buzz 3)
  (fizz-buzz 5)
  (fizz-buzz 15)
  (fizz-buzz 17)
  (fizz-buzz 42))
(defn fizz-buzz [n]
  (condp (fn [a b] (zero? (mod b a))) n
    15 \"fizzbuzz\"
    3  \"fizz\"
    5  \"buzz\"
    n))"]]
      [editor source])))

(defn ^:dev/after-load main []
  (rdom/render
    [samples]
    (js/document.getElementById "codemirror"))

  (.. (js/document.querySelectorAll "[clojure-mode]")
    (forEach #(when-not (.-firstElementChild %)
                (rdom/render [editor (str/trim (.-innerHTML %))] %)))))
