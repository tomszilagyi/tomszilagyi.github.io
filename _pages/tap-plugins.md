---
layout: page
title:  "TAP-plugins"
location: main-area
category: Software
permalink: /tap-plugins/about/
---

Back in summer 2004, [TAP-plugins] started as some small-ish
experiments in host-based digital audio signal processing. I was just
beginning to discover the mysterious world of digital filters, digital
signal processing and all the underlying theories. As I was already
using Linux, I experimented with the [Ardour] DAW (which was in its
infancy at the time) and wrote some LADSPA plugins specifically with
that host in mind.

Because of the severe limitations of LADSPA format, I created a
separate, standalone application called the [Reverb Editor], proudly
dubbed *an interactive tool for room acoustics simulation*. This
allowed the user to interactively build an impulse response and
immediately listen to the results after every change. Even though the
reverberation model was simplistic (basically just a bunch of
biquadratic lowpass and first-order IIR filters), it produced good
enough results that several users reported that [TAP Reverberator] was
their favourite LADSPA reverb.<sup>[1](#f1)</sup>

Since the demand for usable mixing plugins was so
high<sup>[2](#f2)</sup> I created several other plugins, some of which
was really well received: my [Scaling Limiter] and [Tubewarmth] were
really in a class of their own. Nothing comparable existed as free
software.

The plugins became quite popular, several users thanked me publicly
and privately, and all major Linux distributions packaged the plugins.
I had a lot of fun creating a bunch of other plugins, some of them
with interesting (or wild) ideas.

Eventually I discovered the wonderful world of convolution reverbs,
which led me to create [IR], an LV2 plugin that immediately obsoleted
the Reverberator. But that is another story.

The code lives in the corresponding [git repo].

---

<b id="f1">1</b> You could export the room created with the Reverb
Editor and then use it in the Reverberator plugin within Ardour. This
was achieved by generating a source header that -- when the plugin was
recompiled -- got included into the build. All written in C. Very
messy and cumbersome, but it seemed like a good idea at the
time.

<b id="f2">2</b> Mind you, supply was next to nonexistent then. This
is still 2004 with Linux Audio in its infancy, where the prevailing
question is still: *How do I get Ardour and my multi-channel audio
hardware to reliably work with low latency?*


[TAP-plugins]:        /tap-plugins/
[Ardour]:             http://ardour.org
[Reverb Editor]:      /tap-plugins/reverbed/manual.html
[TAP Reverberator]:   /tap-plugins/ladspa/reverb.html
[Scaling Limiter]:    /tap-plugins/ladspa/limiter.html
[Tubewarmth]:         /tap-plugins/ladspa/tubewarmth.html
[IR]:                 /plugins/ir.lv2
[git repo]:           https://github.com/tomszilagyi/tap-plugins
