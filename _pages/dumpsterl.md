---
layout: page
title:  "Dumpsterl: open-ended data exploration in NoSQL environments"
location: main-area
category: Software
permalink: /dumpsterl/
---

Today's information processing and database systems are more capable
than ever. Nowhere is this so apparent as in the world of NoSQL
databases, now mainstream in enterprise software architecture. With
such a dynamic store of data, business exploration and organic growth
happens naturally and in production, thanks to the absence of an
always enforced, rigid schema.

But hang on... _do you know the data in your system?_

Large software systems are constantly evolving (and we all love hot
code updates). However, data once produced might stay there forever.
Not surprisingly, accessing data written by an older software version
may lead to unpleasant discoveries. You might want to check your
assumptions before pushing new code to production... or you might want
to validate the data, cleaning up artifacts of an old bug that has
been fixed long ago. Maybe you are just curious and would like to
learn more about your data.

Dumpsterl is my humble answer to these challenges. Dumpsterl
scrutinizes the contents of an entire table, a single column, or an
arbitrary stream of Erlang terms. It goes through the values (or a
random subset of them) and builds comprehensive metadata, essentially
a specification of the data encountered. While doing this, it collects
representative samples possibly annotated by key and timestamp to
support further probing. Dumpsterl is flexible and easily extensible,
so you can feed it with virtually any source of data.

You might be interested in my talk
[Dumpster Dive your Erlang data!][talk] that I gave at the Erlang User
Conference in 2017 to introduce and showcase dumpsterl. The [handouts]
of the talk might also be useful if you don't want to watch the video
-- along the slides, they also contain an edited transcript of what I
was telling the audience. (Although you will miss the most interesting
part, the live demo, which was not slide-based.)

If you are ready to dive into your data, please go to the
[online documentation][doc] page. Make sure to check out the [User
Guide] and the [API documentation].  The source is on [GitHub].


[doc]:                doc/overview-summary.html
[GitHub]:             https://github.com/tomszilagyi/dumpsterl
[User Guide]:         doc/guide/book.html
[API documentation]:  doc/ds.html
[talk]:               http://www.erlang-factory.com/euc2017/tom-szilagyi
[handouts]:           tomszilagyi_euc2017.pdf
