(ns prime-numbers.core)

(defn sieve [numbers]
  (lazy-seq
    (when-let [numbers (seq numbers)]
      (let [prime (first numbers)]
        (cons prime
              (sieve
                (filter
                  #(not (zero? (mod % prime)))
                  (rest numbers))))))))

(def primes
  (sieve (iterate inc 2)))

; clojure -M -m prime-numbers.core 5
(defn -main [& args]
  (if (not= (count args) 1)
    (println "Usage: clojure -M -m prime-numbers.core 20")
    (let [n (read-string (first args))]
      (if (and (integer? n) (pos? n))
        (println (take n primes))
        (println "Error: N must be a positive integer.")))))