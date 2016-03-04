---
layout: post
title:  "Bitwise operations in Common Lisp"
---

One could argue it is highly suspect to do low-level bit-twiddling
operations in a language such as Common Lisp. But as hairy as that may
sound, it is actually very doable in a high level language with
arbitrary precision arithmetics. And since Common Lisp is one of my
favourite languages, I tend to consider using it for almost *any* kind
of open-ended, experimental programming.

As it happens, I just had the need to implement some of these
operations. Once I got them working, I spotted that they are missing
from the corresponding page on [Rosetta Code], so I decided to publish
them. They have been tested pretty heavily, so I'm quite confident
they are correct.

Below is a copy of the material I submitted [here].

---

Left and right logical shift may be implemented by the following functions:

{% highlight Clojure %}
(defun shl (x width bits)
  "Compute bitwise left shift of x by 'bits' bits, represented on 'width' bits"
  (logand (ash x bits)
          (1- (ash 1 width))))

(defun shr (x width bits)
  "Compute bitwise right shift of x by 'bits' bits, represented on 'width' bits"
  (logand (ash x (- bits))
          (1- (ash 1 width))))
{% endhighlight %}

Left and right rotation may be implemented by the following functions:

{% highlight Clojure %}
(defun rotl (x width bits)
  "Compute bitwise left rotation of x by 'bits' bits, represented on 'width' bits"
  (logior (logand (ash x (mod bits width))
                  (1- (ash 1 width)))
          (logand (ash x (- (- width (mod bits width))))
                  (1- (ash 1 width)))))

(defun rotr (x width bits)
  "Compute bitwise right rotation of x by 'bits' bits, represented on 'width' bits"
  (logior (logand (ash x (- (mod bits width)))
                  (1- (ash 1 width)))
          (logand (ash x (- width (mod bits width)))
                  (1- (ash 1 width)))))
{% endhighlight %}

[Rosetta Code]: http://rosettacode.org
[here]: http://rosettacode.org/wiki/Bitwise_operations#Common_Lisp
