---
layout: page
title:  "DiGenO, a Distributed Genetic Optimizer"
location: main-area
category: Software
permalink: /digeno/
---

[DiGenO] is a Distributed Genetic Optimizer framework in Erlang. It
first emerged as a small part of a much larger "Top Secret Research
Project" I was working on, but later I realized that it could be
broken out and independently released under a permissive BSD
license. This turned out to be a wise decision, because it enabled
further evolution (no pun intended) of the framework long after the
obsolescence of its original surroundings.

The codebase of DiGenO is incredibly small, under 1000 lines. It still
handles the transparent addition or deletion of computation nodes, and
the automatic distribution of operations that perform the genetic
algorithm. It goes to show how compact and well fitting the Erlang
language is when implementing distributed computing algorithms.

DiGenO implements a *steady state* evolution algorithm. By this we
mean that after an initial population is built up, subsequent
evolution is performed on a continuous basis, swapping out old
instances in the population for new ones piecewise. This is in
contrast with more conventional evolutional algorithms that iterate by
generating a complete successor population at once. They usually do
this by keeping a few percent of instances (the "elite") intact, and
filling up the rest by mutations and cross-overs of instances selected
from the preexisting pool.

Instead of this, DiGenO generates, evaluates and releases into the
population new instance candidates on a continuous basis, thereby
performing a more gradual evolution. Each occasion of releasing a
newly generated instance into the population is called a *reduction*.
The number of reductions is a metric showing how many steps the
genetic algorithm has done, and is similar to (but much larger than)
the count of successive populations evolved with a conventional
algorithm.

It is easy to formulate problems so that they can be optimized by
DiGenO. Read this [tutorial] if you are interested in trying the
framework.


[DiGenO]:           https://github.com/tomszilagyi/digeno
[tutorial]:         /2016/03/DiGenO-tutorial
