---
layout: post
title:  "Please use Erlang's lists:concat/1"
---

This is a public service announcement.

How many times have you written Erlang code like this:

{% highlight Erlang %}
Label = atom_to_list(node()) ++ "_" ++ integer_to_list(N) ++ "_" ++ Suffix.
{% endhighlight %}

Now is the time to stop writing such code.<sup>[1](#f1)</sup> There is
a neat function in the `lists` module (see [edoc]) that does the job
for you. Alas, it is way too easy to overlook this humble function.
This is a pity, because using it results in much cleaner code:

{% highlight Erlang %}
Label = lists:concat([node(), "_", N, "_", Suffix]).
{% endhighlight %}

As you can see, it converts atoms and integers (and even floats, even
though that might not always be the greatest idea) implicitly to
string format, and concatenates everything together into one character
list.

---

<b id="f1">1</b> You might actually want to do some grepping around
and retroactively clean up your existing code. Especially if you think
you have another 10 years in this business.

[edoc]:         http://erlang.org/doc/man/lists.html#concat-1
