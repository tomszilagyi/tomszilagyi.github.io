#!/bin/sh

cd /root

export LC_ALL="C.UTF-8"
export LANG="en_US.UTF-8"
export LANGUAGE="en_US.UTF-8"

if [ ! -d .bundle ] ; then
	cp -a /.bundle .
	cp -a /Gemfile.lock .
fi
bundle exec jekyll serve
