---
layout: page
title:  "gen_serial: Cross-platform serial port driver for Erlang"
location: main-area
category: Software
permalink: /gen_serial/
---

In July 2014 I released an updated version of Shawn Pierce's
gen_serial, which, when shopping around for a library allowing serial
port access from Erlang, was clearly the only serious contender.

However, I needed this interface to work on both Linux and Windows.
[Shawn's 0.1], while providing an excellent framework and a great
Windows backend implementation, lacked a POSIX backend. So I set out
to add one. Thankfully the code layout was quite easy to understand
and with some relevant C programming experience on Linux, I could
quickly complete the task.

Shawn kindly gave me his blessing of me taking over his project. So
here it is: a generic serial port interface for Erlang, working on
both Linux and Windows.

Please find the source on [GitHub]. The [API documentation] is also
available online.

Enjoy!


[Shawn's 0.1]:        http://blog.spearce.org/2004/02/genserial-01-released.html
[GitHub]:             https://github.com/tomszilagyi/gen_serial
[API documentation]:  api/gen_serial.html
