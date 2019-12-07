---
layout: post
title:  "Generating custom transactions via hooks in banks2ledger"
---

This post provides tutorial coverage for a new feature I recently
added (included in release [1.2.0][b2l-release-v1.2.0]) to
[banks2ledger], my solution for converting bank account CSV files to
ledger transactions with probabilistic payment matching. You can read
a thorough introduction to **banks2ledger** in
[this article][b2l-article].

Ever since I started using **banks2ledger** over two years ago as the
workhorse that generates most of my personal accounting, one thing I
have always missed is the ability to have certain "special"
transactions output custom ledger entries. I imagined setting this up
via some kind of user-defined templating mechanism. For example, when
bookkeeping my salary income, I want to add tax-related information
(gross income, income tax deduction) to the ledger entry based on the
pay stub information provided by my employer. This means that each
month, the corresponding "vanilla" entry generated by **banks2ledger**
needs to be replaced with a hand-crafted one.

The solution to this problem is a recently implemented new feature
called *entry hooks*, so named because the hooks are invoked on each
generated ledger entry.  The hooks are written in Clojure, just like
**banks2ledger** itself.  This mechanism provides high flexibility as
all data concerning the ledger entry, as well as the full power of
Clojure (and several helper functions in the code of **banks2ledger**)
are available to the hook.

Let's see how this works in detail, in the context of the example
mentioned earlier. *(Please note that all monetary figures below are
illustrations only. I treat my actual salary as confidential
information; you should, too.)*

## Entry hooks

Each hook has two parts: a *predicate* and a *formatter*. As the names
imply, the predicate contains logic to decide whether the hook should
activate for a particular transaction entry, in which case the
formatter is invoked to generate the ledger entry output.

For each entry, **banks2ledger** will go through all registered hooks
and invoke their predicates one after another. In case a hook's
predicate returns `true` for an entry, no further hooks are probed --
that hook will be in charge of producing the entry output via its
formatter. This might be important to keep in mind if you have
multiple hooks. The logical predicates of all hooks must be disjunct;
in case there is an overlap, which hook will be chosen is undefined.

### The hook predicate

Let's consider the example mentioned above: I want a hook to activate
on the bank account transaction of my salary arriving from my
employer. This is how the "vanilla" output of that transaction looks:

    2018/07/25 (TXN123456789) LÖN
        Assets:Bank:Account               SEK 29,290.00
        Income:Salary

The transaction looks this way because **banks2ledger** has
(correctly) inferred that, for transactions appearing on the bank
account `Assets:Bank:Account`, a transaction with a description
containing the token `LÖN` ("salary" in Swedish) should be paired with
the payee `Income:Salary`.

If you know **banks2ledger**, this is nothing remarkable. The
important observation here is that we have a simple and reliable clue
to activate our hook: whenever the description of an entry contains
the token `LÖN`, we are in business. Armed with this knowledge, here
is the predicate for our hook:

{% highlight Clojure %}
(defn salary-hook-predicate [entry]
  (some #{"LÖN"} (tokenize (:descr entry))))
{% endhighlight %}

As you can see, `salary-hook-predicate` takes an entry as its
argument. It looks up the value for key `:descr` in this entry,
tokenizes that via `tokenize` and returns a boolean depending on
whether the tokens contain `"LÖN"` or not.

This is not complicated at all, but you might be wondering how you
could have possibly come up with all this on your own, and you would
be right. So here is what you need to know about the interface.

### The hook interface

Both the hook predicate and formatter functions take a single argument
that represents a ledger entry. This is a map, populated with some
"well-known" (documented here) keys and their values. The following
table shows the actual field values seen with the above example
transaction.

| Entry key      | Value                           | Example value           |
|----------------|---------------------------------|-------------------------|
| `:date`        | Date as a string                | `"2018/07/25"`          |
| `:ref`         | Reference as a string, or `nil` | `"TXN123456789"`        |
| `:descr`       | Description string              | `"LÖN"`                 |
| `:account`     | Account being processed         | `"Assets:Bank:Account"` |
| `:counter-acc` | Inferred counter-account        | `"Income:Salary"`       |
| `:amount`      | Amount as a string              | `"29,290.00"`           |
| `:currency`    | Currency of the amount          | `"SEK"`                 |

<br>
Note that the entry is produced at a point where **banks2ledger** has
already inferred the payee account (keyed `:counter-acc`), so that is
also there for you to use.

To process this entry, hook functions can call certain useful
functions defined in **banks2ledger** itself:

| Function             | Description                                     |
|----------------------|-------------------------------------------------|
| `amount-value`       | Convert a string amount to a `Double` value     |
| `format-value`       | Format a double value to an amount string       |
| `print-ledger-entry` | Print a ledger entry in standard textual form   |
| `tokenize`           | Turn a description string into a list of tokens |

<br>
The only function we used above is `tokenize`; the others are
generally more useful to formatter functions. We will see them all in
the hook formatters below.

### The hook formatter

Once there is a hook whose predicate returned `true` for a given
entry, that entry will be passed to the formatter counterpart of the
hook. It is up to the formatter to produce whatever output is desired
for the transaction that matched the hook.

The formatter is free to use whatever means to print whatever output
it finds appropriate to `*out*`. However, to ease the process of
producing canonically formatted ledger entries, it is recommended to
use the function `print-ledger-entry`. This function takes an entry
map, extended with another key, `:verifs` associated with a sequence
of so-called *verifications*, i.e., the individual lines of a ledger
transaction.  Based on this, it will print a canonically formatted
ledger entry to `*out*`. In summary, the easiest (and recommended) way
to implement a formatter is to come up with a list of verifications,
store that in the entry under the key `:verifs`, and pass the
resulting entry to `print-ledger-entry`.

So what are verifications? Each of them is a map, describing either a
line of comment or an actual line where an account is optionally
followed by amount and currency. All this is easiest understood
through a simple example, so let's take our earlier transaction (with
an added comment):

    2018/07/25 (TXN123456789) LÖN
        ; Vanilla salary transaction
        Assets:Bank:Account               SEK 29,290.00
        Income:Salary

How could we produce exactly this transaction output in our formatter?
With a formatter that, at the end, does this:

{% highlight Clojure %}
(print-ledger-entry {:date "2018/07/25"
                     :ref "TXN123456789"
                     :descr "LÖN"
                     :verifs [{:comment "Vanilla salary transaction"}
                              {:account "Assets:Bank:Account"
                               :amount "29,290.00"
                               :currency "SEK"}
                              {:account "Income:Salary"}]})
{% endhighlight %}

Of course, the `:date`, `:ref` and `:descr` is already there in the
entry received as the argument to the formatter (`:ref` is optional);
you usually do not want to touch them. You just generate a list of
verifications and store it into the entry under the key `:verifs`.
(For "vanilla" transactions, i.e., those not claimed by any installed
hook, this is done by the **banks2ledger** function
`add-default-verifications`.)

### A simple (but useful) formatter

Now we are in a position to implement a formatter actually useful
under real circumstances. Let's say that, as stated above, we want to
bookkeep tax information on our pay stub (gross salary and net
deduction). This is how we would like our generated entry to look
like:

    2018/07/25 (TXN123456789) LÖN
        ; Pay stub data
        Tax:2018:GrossIncome              SEK -00,000.00
        Tax:2018:IncomeTax                SEK 0,000.00
        Tax:2018:NetIncome
        ; Distribution of net income
        Assets:Bank:Account               SEK 29,290.00
        Income:Salary                     SEK -29,290.00

We will manually edit this and replace the zeroes with actual values
based on our monthly pay stub. That is still work to do, but it is
much less work, and thus less room for error. By having these
tax-related legs on our entry, life will be very simple when annual
tax declaration time rolls around.

Implementing this should not come as a great surprise:

{% highlight Clojure %}
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
{% endhighlight %}

Notice how the tax sub-accounts are auto-generated based on the
transaction year (computed as a substring of the date) so we don't
have to update this year by year. Also note that we could have
replaced the hardcoded payee account `"Income:Salary"` with
`(:counter-acc entry)` to use the payee account inferred by
**banks2ledger**.

### A more involved formatter

Now let's take this a step further. Let's try to compute and pre-fill
the values for the gross salary and income tax deduction in our hook,
so we have even less manual work to do. The numbers might not agree
completely with what we see on our pay stub, but we will likely only
need to perform small corrections.

To spice this up further, let's assume that we are taking part in an
employee stock purchase program ("SPP") with our employer wherein a
fixed percentage of our *gross* salary is deducted post-tax, from our
*net* salary. This deduction is collected by the company on our
behalf, used to periodically purchase company stocks for us. Let's say
that our rate of participation in this program is 5% of our gross
salary.

This is what we would like to see generated when we run
**banks2ledger** with our latest and greatest hook installed:

    2018/07/25 (TXN123456789) LÖN
        ; Pay stub data
        Tax:2018:GrossIncome              SEK -38,500.00
        Tax:2018:IncomeTax                SEK 9,210.00
        Tax:2018:NetIncome
        ; Distribution of net income
        Income:Salary                     SEK -29,290.00
        Equity:SPP:Collect                SEK 1,925.00
        Assets:Bank:Account               SEK 27,365.00

Again, the numbers we compute might not agree 100% with the reality of
our pay stub, so we might need to manually adjust them, but it's going
to be pretty close. To implement this, we need to compute backwards
from the actual amount received on our bank account. We also know our
gross salary. Hence our solution:

{% highlight Clojure %}
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
{% endhighlight %}

As you can see, now we are using some of the helper functions provided
by **banks2ledger** summarized in the above table. We are converting
back and forth between formatted amount strings and double values so
we can do computations on them. You might wonder where the `round`
function came from, since it is neither standard Clojure nor
introduced above. In fact, it is a trivial function that takes and
returns a double, rounding its argument:

{% highlight Clojure %}
(defn round [x]
  (double (Math/round x)))
{% endhighlight %}

As stated, the full power of Clojure is there to use -- writing custom
functions to use in your hook is definitely among the possibilities.

### Registering the hook

Up to this point, I have discussed the code that implements the two
counterparts of a hook, the predicate and the formatter. To actually
register a hook with **banks2ledger**, you call the `add-entry-hook`
function with a map argument that defines the hook:

{% highlight Clojure %}
(add-entry-hook {:predicate #(salary-hook-predicate %1)
                 :formatter #(advanced-salary-hook-formatter %1)})
                              ;; ... or use the simple variant above
{% endhighlight %}

The values in the map are constructed in this manner for clarity only;
you are free to put the whole implementation directly in the closure
stored for each key.  I opted for stand-alone functions as shown
above; I feel that this makes it easier to build and test the solution
piecewise.

## Ignoring transactions

If you have more than one account processed by **banks2ledger**, and
you transfer amounts between them, then you have experienced the
problem of duplicate transactions. This is only natural, since
**banks2ledger** processes one account CSV at a time and thus cannot
detect a duplicate that came from the other leg (the counter-account)
of the same transfer.

Here, too, hooks can help. Basically, for one of the accounts, you
want to set up a hook that matches transactions that are transfers
to/from your other account. This is trivial, since **banks2ledger**
most probably already identifies the correct payee account. The only
issue is, you don't actually want to generate any output from the
hook. Of course, you could create a dummy formatter that does not
actually print anything to `*out*`, but there is a better way.

When registering a hook via `add-entry-hook`, the `:formatter` may be
set to `nil` (or left out entirely, which has the exact same
effect). This will suppress output generation, essentially discarding
any transactions matched by the hook predicate.

So assuming that I have another account called `Assets:Bank:Other`,
the following hook will take care of transfers between these accounts.
In other words, processing the CSV of both accounts and concatenating
the results to the main ledger will no longer result in duplicates for
these transfers.

{% highlight Clojure %}
(defn ignore-hook-predicate [entry]
  (= (:counter-acc entry) "Assets:Bank:Other"))

(add-entry-hook {:predicate #(ignore-hook-predicate %1)
                 :formatter nil})
{% endhighlight %}

## Putting it all together

At this point you might be wondering just how all the bits and pieces
fit together.  This is actually quite simple. Just create a file to
contain the entry hooks (and any supporting functions) you write. I
save this file with the name `hooks.clj` and store it besides my
`ledger.dat` in the same git repository, as I feel that this
configuration is part of my ledger data. You might find another
arrangement that is more convenient to you.

At any rate, when calling **banks2ledger** for the account you want to
use the hooks on, just pass the full filename to **banks2ledger** via
the `-hf` (hooks file) option.

For your reference, [here][hooks.clj] you can see the full file
containing the example above.


[banks2ledger]: https://github.com/tomszilagyi/banks2ledger
[b2l-article]: /payment-matching/
[b2l-release-v1.2.0]: https://github.com/tomszilagyi/banks2ledger/releases/tag/v1.2.0
[hooks.clj]: /files/banks2ledger/hooks.clj