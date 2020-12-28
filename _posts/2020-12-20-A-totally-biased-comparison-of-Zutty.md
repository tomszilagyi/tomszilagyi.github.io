---
layout: post
title:  "A totally biased comparison of Zutty (to some better-known X terminal emulators)"
---

[Zutty] is the best X terminal emulator you have never heard of. It
employs a radically simple, massively parallel rendering algorithm to
completely offload the work of drawing the terminal screen to the GPU.
This is quite different from how other terminal emulators operate,
GPU-accelerated ones included. You can read more on [How Zutty works].

Here I am interested in comparing Zutty to some alternatives: partly
those that are well-known and have been around for many decades, but
also (and especially) to other, newer programs similar to Zutty in
that they also employ some kind of GPU-based rendering.

The comparison will be unashamedly *biased*: as the author of Zutty, I
have a natural tendency to select criteria that makes Zutty look good.
At the same time, I will try to make a fair comparison based on
quantifiable metrics. Please note: I am in no way trying to convey
that Zutty is altogether superior to any of the programs mentioned
here; all of the programs are useful to at least some people and they
have been written by obviously talented and dedicated programmers. I
do not believe in reducing ranking among programs to a one-dimensional
metric (a.k.a, "which program is better/best"), as the answer is, in
nearly all aspects, "it depends". The standard disclaimer also
applies: terminals are notoriously complex beasts and my findings with
any of the terminals might be due to "other factors" (including my own
mistakes).

That said, I will aim to show that Zutty provides a compelling set of
features at a high level of correctness and excellent performance
characteristics. Compared to existing GPU-accelerated terminal
emulators, Zutty runs on a wider range of graphics hardware by virtue
of only requiring OpenGL ES as opposed to "desktop" OpenGL, and its
resource demands are otherwise minimal. All this is provided by a
relatively tiny codebase.

Zutty has been written from scratch, and while doing so, I have *not*
consulted the implementation of any other terminal emulator. As a
result, Zutty has a clean-room codebase unencumbered by historical
baggage. The pervasive cross-pollination observed across the terminal
emulator ecosystem is not necessarily a problem in itself, but fresh
blood certainly makes for better diversity than endless recycling.

At the same time, one could argue that Zutty is not for everyone: it
runs on Linux only [update: also on FreeBSD and OpenBSD], and is tied
to the X client API (if only for creating a window and receiving
events). It has relatively little in the "fancy features" department,
even though it is quite configurable (for the things I care about,
anyway). But it also has a list of missing features: things that
should eventually be supported, but are currently missing.

Comparison criteria will be grouped into three broad categories:
Features and correctness; performance and resource usage; and finally
miscellaneous (developer-oriented) metrics.


# Programs included in this comparison

All programs are actively developed with publicly accessible codebases
and recent releases. Sorted in decreasing order of popularity/ubiquity
(no science here, just my gut feeling):

- [xterm](https://invisible-island.net/xterm/xterm.html): The standard
  terminal emulator for the X Window System, the venerable xterm is
  the mother of all X terminal emulators. Carrying several decades of
  historical baggage, but at the same time complete with support for
  (almost) any terminal-related standard or feature. Runs on every
  platform supported by the X Window System.

- [gnome-terminal](https://en.wikipedia.org/wiki/GNOME_Terminal): The
  GNOME Terminal Emulator, as its name suggests, is the official
  terminal emulator provided by the GNOME project. It is released
  under the GNU GPLv3 or later. Gnome terminal uses the
  [VTE](https://wiki.gnome.org/Apps/Terminal/VTE) library under the
  hood, which places it on equal footing with Xfce-terminal,
  Terminator, and a long list of others. Enormous installed base.

- [urxvt](https://software.schmorp.de/pkg/rxvt-unicode.html): Also
  known as rxvt-unicode, a Unicode-oriented reboot of rxvt, itself a
  dumbed down clone of xterm. Boasts features such as "crash-free",
  "flicker-free", using much less memory than xterm, customizability
  with embedded Perl, etc. Still widely used and referred to, and has
  a reputation of being fast. Released under the GNU GPLv3.

- [st](https://st.suckless.org/): A "simple terminal" provided by
  the "suckless" project. It is released under the X/MIT license.
  This program expressly positions itself as minimal (bloat-free)
  and highly performant. Configuration requires recompilation.

- [Alacritty](https://github.com/alacritty/alacritty): The first
  GPU-accelerated terminal emulator to achieve widespread adoption,
  famously claiming itself to be "the fastest terminal emulator in
  existence" (more on this claim below). Written in Rust, a new
  language for systems programming. Runs on several UNIX-like
  platforms (including MacOS) as well as Windows. Distributed under
  the Apache 2.0 license.

- [Kitty](https://sw.kovidgoyal.net/kitty/): Another GPU-accelerated
  terminal written in C and Python. Has its own framework for
  extensions written in Python. Runs on Linux and MacOS. Advertises
  itself as a "feature full [*sic*], cross-platform, *fast*, GPU
  based" terminal emulator. Released under the GNU GPLv3.

- [Zutty](/zutty): GPU-accelerated terminal emulator written by this
  author, aimed at low-end (low-cost, low-energy) systems with
  hardware support for OpenGL ES 3.1. Written in C++, runs on Linux
  and other UNIX-like systems (tested on FreeBSD and OpenBSD).
  Released under the GNU GPLv3.


# Test environment

My regular low-energy home desktop system is an [ASUS TinkerBoard
S](https://en.wikipedia.org/wiki/Asus_Tinker_Board) with an RK3288
32-bit SoC containing an ARM Mali T760 GPU providing hardware support
for OpenGL ES 3.2. Unfortunately, not all tested programs are capable
of running on this (otherwise quite capable) system: Alacritty and
Kitty require "desktop" OpenGL and thus cannot run on this board.  On
the other hand, Zutty runs perfectly fine and is my primary terminal
here.

For the purposes of this article, my test system is a six year old
Lenovo laptop with a 4-core i5-4210U CPU and an Intel Haswell Mobile
chipset with integrated graphics, providing hardware support for
OpenGL ES 3.1 and OpenGL 3.0. Contrary to my low-energy desktop, this
is a 64-bit system. The software is Debian Buster (current stable); I
use the i3 window manager, and the configured locale is `en_US.UTF-8`.

All terminal window screenshots linked into this article were produced
by the [automated testing](/zutty/doc/HACKING.html#Automated%20testing)
toolkit of Zutty. As documented, this toolkit is capable of evaluating
other terminals as well as Zutty, and to collect the screenshots and
data presented below, I relied on exactly this capability.


# Features and correctness

The below table summarizes essential features of each tested terminal.

The value of `TERM` within the shell started by the terminal conveys
the terminal's type to programs being run in it. For terminals
providing their own terminfo entries, the table links to that data as
well as a diff to the quasi-standard `xterm-256color` used by others.

Device attributes (VT self-identification) are what the terminal
proclaims about itself when queried. This is revealed by [VTTEST] menu
items 6.4 and 6.5, and the links PDA (primary device attributes) and
SDA (secondary device attributes) lead to the corresponding screen
captures of each program.

program | tested version | configured font | TERM setting | VT self-id
:--- | :--- | :--- | :--- | :---
gnome-terminal | 3.30.2, VTE 0.54.2 | Monospace 11 | xterm-256color | VT525: [PDA](/zutty/test/output/gnome-terminal/vt_06_04.png) [SDA](/zutty/test/output/gnome-terminal/vt_06_05.png)
Zutty | 0.6 | misc-fixed 9x18 | xterm-256color | VT520: [PDA](/zutty/test/output/zutty/vt_06_04.png) [SDA](/zutty/test/output/zutty/vt_06_05.png)
xterm | 344 | misc-fixed 9x18 | xterm-256color | VT420: [PDA](/zutty/test/output/xterm/vt_06_04.png) [SDA](/zutty/test/output/xterm/vt_06_05.png)
Kitty | 0.19.3 | DejaVuSansMono | [xterm-kitty](/files/zutty/terminfo/xterm-kitty.txt) [[diff](/files/zutty/terminfo/xterm-kitty-diff.txt)] | VT220: [PDA](/zutty/test/output/kitty/vt_06_04.png) [SDA](/zutty/test/output/kitty/vt_06_05.png)
Alacritty | 0.4.3 | DejaVuSansMono | xterm-256color | VT102: [PDA](/zutty/test/output/alacritty/vt_06_04.png) [SDA](/zutty/test/output/alacritty/vt_06_05.png)
st | 0.8.2 | Default OOTB | [st-256color](/files/zutty/terminfo/st-256color.txt) [[diff](/files/zutty/terminfo/st-256color-diff.txt)] | VT102: [PDA](/zutty/test/output/stterm/vt_06_04.png) [SDA](/zutty/test/output/stterm/vt_06_05.png)
urxvt | v9.22 | misc-fixed 9x18 | [rxvt-unicode-256color](/files/zutty/terminfo/rxvt-unicode-256color.txt) [[diff](/files/zutty/terminfo/rxvt-unicode-256color-diff.txt)] | VT102: [PDA](/zutty/test/output/urxvt/vt_06_04.png) [SDA](/zutty/test/output/urxvt/vt_06_05.png)

<br>

All established programs were tested with their version present in
Debian Buster (current stable). The latest stable release of Kitty was
manually installed, as was Alacritty. Alas, Alacritty stopped
releasing .deb files, so I tested their last version for which a .deb
was released.


## VT support levels

The above table is sorted by descending level of proclaimed terminal
support. The difference between implementing a VT400 or VT500 terminal
(provided the implementations are correct) is relatively
inconsequential. The same cannot be said of the gap between these and
VT220-level terminals. Programs running in a less capable virtual
terminal must sometimes use longer series of basic escape sequences to
achieve the results of a fewer number of more modern ones. For
example, the DECLRMM control sequence (set left-right margin mode) is
available from the VT420 and up, and will be used to restrict
scrolling to the active part of two horizontally split `tmux` panes. On
less capable terminals, `tmux` is forced to perform more work to
achieve the same result. This is quite similar to how extended
instructions on modern CPUs allow machine code to be more efficient
than code compiled for an older machine. In other words, not
implementing a modern VT variant goes directly counter to greater
efficiency. And there is yet another downgrade from VT220 to those
terminals that only claim to support VT100/VT102.

It is surprising how unambitious the newer terminals Alacritty and
Kitty are in this regard. I would have hoped that new, supposedly
state-of-the-art entrants would take the effort to add support for
modern VT standards. Switching from xterm to Alacritty (or any other
modern terminal) should not be a major downgrade.


### Side note on VT52 support

On the low end of the VT level hierarchy sits the VT52. This is purely
of academic interest in 2020, but still: there is technically no way
for a "VT100 or better" terminal to be standards-compliant and *not*
support VT52 (as the VT100 spec contains a sequence for downgrading
into VT52 mode). Also, given that VT52 is a (small) subset of what the
VT100 is capable of, any reasonably well structured implementation
should find it easy to fit this in with a minimal amount of additional
code. However, of all the terminals included in this test, only xterm,
urxvt and Zutty implement VT52 mode.


## TERM settings

Another problem area for newer entrants into the terminal ecosystem is
the value of TERM set in the shell. This is a bit of a catch-22 for
all programs that are not xterm, since they can choose from two less
than great options. One, they can provide a terminfo entry that
accurately describes their capabilities, and require that users
install these (often separately packaged) files to be able to
correctly use the terminal. This sounds fine, unless you realize that
the terminfo is required on all *remote* systems as well that you
would ever want to SSH into, in order for your local terminal emulator
to work as intended. The second option is to "hijack" the terminfo of
a well-known, modern terminal, which in practice is almost always
`xterm-256color` (or more generally, `xterm-*`). Now, the problem
becomes that your terminal is *not* xterm, but since the client
application has no way of knowing this, any incompatibilities will
cause breakage.

Notably, Kitty has gone down the route of providing its own terminfo
`xterm-kitty` (the naming in itself has become a
[source](https://lists.nongnu.org/archive/html/bug-ncurses/2018-12/msg00015.html)
of
[controversy](https://lists.nongnu.org/archive/html/bug-ncurses/2018-09/msg00007.html)),
and has its obligatory FAQ entry for the "unknown terminal" kind of
error -- clearly not the ideal path to user satisfaction. On the other
hand, Alacritty simply declares itself as just another xterm -- while
supporting only VT102. This is actually less problematic than it
looks: applications should query the terminal's support level (device
attributes) instead of jumping to conclusions based on its name. For
Zutty, I have chosen the second path, as Alacritty and countless
others before me, with the significant difference of aiming for modern
VT support.


## Support for bitmap fonts

Neither Alacritty nor Kitty seems to support bitmap fonts. This is
sad, because in my humble opinion, some of the old-school bitmap fonts
are still the best option for putting crisp, well-readable text on
low-resolution computer displays (between 70 and 120 dpi).  I have a
strong personal preference towards working with the misc-fixed 9x18
font (which provides excellent Unicode support), and there is no way
to use this font (or any other such font) on these terminals.  Again,
switching from xterm to Alacritty (or any other modern terminal)
should not be a downgrade. With Zutty, I also hope to make a statement
of keeping bitmap fonts usable in modern terminals.


## Correctness of claimed VT support

The sadness only deepens when one starts to scrutinize the actual
conformance levels of the terminals against well established standards
and prior art set by the actual VT-series terminals manufactured by
DEC, independent standards such as ISO-6429 (ECMA-48), as well as
established programs such as xterm. Here we defer to the [VTTEST]
program as the source of truth.

Sadly, this section is going to look like an endless list of bugs. I
have to state up-front that different programs have different
development goals, and achieving a perfect score on VTTEST is probably
not the most important goal for most of them (or the below sections
would look very different).

My personal view is that correctness is *important* -- if you claim
yourself a VT100 (or better), you better act like one. Not everyone
agrees with me though: if presented with a choice, some people will
prefer transparent backgrounds instead (and some of *them* will go on
and write their own terminals). To be clear: there is nothing wrong
with this, as long as everyone is allowed to choose the programs they
use, or decide to write their own.

All screens will be presented side by side with the output of Zutty
for comparison, but feel free to play along with xterm instead (you
will see identical results).


### gnome-terminal

Given that gnome-terminal identifies itself as a VT525 and sets TERM
to `xterm-256color`, and given its ubiquity (being the default
terminal of the GNOME Desktop), expectations on its compatibility are
set high. But alas:

<table width="100%">
   <tr>
      <th width="50%">gnome-terminal</th>
      <th width="50%">Zutty</th>
   </tr>

<tr><td colspan="2">
<details><summary>2.2. TAB setting/resetting</summary>
<img src="/zutty/test/output/gnome-terminal/vt_02_02.png" width="48%">
<img src="/zutty/test/output/zutty/vt_02_02.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>6.2. LineFeed/NewLine mode</summary>
<img src="/zutty/test/output/gnome-terminal/vt_06_02.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_02.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.3.2.9. Cursor movement within margins</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_02_09_05.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_02_09_05.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_02_09_06.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_02_09_06.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_02_09_07.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_02_09_07.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.3.2.10. Other movement within margins</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_02_10_05.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_02_10_05.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_02_10_06.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_02_10_06.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_02_10_07.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_02_10_07.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.3.3.9. Insert/delete column (DECIC/DECDC)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_09_01a.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_09_01a.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_09_01b.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_09_01b.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.3.3.11. Insert/delete line (IL, DL)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_11_03a.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_11_03a.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_11_04a.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_11_04a.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.3.3.13 ASCII formatting (BS, CR, TAB)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_13_05.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_13_05.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_13_06.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_13_06.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_03_13_07.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_03_13_07.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.3.4. Backarrow key (DECBKM)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_03_04_01.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_03_04_01.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.4.2.10. Cursor-Horizontal-Index (CHR)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_04_02_10_05.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_04_02_10_05.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_04_02_10_06.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_04_02_10_06.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_04_02_10_07.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_04_02_10_07.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.4.2.11. Horizontal-Position-Relative (HPR)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_04_02_11_01.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_04_02_11_01.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.4.2.15. Vertical-Position-Relative (VPR)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_04_02_15_01.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_04_02_15_01.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.6.3. SGR-0 color reset</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_06_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_06_03.png" width="48%">
</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.7. Scroll-Left (SL), Scroll-Right (SR)</summary>
<img src="/zutty/test/output/gnome-terminal/vt_11_07_04.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_07_04.png" width="48%">
<br><br>
<img src="/zutty/test/output/gnome-terminal/vt_11_07_05.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_07_05.png" width="48%">
</details>
</td></tr>

</table>

<br>

Surprise: *a steaming pile of bugs* in various problem areas, some of
them pretty basic (such as TAB handling). Horizontal margins (VT420
and above) seem to be completely unsupported, as well as various other
sequences from VT420, VT520 and ISO-6429 (ECMA-48) -- not entirely
sure if the latter is in scope. Overall, a puzzling outcome!

Digging a bit further, I discovered some ambiguity on whether
gnome-terminal supports the claimed VT520 level at all: its
[manpage](https://www.systutorials.com/docs/linux/man/1-gnome-terminal/)
(not organic to the project, but contributed by Debian) has this to
say about the support level of gnome-terminal: `Run any application
that is designed to run on VT102, VT220, and xterm terminals.`

That is pretty sloppy as far as specifications go. Apparently, it
might be the case that nothing above VT220 is intentionally supported
by gnome-terminal at all, and that its primary device attributes
[response](/zutty/test/output/gnome-terminal/vt_06_04.png) is a *big
fat lie*. Note: the xterm FAQ entry about [known
bugs](https://invisible-island.net/xterm/xterm.faq.html#known_bugs)
also has a section about gnome-terminal.

As a closing note on this program: I did discover the existence of the
`gnome-256color` terminfo. In absence of any clear documentation, I am
guessing that this *might* be intended as a closer match to the
terminal's capabilities than the default `xterm-256color`. However,
the latter is the default value of TERM within gnome-terminal, and in
fact I have not found any way to persuade the program to change this.
Nevertheless, I verified that manually changing TERM to
`gnome-256color` does *not* affect the issues outlined above in any
way.

Okay, let's move on to some more modern, GPU-accelerated terminals!


### Kitty

According to the author of Kitty, [tmux is a bad
idea](https://sw.kovidgoyal.net/kitty/faq.html#i-am-using-tmux-and-have-a-problem),
apparently because it does not care about the arbitrary xterm-protocol
extensions Kitty implements. Ostensibly, terminal multiplexing
(providing persistence, sharing of sessions over several clients and
windows, abstraction over underlying terminals and so on) are either
unwarranted and seen as meddling by a middleman, or should be provided
by the terminal emulator itself (Kitty being touted as the innovator
here). A remarkable standpoint, to say the least.

Kitty comes with its own terminfo `xterm-kitty`. This needs separate
manual installation (I don't run curl-pipe-shell installers -- this
newfangled fashion is spreading like a disease, but it is a
[Very](https://www.seancassidy.me/dont-pipe-to-your-shell.html)
[Bad](https://xkcd.com/1654/)
[Idea](https://www.idontplaydarts.com/2016/04/detecting-curl-pipe-bash-server-side/)
[Nevertheless](https://drj11.wordpress.com/2014/03/19/piping-into-shell-may-be-harmful/)).
Apart from [minor
differences](/files/zutty/terminfo/xterm-kitty-diff.txt) (consult
[terminfo(5)](https://man7.org/linux/man-pages/man5/terminfo.5.html)
if you are interested in decoding them), the main point of difference
is that Kitty does not support background colour erase (cap-name
`bce`), in favour of its proposed (self-implemented) extensions,
promoting capabilities such as support for coloured, wavy underlines
and drawing arbitrary raster images in the terminal.

Nevertheless, the basic VT implementation has some potholes to fix:

<table width="100%">
   <tr>
      <th width="50%">Kitty</th>
      <th width="50%">Zutty</th>
   </tr>

<tr><td colspan="2">
<details><summary>1.3. Autowrap, mixing control and print characters</summary>
<img src="/zutty/test/output/kitty/vt_01_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_01_03.png" width="48%">
<br>

This screen tests the faithful reproduction of quirky original VT
behaviour; read about it in the section <i>Glitches and
Braindamage</i> of the <a
href="https://man7.org/linux/man-pages/man5/terminfo.5.html">terminfo(5)</a>
manual page, or see <a
href="https://github.com/mattiase/wraptest">wraptest</a>, a testing
tool dedicated to this issue. The xterm FAQ has its <a
href="https://invisible-island.net/xterm/xterm.faq.html/en-en/ctlseqs/terminfo.html#vt100_wrapping">own
entry</a> about it, as well as a <a
href="https://invisible-island.net/vttest/vttest-wrap.html">screenshot
gallery</a> of various terminals, compliant or not. Fun fact: the <a
href="/files/zutty/terminfo/xterm-kitty.txt">xterm-kitty</a> terminfo
includes <code>xenl</code> a.k.a. the "eat-newline-glitch" capability,
just like all other terminals with VT100 in their ancestry...

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>6.2. LineFeed/NewLine mode</summary>
<img src="/zutty/test/output/kitty/vt_06_02.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_02.png" width="48%">
<br>

This should be implemented by anyone claiming to be a VT100 or better.

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>6.3. Device Status Report (DSR)</summary>
<img src="/zutty/test/output/kitty/vt_06_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_03.png" width="48%">
<br>

Like the previous screen, this is in scope for a VT100 or better.  The
problem here is with the response to the last query, which is when
VTTEST asks for a cursor position report after enabling DECOM (origin
mode).

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.1.2.1. Send/Receive mode (SRM)</summary>
<img src="/zutty/test/output/kitty/vt_11_01_02_01.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_01_02_01.png" width="48%">
<br>

Local echo seems not to be supported, even though it is totally in
scope for a VT220.

</details>
</td></tr>

</table>

<br>

At this point I stop the VTTEST tour of Kitty, simply because it would
be deeply unfair to test a VT220 for things it does not even claim to
support (spoiler alert: it is a mixed bag -- some work, others do not).
But see the above section titled "VT support levels" on why, in my
opinion, a modern terminal should consistently support modern VT levels.


### Alacritty

Alacritty certainly has a set of interesting design choices, partially
overlapping those of Zutty when it comes to sticking with the basics
and providing excellent performance. Alas, Alacritty only claims to be
a VT102 (a downgrade even from Kitty's perspective); I guess that is
part of the simplicity thing going on. At least that makes its test
surface much smaller, so let's see how well those basics are covered:

<table width="100%">
   <tr>
      <th width="50%">Alacritty</th>
      <th width="50%">Zutty</th>
   </tr>

<tr><td colspan="2">
<details><summary>6.2. LineFeed/NewLine mode</summary>
<img src="/zutty/test/output/alacritty/vt_06_02.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_02.png" width="48%">
<br>

VT100 and up should clearly support this.

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>6.3. Device Status Report (DSR)</summary>
<img src="/zutty/test/output/alacritty/vt_06_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_03.png" width="48%">
<br>

Ditto here: this is in scope for VT100 and above. The problem is the
same as with Kitty, although the response is different, recognized by
VTTEST as the response that <i>would be correct</i>, had it not
enabled DECOM (origin mode) prior to this query.

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>11.8.7. XTERM Alternate-Screen features</summary>
<img src="/zutty/test/output/alacritty/vt_11_08_07_01.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_08_07_01.png" width="48%">
<br><br>
<img src="/zutty/test/output/alacritty/vt_11_08_07_02.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_08_07_02.png" width="48%">
<br><br>
<img src="/zutty/test/output/alacritty/vt_11_08_07_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_11_08_07_03.png" width="48%">

<br>

A sigh of relief: Alacritty implements at least one of the three
sequences for alternate-screen save-restore. Technically, these
sequences are xterm extensions, but in practice everybody has
supported them since forever. The sequence supported by Alacritty is
the latest and arguably the best, obsoleting older ones.  One could
argue that those are not needed; however, any reasonably structured
program will only need to expend minimal code to keep supporting the
older methods (as most other programs do).

</details>
</td></tr>
</table>

<br>

In conclusion: some missing features here and there -- not great, not
terrible. Definitely not great, considering how small the promised
feature set of a VT102 is compared to a VT520. At least basic screen
output rendering is correct.


### st (simpleterm)

The "simple terminal" from *suckless* boasts minimalism as its primary
strength.  As such, it is similar to Alacritty (albeit much older) and
also does not seek to go beyond VT102 support.

<table width="100%">
   <tr>
      <th width="50%">st</th>
      <th width="50%">Zutty</th>
   </tr>

<tr><td colspan="2">
<details><summary>1.3. Autowrap, mixing control and print characters</summary>
<img src="/zutty/test/output/stterm/vt_01_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_01_03.png" width="48%">
<br>

According to the <a href="https://st.suckless.org/">st homepage</a>,
"xterm is bloated and unmaintainable" (hmm, to me it looks like xterm
has been maintained just fine, <i>for decades</i>). On the other hand,
st is evidently so minimal that it does not even include correct
(basic) VT behaviour. In this case, the suspect is not the
<code>xenl</code> quirk (present in the <a
href="/files/zutty/terminfo/st-256color.txt">st-256color</a> terminfo
as expected), rather it looks like st does not correctly support
DECSTBM (set top/bottom margins) and/or DECOM (origin mode). Maybe st
has pursued minimalism to a fault?

<br>
Note: the maintainer of xterm has published a <a
href="https://invisible-island.net/vttest/vttest-wrap.html#WRAP-st-0.4">screenshot</a>
of st-0.4 exhibiting this issue. I have tested st-0.8.2; fixing this
was clearly not a priority. [Additional notes and links on the issue
under this same screen for Kitty.]

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>2.15. Save/Restore Cursor (plus attributes)</summary>
<img src="/zutty/test/output/stterm/vt_02_15.png" width="48%">
<img src="/zutty/test/output/zutty/vt_02_15.png" width="48%">
<br>

Minimalism also appears to exclude correctly saving and restoring
attributes along with cursor save/restore operations.

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>6.3. Device Status Report (DSR)</summary>
<img src="/zutty/test/output/stterm/vt_06_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_03.png" width="48%">
<br>

At this point, I am all but surprised: st is minimal indeed.

</details>
</td></tr>

</table>

<br>

On the plus side, st does support LineFeed/NewLine mode, as well as
Send/Receive mode (SRM) a.k.a. local echo on/off. And, despite all the
focus on minimalism, *all three* alternate-screen save/restore
sequences are supported and working correctly (looking at you,
Alacritty)!


### urxvt

<table width="100%">
   <tr>
      <th width="50%">urxvt</th>
      <th width="50%">Zutty</th>
   </tr>

<tr><td colspan="2">
<details><summary>1.3. Autowrap, mixing control and print characters</summary>
<img src="/zutty/test/output/urxvt/vt_01_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_01_03.png" width="48%">
<br>

This <a
href="https://invisible-island.net/xterm/xterm.faq.html/en-en/ctlseqs/terminfo.html#vt100_wrapping">xterm
FAQ entry</a> mentions rxvt as one of the terminals known to be
inconsistent with standard VT100 behaviour; the <a
href="https://invisible-island.net/vttest/vttest-wrap.html#WRAP-rxvt">corresponding
screenshot</a> notes "Old and new versions of rxvt behave the same;
its divergence from VT100 is old and well-established." Apparently,
this extends to urxvt as well.

(In case you were wondering: the <a
href="/files/zutty/terminfo/rxvt-unicode-256color.txt">rxvt-unicode-256color</a>
terminfo includes <code>xenl</code>.)

</details>
</td></tr>

<tr><td colspan="2">
<details><summary>6.3. Device Status Report (DSR)</summary>
<img src="/zutty/test/output/urxvt/vt_06_03.png" width="48%">
<img src="/zutty/test/output/zutty/vt_06_03.png" width="48%">
<br>

The same as Alacritty and st, arguably "less wrong" than Kitty.

</details>
</td></tr>

</table>

<br>

On the bright side, LineFeed/NewLine mode is present. Send/Receive
mode (SRM) is not, but that is out of scope for a VT102. Cursor
show/hide is out of scope as well, but that works. In fact, quite a
lot of higher level stuff is implemented, such as (VT420)
backward/forward index (DECBI, DECFI), vertical scrolling (IND, RI),
insert/delete line (IL, DL), insert/delete char (ICH, DCT), even the
backarrow key (DECBKM) -- plus (VT520) HPA, CHA, CBT, and so on.  As a
bonus, all three alternate-screen save/restore sequences are supported
(*khrrm* Alacritty). All in all, not too bad for a VT102.


### xterm and Zutty

So what about xterm and Zutty? You might be asking why they are not
getting their own sections here. For xterm, suffice it to say that it
is maintained by Thomas E. Dickey, who also maintains ncurses and
VTTEST. Also note that xterm is (by a large margin) the oldest of all
the programs listed here, and a de-facto standard. Thomas has once <a
href="https://invisible-island.net/xterm/xterm.faq.html/en-en/ctlseqs/terminfo.html#compare_versions">counted the control sequences</a> supported by various terminals; xterm
provides the most comprehensive support (by a wide margin). Thomas is
widely acclaimed for his ongoing, meticulous work on maintaining the
programs mentioned above (and more); there is really not that much
room for error here.

And regarding Zutty: I used it to provide the reference images for all
the above highlighted issues, obviously to make a statement.  Zutty
has been carefully written to match the output of xterm as much as
possible -- for a great number of rendering tests, it is verified to
produce *pixel-identical output* if set up with the same font. Almost
all deviations from xterm are due to "not yet implemented" features,
documented in their own list (see the bottom of the
[README](/zutty/README.html)).

Feel free to consult the [VTTEST results of Zutty](/zutty/doc/VTTEST.html)
for the complete list of screens covered by the testing toolkit of
Zutty. There are a lot more screens in VTTEST, but I believe I have
covered the most interesting ones.


## UTF-8 handling

All programs claim to be Unicode or UTF-8 terminals, and a good
implementation should handle error cases just as well as decoding
correct UTF-8 data. A comprehensive test of the corner cases is
presented by Markus Kuhn's [UTF-8 decoder capability and stress
test](https://www.cl.cam.ac.uk/~mgk25/ucs/examples/UTF-8-test.txt).

The Zutty automated testing toolkit has a suite for displaying
snippets from this file and verifying the results. The primary aim of
this is to detect regressions in Zutty.  The results of what other
programs produce is included in the table below (you will have to
click on the links to see the screen captures).

Disclaimer: As Markus writes in his first paragraph, *"This file is
not meant to be a conformance test. It does not prescribe any
particular outcome. Therefore, there is no way to "pass" or "fail"
this test file, even though the text does suggest a preferable decoder
behaviour at some places."* Which is exactly what we do here,
comparing implementations on how close they are to the *preferable*
decoder behaviour. That is, deviations are not necessarily bugs in any
program, merely implementation choices that differ from the preferable
ones. Please refer to the linked document to understand why results
shown as DVN (for "deviation") are less than preferable.

program | 01 | 02 | 03 | 04 | 05 | 06 | 07 | 08 | 09 | 10 | score
:--- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | :---:
Zutty | [good](/zutty/test/output/zutty/utf8_01.png) | [good](/zutty/test/output/zutty/utf8_02.png) | [good](/zutty/test/output/zutty/utf8_03.png) | [good](/zutty/test/output/zutty/utf8_04.png) | [good](/zutty/test/output/zutty/utf8_05.png) | [good](/zutty/test/output/zutty/utf8_06.png) | [good](/zutty/test/output/zutty/utf8_07.png) | [good](/zutty/test/output/zutty/utf8_08.png) | [good](/zutty/test/output/zutty/utf8_09.png) | [good](/zutty/test/output/zutty/utf8_10.png) | 10
xterm | [DVN](/zutty/test/output/xterm/utf8_01.png) | [DVN](/zutty/test/output/xterm/utf8_02.png) | [good](/zutty/test/output/xterm/utf8_03.png) | [good](/zutty/test/output/xterm/utf8_04.png) | [DVN](/zutty/test/output/xterm/utf8_05.png) | [good](/zutty/test/output/xterm/utf8_06.png) | [good](/zutty/test/output/xterm/utf8_07.png) | [DVN](/zutty/test/output/xterm/utf8_08.png) | [good](/zutty/test/output/xterm/utf8_09.png) | [DVN](/zutty/test/output/xterm/utf8_10.png) | 5
st | [DVN](/zutty/test/output/stterm/utf8_01.png) | [DVN](/zutty/test/output/stterm/utf8_02.png) | [good](/zutty/test/output/stterm/utf8_03.png) | [good](/zutty/test/output/stterm/utf8_04.png) | [DVN](/zutty/test/output/stterm/utf8_05.png) | [good](/zutty/test/output/stterm/utf8_06.png) | [DVN](/zutty/test/output/stterm/utf8_07.png) | [DVN](/zutty/test/output/stterm/utf8_08.png) | [good](/zutty/test/output/stterm/utf8_09.png) | [DVN](/zutty/test/output/stterm/utf8_10.png) | 4
gnome-terminal | [DVN](/zutty/test/output/gnome-terminal/utf8_01.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_02.png) | [good](/zutty/test/output/gnome-terminal/utf8_03.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_04.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_05.png) | [good](/zutty/test/output/gnome-terminal/utf8_06.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_07.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_08.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_09.png) | [DVN](/zutty/test/output/gnome-terminal/utf8_10.png) | 2
alacritty | [DVN](/zutty/test/output/alacritty/utf8_01.png) | [DVN](/zutty/test/output/alacritty/utf8_02.png) | [DVN](/zutty/test/output/alacritty/utf8_03.png) | [DVN](/zutty/test/output/alacritty/utf8_04.png) | [DVN](/zutty/test/output/alacritty/utf8_05.png) | [DVN](/zutty/test/output/alacritty/utf8_06.png) | [DVN](/zutty/test/output/alacritty/utf8_07.png) | [DVN](/zutty/test/output/alacritty/utf8_08.png) | [good](/zutty/test/output/alacritty/utf8_09.png) | [DVN](/zutty/test/output/alacritty/utf8_10.png) | 1
kitty | [DVN](/zutty/test/output/kitty/utf8_01.png) | [DVN](/zutty/test/output/kitty/utf8_02.png) | [DVN](/zutty/test/output/kitty/utf8_03.png) | [DVN](/zutty/test/output/kitty/utf8_04.png) | [DVN](/zutty/test/output/kitty/utf8_05.png) | [DVN](/zutty/test/output/kitty/utf8_06.png) | [DVN](/zutty/test/output/kitty/utf8_07.png) | [DVN](/zutty/test/output/kitty/utf8_08.png) | [DVN](/zutty/test/output/kitty/utf8_09.png) | [DVN](/zutty/test/output/kitty/utf8_10.png) | 0
urxvt | [DVN](/zutty/test/output/urxvt/utf8_01.png) | [DVN](/zutty/test/output/urxvt/utf8_02.png) | [DVN](/zutty/test/output/urxvt/utf8_03.png) | [DVN](/zutty/test/output/urxvt/utf8_04.png) | [DVN](/zutty/test/output/urxvt/utf8_05.png) | [DVN](/zutty/test/output/urxvt/utf8_06.png) | [DVN](/zutty/test/output/urxvt/utf8_07.png) | [DVN](/zutty/test/output/urxvt/utf8_08.png) | [DVN](/zutty/test/output/urxvt/utf8_09.png) | [DVN](/zutty/test/output/urxvt/utf8_10.png) | 0

<br>


# Performance and Resource usage

Once again, the [testing toolkit of Zutty](/zutty/doc/HACKING.html#Performance%20tests)
serves as the foundation of our measurements reported here.

To level the playing field, scrollback has been disabled in all terminals:
- xterm: the `-sl 0` option was used
- gnome-terminal: the option "Limit scrollback" was enabled with a setting of 0 lines
- urxvt: `URxvt.saveLines: 0` was added to `~/.Xresources` (and the xrdb reloaded)
- Alacritty: the `scrolling.history` configuration value was set to 0
- Kitty: the `scrollback_lines` configuration value was set to 0

All programs were manually verified to ensure that the changes have
taken effect.  Since st and Zutty do not (currently) support
scrollback, no adjustments to them were necessary.


## The dumb terminal test

This is arguably the dumbest possible performance test of any text
terminal, and consists of outputting a very long (3.4 MB) text file
containing (mostly) very short lines with English words, one per
line. This will be repeated 100 times and the overall timing and data
throughput will be computed at the end. Since the input does not
contain any terminal controls (escape sequences), it is a measure of
the raw incoming data rate the terminal can sustain while frequently
forced to scroll/page its output. This load resembles one extreme end
of the way a terminal can be used.

Reported metrics in the below table are as follows:
- real, user, sys: overall timing statistics as reported by
  [time(1)](https://www.man7.org/linux/man-pages/man1/time.1.html).
  Numbers are in seconds.
- RSS: resident set size, a.k.a. in-memory (non-swapped) physical
  memory allocation, measured at the end of the test (just before the
  program under test exits) via
  [ps(1)](https://www.man7.org/linux/man-pages/man1/ps.1.html).
  Numbers are in kB.
- MAJFLT, MINFLT: count of major and minor page faults, acquired in the
  same measurement as RSS.
- PTY read: calculated read rate, in MB/s, indicating the average
  speed of consuming the input file.

Sorted by increasing overall timings (fastest program first):

program | real | user | sys | RSS | MAJFLT | MINFLT | PTY read rate
:--- | ---: | ---: | ---: | ---: | ---: | ---: | ---:
Alacritty | 16.24 | 0.05 | 16.16 | 30,656 | 1 | 2,930 | 21.75
urxvt | 18.46 | 0.06 | 18.35 | 16,104 | 0 | 2,430 | 19.14
Zutty | 24.65 | 0.04 | 24.55 | 23,912 | 0 | 3,748 | 14.33
gnome-terminal | 36.19 | 0.03 | 18.55 | 41,624 | 0 | 4,894 | 9.76
Kitty | 45.91 | 0.07 | 23.69 | 52,736 | 0 | 11,574 | 7.69
st | 46.08 | 0.03 | 45.18 | 9,432 | 0 | 767 | 7.67
xterm | 912.54 | 0.11 | 103.58 | 10,228 | 0 | 1,589 | 0.39

<br>

## The control sequence-heavy test

This test generates load resembling the other extreme end of possible
terminal usage, by outputting the stream of data written to the
terminal in the course of a complete VTTEST run (refer to `vttest.sh`
in the Zutty testing toolkit). However, instead of verifying the
correctness of the generated screen output, here we are interested in
the performance of processing the input stream heavy on all kinds of
escape sequences. Screen updates are dominated by intra-screen
rewrites and relatively little scrolling/paging activity is forced.
Similar to the previous test, the input is fed into the terminal
several times in a row (400 times for this test), and overall timing
and throughput for the whole run is measured and calculated.

Results in the same format as above:

program | real | user | sys | RSS | MAJFLT | MINFLT | PTY read rate
:--- | ---: | ---: | ---: | ---: | ---: | ---: | ---:
Zutty | 8.59 | 2.18 | 5.73 | 24,004 | 1 | 3,750 | 45.09
Alacritty | 11.61 | 2.07 | 5.77 | 28,520 | 48 | 3,001 | 33.36
Kitty | 21.20 | 2.10 | 5.94 | 56,744 | 81 | 10,362 | 18.27
st | 22.82 | 2.19 | 6.61 | 9,780 | 0 | 981 | 16.97
gnome-terminal | 25.93 | 2.03 | 5.15 | 41,436 | 0 | 5,394 | 14.94
urxvt | 37.07 | 2.45 | 7.07 | 21,584 | 2 | 195,393 | 10.45
xterm | 770.76 | 3.76 | 14.63 | 99,800 | 0 | 31,396,048 | 0.50

<br>

## On performance

- Alacritty scores first in the "dumb terminal" test, about 10% faster
  compared to urxvt, and 50% faster compared to Zutty. In the second,
  more demanding test, Zutty is the fastest (50% faster compared to
  Alacritty). Alacritty is still well ahead (in fact, about twice as
  fast) as the rest of the pack (Kitty, st and gnome-terminal).

- Kitty appears to be a mediocre performer, stuck in the middle
  together with st and gnome-terminal. Kitty is consistently, and by a
  wide margin, the slowest among GPU-accelerated terminals.

- Rumors of xterm being slow are not unfounded. Apart from disabling
  scrollback, I also used the `-j` option when running xterm to enable
  "jump scrolling". According to the xterm manual, this setting
  *"allows xterm to move multiple lines at a time so that it does not
  fall as far behind.  Its use is strongly recommended since it makes
  xterm much faster when scanning through large amounts of text"*.
  Apparently, it does not help much -- xterm is (very) slow, at least
  compared to the others: in both tests, about 20x slower than the
  second slowest. (There is also the `fastScroll` option that works by
  *"suppressing screen refreshes for the special case when output to
  the screen has completely shifted the contents off-screen"*. I tried
  enabling it; xterm ceased to update the screen on the dumb test and
  its timing results did not improve on the second test.)

- Comparing absolute PTY read speeds across the tests, only urxvt has
  gotten slower on the second test. Even xterm has managed to improve
  its (low) speed; all the others have achieved higher read speeds on
  the second test.


A subjective observation on gnome-terminal: While running the tests, I
noticed that this program behaved visibly different than all others,
in that its window refresh rate seemed suspiciously low. Instead of
the constant high-speed flicker that all others exhibited,
gnome-terminal repainted its window with a leisurely rate, which I
estimated to be about 10 per second. That is a slide-show relative to
the action movie played by the others! I have no explanation for this,
but it is very apparent on visual inspection.


## On memory usage

- Of the three GPU-accelerated terminals, Zutty consistently uses the
  least amount of memory -- less than half of what Kitty consumes, in
  both tests.

- Overall, only xterm, urxvt and st are able to consume less memory
  than Zutty. In the second, more demanding test, only st remains
  stable and well under Zutty: urxvt is still below, but its resource
  use has apparently ballooned and its behaviour seems to be less
  stable.

- Compared to the "dumb test", the second test provokes Alacritty,
  Kitty, urxvt, as well as xterm to trigger many more page faults (a
  sign of memory allocations). st and gnome-terminal look more stable
  in this regard, with only slightly increased page fault counts.
  Zutty is practically unmoved by the difference in workload.


## Alacritty vs Zutty

It is readily apparent that Alacritty and Zutty are the two
consistently high-performing terminals; nothing else comes even close.
Alacritty has a higher variance in its (overall higher) memory usage.
Further, it seems that simpler workloads (less control sequences) will
favour Alacritty, while Zutty will be faster with more complex
workloads. Practically, both terminals are *blazing fast*, and the
difference between them is of no practical importance. (The same
cannot be said of their levels of VT support: VT102 and VT520 are
*very* different targets.)

As a trivial real-world test, I ran Alacritty and Zutty side-by-side,
in identically sized windows, started at the same time with identical
display settings (DejaVuSans Mono is the default font for Alacritty,
so I configured Zutty to use the same font). Two instances of the same
`htop` program, started at the same time, are running side-by-side,
periodically refreshing themselves at the same rate, producing output
based on the same system information. While the exact content of the
two windows is not strictly identical, it is safe to say that the two
terminals are tasked with virtually identical workloads.

![](/images/zutty/alacritty_vs_zutty.png){:width="100%"}  
*Zutty (left) vs Alacritty (right): a side-by-side CPU usage comparison*

After a good couple minutes, I captured the above screenshot. It shows
that Alacritty consumed 27.12 seconds of CPU time, while Zutty used
only 7.93 seconds: a difference of 3.4x in favour of Zutty.

Still, I would be hesitant to declare Zutty "the fastest terminal
emulator in existence". It might be more than three times faster than
Alacritty on this particular test, on my particular machine. But
terminals are diverse and complex, the computing stack they sit on top
of is even more complex, and performance measurements are extremely
tricky.


# Miscellaneous: Source code volume

There is widespread agreement on that *ceteris paribus*, less code is
better.

To count source code lines, [cloc](https://github.com/AlDanial/cloc)
is used. Only the main implementation languages for each program are
left in the table below (summed up into a single row per program),
everything else is discarded from the cloc output.

All source repositories were counted at the time of writing this
article (December 2020), taking into account the latest commit on
branch `master`.

In all fairness, at least some of the code volume counted for the
larger programs is probably test code plus maybe even generated code,
and I have made no effort to separate those parts from the
implementation itself. Still, cloc was run on a fresh clone of each
program, only counting files under version control.

Sorted by total LOC numbers:

program | main language | files | blank | comment | code | total | source/repo
:--- | --- | ---: | ---: | ---: | ---: | ---: | :---
st | C | 6 | 592 | 430 | 4,333 | 5,355 | [link](https://git.suckless.org/st)
Zutty | C++ | 25 | 768 | 429 | 4,808 | 6,005 | [link](https://github.com/tomszilagyi/zutty)
gnome-terminal | C | 61 | 3,938 | 2,748 | 16,198 | 22,884 | [link](https://gitlab.gnome.org/GNOME/gnome-terminal)
Alacritty | Rust | 65 | 3,784 | 2,601 | 18,026 | 24,411 | [link](https://github.com/alacritty/alacritty)
urxvt | C++ | 107 | 5,840 | 4,039 | 63,183 | 73,062 | [link](http://dist.schmorp.de/rxvt-unicode/rxvt-unicode-9.22.tar.bz2)
xterm | C | 66 | 8,449 | 11,935 | 81,300 | 101,684 | [link](https://invisible-island.net/datafiles/release/xterm.tar.gz)
Kitty | C, Python | 280 | 12,224 | 12,591 | 179,087 | 203,902 | [link](https://github.com/kovidgoyal/kitty)

<br>

The only other terminal on the order of Zutty's source code volume
(beating it by about 10%) is st. However, st is a far cry from Zutty
on all other accounts: it is only a VT102, comes with some obvious
correctness issues, and performance is comparatively worse.

Zutty is about 4x smaller than anything comparable in terms of VT
implementation scope (gnome-terminal) or performance (Alacritty); it
beats the former on performance and the latter on scope, and both of
them on correctness.

All of this only serves to demonstrate the power that lies in the
simplicity of [How Zutty works].


# Conclusion

Is Zutty the best, or fastest, or most *[insert favourite adjective]*
terminal in existence?  Yes, or no, or maybe, or (most likely) "it
depends". I am not making any such claims, and I don't care too much
about such badges of honor. There are many other differentiating
aspects and features of all the programs mentioned above; I have
barely scratched the surface here. As stated before, all terminals are
useful in some circumstances, and you should feel free to keep using
the one you already have, like, and trust. Or maybe [give Zutty a
try](/zutty/doc/USAGE.html), in case you have now gotten curious about
it.

[Zutty]:                   /zutty
[How Zutty works]:         /2020/11/How-Zutty-works
[VTTEST]:                  https://invisible-island.net/vttest/vttest.html
