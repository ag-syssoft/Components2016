(ns generator-clojure.generate_init
  (:gen-class))

;; Imports
(import GenInit)

(defn print_sudoku
  "Print a sudoku in correct form"
  [sudoku]
  (doseq [item sudoku]
    (println item)))

(defn generate_row
  [size offset]
  (loop [j 0, l (list)]
    (if (>= j size)
      (into [] (reverse l))
      (recur (+ j 1) (list* (+ (mod (+ j offset) size) 1) l)))))

(defn generate_init_sudoku
  "Generate sudoku"
  [size]
  (let [k (int(Math/sqrt size))]
    (loop [i 0, l (list)]
    (if (>= i size)
      (into [] (reverse l))
      (recur (+ i 1) (list* (generate_row size (+ (* k (mod i k)) (int(/ i k)))) l))))))

(defn swap [v i1 i2] 
   (assoc v i2 (v i1) i1 (v i2)))

(defn inner_swap
  [sudoku, size, fst, snd]
  (loop [i 0, s sudoku]
          (if (>= i size)
            s
            (recur (+ i 1) (assoc s i (swap (nth s i) (.indexOf (nth s i) fst)
                                (.indexOf (nth s i) snd)))))))

(defn swap_numbers
  "swap all numbers in sudoku"
  [sudoku]
  (let [size  (count sudoku), fst (+ (rand-int (- size 1)) 1), snd (+ (rand-int (- size 1)) 1)]
    (loop [done 0, fst (+ (rand-int (- size 1)) 1), snd (+ (rand-int (- size 1)) 1)]
      (if (= fst snd)
        (recur (+ done 0) (fst (+ (rand-int (- size 1)) 1)) (snd (+ (rand-int (- size 1)) 1)))
        (inner_swap sudoku size fst snd)))))

(defn swap_rows
  "swap some rows in sudoku"
  [sudoku]
  (let [k (int(Math/sqrt (count sudoku)))]
    (let [block (rand-int (- k 1))]
      (let [cc1 (* block k), cc2 (+ cc1 (+ (rand-int (- k 2)) 1))]
        (swap sudoku cc1 cc2)))))

(defn inner_block_swap
  [sudoku k b1 b2]
  (loop [i 0, s sudoku]
    (if (>= i k)
      s
      (recur (+ i 1) (swap s (+ (* b1 k) i) (+ (* b2 k) i))))))

(defn swap_blocks
  "swap blocks in a given sudoku"
  [sudoku]
  (let [k (int (Math/sqrt (count sudoku)))]
    (loop [b1 (rand-int (- k 1)), b2 (rand-int (- k 1))]
      (if (= b1 b2)
        (recur b1 (rand-int (- k 1)))
        (inner_block_swap sudoku k b1 b2)))))

(defn rotate_sudoku [m]
  (apply mapv vector m))

(defn randomize
  [sudoku k case]
  (let [rdm_rng (+ k (rand-int (+ k 2)))]
    (loop [j 0, s sudoku]
      (if (> j rdm_rng)
        s
        (if (= case 1)
          (recur (+ j 1)  (swap_numbers s))
          (if (= case 2)
            (recur (+ j 1) (swap_rows s))
            (if (= case 3)
              (recur (+ j 1) (swap_blocks s))
              (if (= case 4)
                (recur (+ j 1) (rotate_sudoku s))))))))))

(defn generate_filled_sudoku
  "Generate a fully filled sudoku"
  [size]
  (let [sudoku (generate_init_sudoku size), k (int (Math/sqrt size))]
    (loop [i 1, s sudoku]
      (if (> i 4)
        s
        (recur (+ i 1) (randomize s k i))))))

(println "----")
(print_sudoku (generate_filled_sudoku 9))

;; Test für generate_init_sudoku
(println "----")
(print_sudoku (generate_init_sudoku 9))

;; Test für swap_numbers
(println "----")
(def s (generate_init_sudoku 9))
(print_sudoku (swap_numbers s))

;; Test für swap_rows
(println "----")
(def s (generate_init_sudoku 9))
(print_sudoku (swap_rows s))

;; Test für swap_blocks
(println "----")
(def s (generate_init_sudoku 9))
(print_sudoku (swap_blocks s))

;; Test für rotate_sudoku
(println "----")
(def s (generate_init_sudoku 9))
(print_sudoku (rotate_sudoku s))
