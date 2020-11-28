---
layout: page
title: "IR: LV2 Convolution Reverb"
location: main-area
category: Software
permalink: /plugins/ir.lv2/
---

![Screenshot](/images/ir.lv2/sshot.png)

Author: Tom Szilagyi  
License: GNU GPL v2  
Format: LV2 audio processing plugin  
Homepage: <https://tomscii.sig7.se/plugins/ir.lv2>  
Source: <https://github.com/tomszilagyi/ir.lv2>


IR is a no-latency/low-latency, realtime, high performance signal
convolver especially for creating reverb effects. Supports impulse
responses with 1, 2 or 4 channels, in any soundfile format supported
by libsndfile.

Developed by the author for their own needs, this plugin is released
to the public in the hope that it will be useful to others, but
strictly on an AS IS basis. There is ABSOLUTELY NO WARRANTY and there
is NO SUPPORT. High quality bug reports and well-tested patches are
nevertheless welcome. Other correspondence may be mercilessly ignored;
thank you for your understanding.


Features
--------

* Realtime convolution of impulse responses with stereo audio
* Supports Mono, Stereo and 'True Stereo' (4-channel) impulses
* Very reasonable CPU consumption
* Maximum impulse length: 1M samples (~22 seconds @ 48kHz)
* Loads a large number of audio file formats
* High quality sample rate conversion of impulse responses
* Stretch control (via high quality SRC in one step integrated with impulse loading)
* Pre-delay control (0-2000 ms)
* Stereo width control of input signal & impulse response (0-150%)
* Envelope alteration with immediate visual feedback:
  Attack time/percent, Envelope, Length
* Reverse impulse response
* Autogain: change impulses without having to adjust 'Wet gain'
* Impulse response visualization (linear/logarithmic scale, peak & RMS)
* Easy interface for fast browsing and loading impulse responses
* Free software released under the GNU GPL v2

For further documentation, please visit the GitHub repository at
<https://github.com/tomszilagyi/ir.lv2>.
