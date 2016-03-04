---
layout: page
title:  "Telemetry radio"
location: main-area
category: Software
permalink: /tmradio/
---

This was one of my main jobs from the start of 2013 throughout to
mid-2015, while I worked for the AMORES project. I was developing the
software of a small embedded device, the Texas Instruments CC430 (a
SoC containing an MSP430 &mu;C and a digital sub-1 GHz radio
module). This device was used as the hardware platform of our working
group's telemetry radio. We were doing R&D in UAV (Unmanned Aerial
Vehicle) a.k.a. 'drone' technology, and a good telemetry modem was
something everybody knew we needed.

We decided that we wanted to have a mostly 'dumb' over-the-air duplex
serial connection, with an UART interface connecting the radio module
to the ground station on one end and to the flight computer on the
other.  This provided compatibility with most existing commercially
available options. All design aspects beyond this vision were my
responsibility.

For several months (while waiting for a hardware prototype) I worked
with Texas evaluation kits of the CC430 chip. I acquainted myself with
general development for the MSP430 processor and implemented a
Makefile-driven toolchain on Linux, ditching the Eclipse-based
Windows-only IDE from Texas.

Then I designed a sophisticated scheme of selective packet acknowledge
and retransmit, later carefully tuned and tested to provide the
highest possible payload throughput over the air. The available
air/modem (gross) speeds were carefully matched to correspond to
standard UART (net payload) speeds. I changed the over-the-air packet
format several times during development to accommodate features such
as true diversity support and [forward error correction]. Several
radio speed options were available to the user, who could configure
the device and set several configuration parameters stored in
persistent FLASH.

The diversity support was in fact interesting, because after some
false starts and further research, I managed to implement it in the
"best way". On the reception of each individual packet, the signal
strength was measured on both antennas during the preamble. Then, the
payload of the packet was received on the antenna that gave the better
signal. (Timing issues posed a certain challenge, as the preamble had
to be lengthened to allow enough time for the RSSI measurement
circuits to settle.)

Since at the higher speed settings the UART itself generated a lot of
high-priority interrupts, the code path had to be carefully tuned to
be interruptible without negatively affecting the radio part (which
itself heavily depended on low latency interrupt servicing). I managed
to enable the highest throughput with minimal latency, even at a
payload baudrate of 115,200. I remember completely rewriting the UART
interrupt service routines at least twice. I thought that was
something so trivial that I could fully understand from the outset,
and also something not worth too much attention. I was proven wrong in
both aspects.

What I did anticipate was that I would literally spend several weeks
tuning internal parameters of the radio core in order to get the best
possible AGC performance and sensitivity characteristics. That looked
like a tricky problem from the outset (partly because a few aspects of
the chip were like a black box -- even the datasheet referred to
registers like *internal parameter XYZ* with no further explanation).
I remember getting to the point where I could recite the hexadecimal
values of several important registers for the most common settings.

Right from the start of development, I embedded a complete diagnostic
sub-mode into the software. This grew organically with the "main"
codebase and provided an alternative mode of operation. A pair of
devices, when configured to operate in this diagnostic mode, would
generate test traffic between them and continuously evaluate several
parameters regarding the link quality and performance. Among others,
payload throughput rates, packet error rates, antenna usage shares
(diversity decisions) and signal strength statistics were dumped once
per second to a dumb terminal connected to the UART.

Then I decided to have a little fun, and implemented link distance
estimation based on measuring the signal propagation delay. Or, to be
more exact, the time from the very end of the transmitted packet to
the start of the incoming ACK. For this to work, the critical code
path needed to be carefully tuned so that all conditional branches
took the same time to execute -- putting some NOPs to good use.
Eventually I achieved an accuracy of about 100 meters, which was a
reasonable margin of error given the low precision of the main
oscillator (furthered by temperature drift) and a timer resolution of
0.2 &mu;s. I still employed a table of radio mode-dependent
compensation constants carefully tuned to give the most accurate
results.

At this point I think it is worth mentioning that the CC430 I used had
exactly 4 kB of RAM. Compared to this, I was spoiled with 64 kB of
FLASH memory. As a consequence, I used constant lookup tables for
computations and conversions everywhere I could.

In the inevitably painful process of productification I eventually
devised a clever scheme to be able to do in-field firmware upgrades
via the single UART normally used for data communications, and ditched
the proprietary Texas MSP430 programmer. The programmer stub was
selected via shorting a jumper on the circuit board during
power-up. This way the chance of a software error accidentally causing
the device to (re)boot into waiting for being reprogrammed while in
the air could be eliminated. (The watchdog reset was disabled in this
case.)

During the research part of this phase I gained intimate knowledge
about the MSP430 bootloader, in fact much more intimate than I ever
desired. I wrote a separate host application written in Python that
contained the firmware images of all versions of the product, and the
user could choose which one should be flashed to the connected
device. This Python application was then bundled into a
self-extracting executable that could run anywhere. Everything
magically worked. Needless to say that this programmer application was
itself an [interesting activity].

In the end, the development project was a great success -- at least
from a technical point of view. During our last shoot-out using a real
drone flying a test-course of 15 kilometers, the link was continuously
maintained at a payload speed sufficient to control and monitor the
telemetry of an off-the-shelf MAVLink-based drone. With a radio
transmit power of 400 mW, maintaining a radio link between a moving
endpoint over uneven terrain from over ten kilometers was considered a
nontrivial achievement.

I authored the following two documents concerning the product.

![PDF](/images/common/pdf.png) [Handbook]  
![PDF](/images/common/pdf.png) [Performance evaluation]


[forward error correction]:   /2014/04/Golay-codec-optimisation
[interesting activity]:       /2013/12/python-msp430-tools
[Handbook]:                   /files/tmradio/AMORES_TeRad_Handbook.pdf
[Performance evaluation]:     /files/tmradio/AMORES_TeRad_PerfEval.pdf
