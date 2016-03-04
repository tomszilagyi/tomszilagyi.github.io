---
layout: post
title:  "Optimisation of a Golay codec for serious speed and space gains"
---

The [Golay code] is a fascinating example of a simple but effective
forward error correction code. I got interested in this particular
code because it held the promise of being simple enough to fit into a
small embedded system I was working with, the Texas Instruments CC430
(a SoC containing an MSP430 &mu;C and a digital sub-1 GHz radio module).

I already went through the quite painful process of gaining intimate
knowledge of this chip while implementing the AMORES [Telemetry Radio]
software to run on it. So I got interested to see if I was able to
squeeze in some more bang for the buck -- namely, a decent ECC
algorithm. In this process I first started looking at existing, open
implementations, of which the one by [DIY Drones] (commercialised
through 3DRobotics) became my primary inspiration. However, after
thoroughly understanding the implementation, I soon discovered there
was much to be improved.

The scope of my improvements was such that the original design I found
would not even fit in my target device, not even if I threw out all
the pre-existing code (a small realtime operating system in itself).
However, after my changes, I could re-design it to fit into the
*remaining* capacity of the device (approximately 20% of the whole
memory was free at that point).

At this point I want to make it perfectly clear that I completely
re-implemented the code I found (and vastly improved upon); none of
the original made it into the product I was working on.  I also made
further modifications and improvements fit to the specific resource
profile of my target device; these changes eventually rendered my
version incompatible with the SiK implementation.

At the end, I created a [pull request] against the original SiK code
as a way of contributing my improvements back and saying "thank you"
to those who inspired me through their prior work. Andrew Tridgell was
kind to quickly merge it. I think I wrote my lengthiest commit message
ever:

    Golay codec optimisation for space and speed (esp. decoding)

    The Golay23 codec in SiK/radio could use some optimisations. An
    improved version is proposed in this commit. Changes to the upstream
    version, with observations motivating those changes, are outlined
    below.

    a) Code table width

    The tables used for encoding and decoding are 32 bits wide, which is a
    complete waste of ROM space. We are only interested in recovering the
    12-bit payload data -- we don't really care about whether errors that
    fall into the parity symbol part of the codeword get corrected or not.
    We throw that part away anyway.

    So only 11 bits of the encoder table are significant to us -- 11 bits
    is the width of the syndrome that, combined with our 12-bit payload,
    will form the 23 bits of the Golay23 codeword. For easy storage, we
    store these values in a 16 bit wide table. The table still contains
    4096 values, but requires half the storage space.

    Naturally, the decoder table also can be restricted to the meaningful
    part. In this case, the table contains the error correction lookup
    values of which 12 bits are useful -- the width of our payload we wish
    to correct. For easy storage, we store these also in a 16 bit wide
    table. The table still contains 2048 values, but requires half the
    storage space.

    b) Decoding algorithm

    That was trivial, you might say. Now comes the interesting part. The
    decoding of Golay23 codewords can be done in much less work compared
    to the existing implementation. Specifically, the whole syndrome
    calculation function is redundant. Why calculate the syndrome on the
    spot each time, when it is already stored in a table?

    Realise that the encoder table is nothing but precomputed values of
    the syndrome for all possible payload values. The encoding operation
    is in fact nothing more but a lookup to obtain the syndrome value
    corresponding to the payload at hand, and appending it to obtain the
    encoded codeword. The exact same operation is usable to obtain the
    syndrome when decoding. The difference, of course, is that the
    received payload (the part of the received codeword that *is* the
    payload) may contain some bit errors. Nevertheless, we look it up in
    the encoder table to obtain the syndrome corresponding to that
    payload, were it the real payload that was sent (and if there is no
    error, it is).

    Since the operations over the code space are linear, XORing this
    looked-up syndrome with the received parity will yield the *real*
    received syndrome. The same as the expensive syndrome calculator
    function would have yielded. That is, we traded a function call with a
    loop over individual bits for a cheap table lookup and an XOR.

    NOTES

    My earlier version of this patch shaved off a few more cycles by
    re-arranging the bytes within the 3-packs (payload) / 6-packs (encoded
    data). This version takes care to keep the pre-existing over-the-air
    data format so a radio flashed with a firmware carrying this patch
    will still be able to communicate with another endpoint that does not
    contain this patch. However, the overwhelming majority of the
    performance gain, as well as all the storage gain is still there.

    I don't have access to the real hardware so I cannot measure the
    real-time improvement these changes yield. I estimate the time
    required to run golay_decode24() to be about one third of the
    original.

    I did verify the codec implementation driving it via a host program
    (compiled with GCC on Linux) acting as a testbench: generating
    random payload, encoding, applying errors, decoding and verifying
    that the original payload was recovered. In case of interest, I
    shall be happy to supply this testbench application. The application
    now also verifies that the encoder emits the very same encoded
    6-packs for all possible 3-byte inputs that the original version
    does.

[Golay code]:       https://en.wikipedia.org/wiki/Binary_Golay_code
[Telemetry Radio]:  /tmradio/
[DIY Drones]:       https://github.com/Dronecode
[pull request]:     https://github.com/Dronecode/SiK/pull/17
