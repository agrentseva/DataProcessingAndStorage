(ns alphabet-strings.core)

(defn extend-strings [strings alphabet]
  (reduce
    concat
    (map
      (fn [s]
        (remove
          #(= (subs % (- (count %) 2) (- (count %) 1))
              (subs % (- (count %) 1) (count %)))
          (map (fn [x] (str s x)) alphabet)))
      strings)))

(defn generate-strings [alphabet n]
  (if (<= n 0)
    '()
    (reduce
      (fn [strings step]
        (extend-strings strings alphabet))
      alphabet
      (range 1 n))))

; clojure -M -m alphabet-strings.core '("a" "b" "c")' 3
(defn -main [& args]
  (if (not= (count args) 2)
    (println "Usage: clojure -M -m alphabet-strings.core '(\"a\" \"b\" \"c\")' 3")
    (let [alphabet (read-string (first args))
          n (read-string (second args))]
      (if (and (sequential? alphabet)
               (every? string? alphabet)
               (integer? n)
               (pos? n))
        (println (generate-strings alphabet n))
        (println "Error: alphabet must be a sequence of strings and n a positive integer.")))))