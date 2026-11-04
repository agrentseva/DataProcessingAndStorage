(ns prime-numbers.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [prime-numbers.core :refer [primes sieve]]))

(deftest first-primes-test
  (is (= [2 3 5 7 11 13 17 19 23 29]
         (take 10 primes))))

(deftest first-prime-test
  (is (= 2 (first primes))))

(deftest primes-are-increasing-test
  (let [numbers (take 100 primes)]
    (is (every? true?
                (map < numbers (rest numbers))))))

(deftest sieve-filters-multiples-test
  (is (= [2 3 5 7 11]
         (take 5 (sieve (iterate inc 2))))))

