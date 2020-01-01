;;; Example code from the article:
;;;   https://github.tomszilagyi.io/2018/08/Custom-transactions-in-banks2ledger
;;;
;;; This code is placed in the public domain.

(ns banks2ledger.core)

(defn round [x]
  (double (Math/round x)))

;; Customize salary income transactions

(defn salary-hook-predicate [entry]
  (some #{"LÖN"} (tokenize (:descr entry))))

(defn simple-salary-hook-formatter [entry]
  (let [amount (:amount entry)
        currency (:currency entry)
        year (subs (:date entry) 0 4)
        verifs [{:comment "Pay stub data"}
                {:account (format "Tax:%s:GrossIncome" year)
                 :amount "-00,000.00"
                 :currency currency}
                {:account (format "Tax:%s:IncomeTax" year)
                 :amount "0,000.00"
                 :currency currency}
                {:account (format "Tax:%s:NetIncome" year)}
                {:comment "Distribution of net income"}
                {:account (:account entry)
                 :amount amount
                 :currency currency}
                {:account "Income:Salary"
                 :amount (str "-" amount)
                 :currency currency}]]
    (print-ledger-entry (conj entry [:verifs verifs]))))

(defn advanced-salary-hook-formatter [entry]
  (let [gross-salary 38500.0
        spp-contrib (round (* 0.05 gross-salary))
        recv-amount (amount-value (:amount entry))
        net-salary (+ recv-amount spp-contrib)
        income-tax (- gross-salary net-salary)
        currency (:currency entry)
        year (subs (:date entry) 0 4)
        verifs [{:comment "Pay stub data"}
                {:account (format "Tax:%s:GrossIncome" year)
                 :amount (format-value (- gross-salary))
                 :currency currency}
                {:account (format "Tax:%s:IncomeTax" year)
                 :amount (format-value income-tax)
                 :currency currency}
                {:account (format "Tax:%s:NetIncome" year)}
                {:comment "Distribution of net income"}
                {:account "Income:Salary"
                 :amount (format-value (- net-salary))
                 :currency currency}
                {:account "Equity:SPP:Collect"
                 :amount (format-value spp-contrib)
                 :currency currency}
                {:account (:account entry)
                 :amount (:amount entry)
                 :currency currency}]]
    (print-ledger-entry (conj entry [:verifs verifs]))))

(add-entry-hook {:predicate salary-hook-predicate
                 :formatter advanced-salary-hook-formatter})
                            ;; ... or use the simple variant above


;; Discard matching transactions

(defn ignore-hook-predicate [entry]
  (= (:counter-acc entry) "Assets:Bank:Other"))

(defn commented-entry-formatter [entry]
  (let [orig-out
        (with-out-str
          (print-ledger-entry (add-default-verifications entry)))]
    (print (clojure.string/replace orig-out #"([^\n]+\n)" "# $1"))))

(add-entry-hook {:predicate ignore-hook-predicate
                 :formatter commented-entry-formatter})
                            ;; ... or nil to silently discard entry
