#!/bin/bash

#
# Helper script to put everything in the right place
# for publishing under my GitHub pages.
#

SRC=$HOME/src/dumpsterl
TALK=$HOME/doc/talks/20170609-EUC-Dumpsterl

# copy the handout from the conference
cp $TALK/handout.pdf tomszilagyi_euc2017.pdf

# copy the documentation from dumpsterl repo
mkdir -p doc/guide/images
cp $SRC/doc/ds.html doc/
cp $SRC/doc/erlang.png doc/
cp $SRC/doc/overview-summary.html doc/
cp $SRC/doc/shell_ref.txt doc/
cp $SRC/doc/stylesheet.css doc/
cp $SRC/doc/guide/book.adoc doc/guide/
cp $SRC/doc/guide/book.html doc/guide/
cp $SRC/doc/guide/images/*.png doc/guide/images/
