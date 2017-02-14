---
layout: post
title:  "GNU Wget += --retry-on-http-error"
---

Lately, I found myself frustrated after having recursively downloaded
parts of some websites via [Wget]. By chance, I discovered that one or
two files had been skipped despite the program's otherwise well
working retry mechanism. It turns out that it is not trivial to fix
the result of such a situation. What can you do when you have
downloaded several dozen html files linking to hundreds (perhaps
thousands) of other resources, with all the links neatly re-written to
work offline...  except for a few files that failed to download? A
simple retry of the whole download is not feasible, as the `-r`
(`--recursive`) option does not prevent re-downloading all the other
correctly downloaded material again. It is not especially harmful *per
se*, since those files will be overwritten with identical content, but
still, it is a waste of time, bandwidth and server resources. And
partial re-downloads of missing material do not play well with `-k`
(`--convert-links`), also required for a download suitable for local
browsing. The best solution, as always, would be to prevent the
failures from happening in the first place.

Inspection of the logs revealed that the downloads failed with obscure
error codes such as HTTP 503 (Service Unavailable). Investigating
further, I found that the exact semantics of several HTTP result codes
are server-specific (or rather, CDN- or webapp-specific). Hence, it is
not entirely clear whether Wget should retry the retrieval in such
cases.  And indeed, it does not retry in case of a 503. In my case,
the CDN apparently decided that servers were momentarily overloaded
and denied my request in the name of load regulation, leaving me
unhappy with an incomplete, broken download.

At any rate, I found it unfortunate that there was no way to force
Wget to retry in these cases. Others, too, have seemingly arrived at
the [same conclusion]. This led me to start poking around in the
source code of Wget to see if I can modify its wired-in behaviour.

The answer is [yes]. Wget maintainer *Tim Rühsen* was kind to quickly
respond and comment on my patch, merging it after a round of
thoughtful review comments. As a byproduct of accepting my patch, a
[feature request] open for *eleven years* has now been closed!

From the contributed usage documentation:

> `--retry-on-http-error=code[,code,...]`
>
> Consider given HTTP response codes as non-fatal, transient errors.
> Supply a comma-separated list of 3-digit HTTP response codes as
> argument. Useful to work around special circumstances where
> retries are required, but the server responds with an error code
> normally not retried by Wget. Such errors might be 503 (Service
> Unavailable) and 429 (Too Many Requests). Retries enabled by this
> option are performed subject to the normal retry timing and retry
> count limitations of Wget.
>
> Using this option is intended to support special use cases only
> and is generally not recommended, as it can force retries even in
> cases where the server is actually trying to decrease its load.
> Please use wisely and only if you know what you are doing.

My patch has been part of the changes [released] as Wget version
1.19.1, so now (if you run that version or newer) you can just write
something like:

    wget --retry-on-http-error=503 ...

If there are multiple HTTP result codes for which you would like Wget
to retry ignoring the "usual" retry semantics, list them
comma-separated.  I found it useful to test the behaviour by pointing
Wget to a test service:

    wget --retry-on-http-error=429,503,504 http://httpbin.org/status/503

That URL will always answer with code 503; you can change the number
in the URL to any other code to get that result code in the reply.

In real life, however, you really want to use this together with a
sufficiently high `--waitretry` setting. You do not want to noticeably
increase the load on the server, but then again, you don't want to be
the one suffering from an incomplete download either. So the best
solution is to retry after a politely long wait. You might also want
to use option `-t` (`--tries`) to change the allowed retry count.


[Wget]:             https://www.gnu.org/software/wget/
[yes]:              https://lists.gnu.org/archive/html/bug-wget/2017-02/msg00063.html
[released]:         https://lists.gnu.org/archive/html/bug-wget/2017-02/msg00065.html
[same conclusion]:  https://savannah.gnu.org/bugs/?20417
[feature request]:  https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=347573
