---
layout: post
title:  "Erlang shell: support Del, Home & End"
---

The Erlang/OTP maintainer team finally merged my modest [pull
request].

This is a small step in the direction of a more usable Erlang
shell. As someone working daily with this interface, absence of
support for these keys has been a major PITA, so I decided to improve
the situation. The only turn-off to regular contribution was the
rather long response time (I opened the pull request -- adding a total
of *four lines* to the codebase -- on the 16th of June, mind you).

Anyway, there it is. One more pain point for me regarding this shell
is that when I delete from inside a parenthesized section of a command
line (ie. the cursor is before the closing paren) the line after the
closing paren tends to get garbled. This is fixable by hitting Ctrl-L
to redraw the line. I only learned about this capability by looking at
the Erlang terminal driver source, so it seems a bit hidden.

[pull request]: https://github.com/erlang/otp/pull/794
