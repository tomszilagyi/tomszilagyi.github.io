---
layout: post
title:  "Gray code in Erlang"
---

The [Gray code] is a fascinating contraption with a rich historical
legacy, but still useful in a number of important applications.

I was interested in how difficult it is to implement idiomatic Erlang
functions that convert to and from Gray code. It turned out to be
surprisingly easy.

Converting a binary (represented as an ordinary nonnegative integer)
to Gray code:

{% highlight Erlang %}
bin2gray(N) -> N bsr 1 bxor N.
{% endhighlight %}

Converting a number in Gray code back to its corresponding ordinal:

{% highlight Erlang %}
gray2bin(G) -> gray2bin(G, log2(G)).

gray2bin(G, 0) -> G bsr 1 bxor G;
gray2bin(G, K) -> gray2bin(G bsr (1 bsl K) bxor G, K-1).

log2(G) -> log2(G, 0).

log2(G, K) when 1 bsl K >= G -> K;
log2(G, K) -> log2(G, K+1).
{% endhighlight %}

Note that the implementations are not restricted to any number of
bits. Since Erlang supports arbitrary precision arithmetics, the
functions should work for as large numbers as constrained by available
memory.

For any bit width B, the gray codes corresponding to
0..2<sup>B</sup>-1 will be cyclic. That is, the codes for 0 and
2<sup>B</sup>-1 will also differ in exactly one bit, just like any two
adjacent codes.

To print a table of gray codes for a given bit width Bits:

{% highlight Erlang %}
table(Bits) ->
    [ io:format("~*.2.0B | ~*.2.0B~n", [Bits, N, Bits, bin2gray(N)]) ||
        N <- seq(Bits) ],
    ok.

seq(Bits) -> lists:seq(0, 1 bsl Bits - 1).
{% endhighlight %}

For Bits = 4, we get this table:

 Bin | Gray
-----|-----
0000 | 0000
0001 | 0001
0010 | 0011
0011 | 0010
0100 | 0110
0101 | 0111
0110 | 0101
0111 | 0100
1000 | 1100
1001 | 1101
1010 | 1111
1011 | 1110
1100 | 1010
1101 | 1011
1110 | 1001
1111 | 1000

---
Download complete [module] with all code.

[Gray code]: https://en.wikipedia.org/wiki/Gray_code
[module]:    /files/gray/gray.erl
