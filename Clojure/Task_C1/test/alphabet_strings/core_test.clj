(ns alphabet-strings.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [alphabet-strings.core :refer [generate-strings]]))

(deftest generate-strings-test
  (testing "Strings of length 1"
    (is (= (set '("a" "b" "c"))
           (set (generate-strings '("a" "b" "c") 1)))))

  (testing "Strings of length 2"
    (is (= (set '("ab" "ac" "ba" "bc" "ca" "cb"))
           (set (generate-strings '("a" "b" "c") 2)))))

  (testing "Strings of length 3"
    (is (= (set '("aba" "abc" "aca" "acb"
                   "bab" "bac" "bca" "bcb"
                   "cab" "cac" "cba" "cbc"))
           (set (generate-strings '("a" "b" "c") 3)))))

  (testing "Zero length"
    (is (= #{}
           (set (generate-strings '("a" "b" "c") 0))))))