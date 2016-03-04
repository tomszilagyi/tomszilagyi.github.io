%% This code is placed in the Public Domain.

-module(gray).
-author("Tom Szilagyi <tomszilagyi@gmail.com>").
-export([bin2gray/1, gray2bin/1, table/1, test/1]).

bin2gray(N) -> N bsr 1 bxor N.

gray2bin(G) -> gray2bin(G, log2(G)).

gray2bin(G, 0) -> G bsr 1 bxor G;
gray2bin(G, K) -> gray2bin(G bsr (1 bsl K) bxor G, K-1).

log2(G) -> log2(G, 0).

log2(G, K) when 1 bsl K >= G -> K;
log2(G, K) -> log2(G, K+1).

table(Bits) ->
    [ io:format("~*.2.0B | ~*.2.0B~n", [Bits, N, Bits, bin2gray(N)]) ||
        N <- seq(Bits) ],
    ok.

test(Bits) ->
    true = lists:all(fun(N) -> gray2bin(bin2gray(N)) =:= N end,
                     seq(Bits)),
    true = lists:all(fun({A, B}) -> gray2bin(A) =/= gray2bin(B) end,
                     [{A, B} || A <- seq(Bits), B <- seq(Bits), A /= B]).

seq(Bits) -> lists:seq(0, 1 bsl Bits - 1).
