---
layout: post
title:  "Measured: Typing latency of Zutty (compared to others)"
---

This is a follow-up post to my [previous article] comparing [Zutty] to
various other X terminal emulators. That assessment, while going into
a lot of detail, missed an important aspect: the typing latency of
each program.

On the most basic level, we are talking about the delay it takes for
an interactive program (such as a terminal emulator) to appropriately
respond to an input event, in this case by altering its screen output
in response to the user pressing a key. This is quite fundamental for
usability: high latencies tend to affect the user negatively, as the
constant lag between user action and perceptional feedback acts as a
(perhaps unregistered) mental burden.
[This article](https://pavelfatin.com/typing-with-pleasure/) by Pavel
Fatin argues for the importance of sufficiently low latencies much
better than I could, so I encourage you to check it out.

Clearly, typing latency is an essential metric to consider when
evaluating any terminal emulator, and since I had the recent pleasure
of inflicting Zutty on a completely unassuming world of innocent Linux
users, it is only natural that I also benchmark it to demonstrate how
it fits in with other, more established programs. If you are not
familiar with my [previous article], I encourage you to check it out,
as it contains a summary of all programs in my test that I will not
repeat here. In short: we will be looking at seven programs in total,
tested on the same hardware setup as documented there. The only
difference here is that I am testing a slightly newer revision of
Zutty (tagged as
[Release 0.7](https://github.com/tomszilagyi/zutty/releases/tag/0.7))
with some fixes and enhancements that did not yet exist at the time
of my last article.

To measure the typing latency of each program, I used Pavel Fatin's
excellent [typometer]. This program measures end-to-end latency: it
emits keypress events and captures the screen to note when it gets
updated (as actually visible to the user), and this is clearly the
right thing to do (check out Pavel's cool article with lots of detail
on various components of latency).

The default measurement settings were used (200 chars with 150 ms
delay, using native API) with the exception that I also enabled async
mode. I did this to decouple the periodicity of typing from the
display lag, and also because in my experience it produced less noisy
results. To measure only essential latency induced by the programs
themselves, I ran each of them full-screen (without any window
decorations, their client area being equivalent to the root window)
for the time of the measurement in a non-compositing, lightweight
tiling window manager (i3). The screen resolution was 1920x1080 and
the LCD refresh rate 60 Hz. The machine was not running anything other
than the programs involved in the measurement.

Results:

![](/files/zutty/latency/latency.png)

Graph data in tabular format (all numbers are milliseconds):

program | minimum | 1st quantile | median | mean | 3rd quantile | maximum
:--- | ---: | ---: | ---: | ---: | ---: | ---:
xterm | 1.04 | 1.74 | 1.96 | 2.21 | 2.68 | 8.14
Zutty | 5.5 | 6.22 | 6.42 | 6.51 | 6.72 | 10.58
Kitty | 7.92 | 8.90 | 8.99 | 8.96 | 9.07 | 13.8
Alacritty | 8.54 | 13.51 | 17.11 | 17.48 | 21.87 | 26.43
urxvt | 18.01 | 18.88 | 19.32 | 19.28 | 19.66 | 23.21
st | 19.80 | 20.11 | 20.29 | 20.36 | 20.55 | 22.39
gnome-terminal | 26.78 | 27.71 | 27.95 | 27.98 | 28.18 | 32.60

<br>


# Obvious conclusions

The figures pretty much speak for themselves, so I will leave it up to
you to draw your conclusions. Some of my own points though:

- xterm does direct rendering via Xlib, which is the primary reason it
  can be so fast in responding to user input. This is also the reason
  why its throughput is, on the other hand, rather poor.

- Kitty seems to deliver pretty low latency on a very consistent
  basis, which is a non-trivial fact given that it is GPU-based. It is
  almost as responsive as Zutty. In fact, I am not sure whether anyone
  would be able to tell them apart in a blind test. [Update: Kitty's
  author informs me that in order to achieve the lowest possible
  latency with Kitty, its configuration option `input_delay` should be
  set to 0 (the default value is 3). I found this to improve Kitty's
  overall numbers by about 2 milliseconds, putting it squarely in the
  same spot (within measurement error) as Zutty.]

- Alacritty is surprisingly slow here and worse, its performance
  exhibits great variation, which is also bad for user experience.
  This seems to be directly tied in with the program's design; a
  variety of tickets in Alacritty's issue tracker demonstrate that its
  developers are well aware of the problem (but there is no easy
  solution).

- Speaking of the remaining terminal emulators, there is clearly room for
  improvement.


[Zutty]:                   /zutty
[previous article]:        /2020/12/A-totally-biased-comparison-of-Zutty
[typometer]:               https://github.com/pavelfatin/typometer
