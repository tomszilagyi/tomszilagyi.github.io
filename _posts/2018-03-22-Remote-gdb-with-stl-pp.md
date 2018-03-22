---
layout: post
title:  "Remote gdb from MacOS X to Linux with C++ STL pretty-printing"
---

Using a Mac as your main workstation should not stop you from
debugging backend services that are (naturally) built and run on
Linux. In fact, you should be able to perform remote debugging via a
locally running gdb. One of the main motivators for getting this up
and running is the possibility of integrating gdb with your local IDE.

The below notes pertain to MacOS Sierra (10.12.6) and Red Hat / CentOS 7.
I suspect that they are applicable to other scenarios as well.

When we talk about remote debugging, we refer to the machine actually
running the debugged program (the **target**) as the **remote** side.
Conversely, the host we are running gdb on is the **local** side.

Remote debugging is made possible via a remote debugging stub that our
local gdb talks to; the stub acts as an intermediary to control the
target. The stub may be compiled into the target itself (usually done
on severely resource-constrained embedded platforms), but in "normal"
scenarios we will use a ready-made general purpose stub called
gdbserver.

## Preliminaries

1. Install Homebrew on your Mac and with it, the usual GNU dev stuff.
   [Here][homebrew-gnu-dev] is an excellent starting point.

2. Install gdb via homebrew:


   `$ brew install gdb --with-all-targets`

   *Note: The `--with-all-targets` option is important; without it, you
   won’t be able to debug on a remote machine with a different OS or
   architecture than your local machine ([source][brew-install-gdb]).*

3. Follow [these instructions][codesign-gdb] to codesign the gdb
   executable.

   *Note: You might get away with omitting this but **only** in case you
   only do remote debugging with this gdb, otherwise you will need this
   to take control of (i.e. debug) local processes running on your Mac.*

## Test program

We will use a minimal C++ test program to expose the problem and
verify the solution. Source listing of `main.cpp` below:

{% highlight C++ %}
#include <map>
#include <list>
#include <vector>
#include <string>

int main() {
    std::string s { "It ain't over 'til it's over" };
    std::vector<double> v(3, 3.141593);
    std::list<int> l;
    l.push_back(3);
    l.push_back(1);
    l.push_back(4);
    std::map<int,int> m;
    m[7] = 2;
    m[2] = -987;
    m[-9] = 5;
    return 0;   /* line 17 */
}
{% endhighlight %}

Compile it locally (on the target) with something like the below command:

    centos7$ g++ -g -Wall -std=c++14 -o test main.cpp

Now we can debug this as we normally would, if we were working locally on the target:

```
centos7$ gdb -q test
Reading symbols from test...done.
(gdb) break 17
Breakpoint 1 at 0x400c7f: file main.cpp, line 17.
(gdb) run
Starting program: /home/tom/src/dbgex/test
Breakpoint 1, main () at main.cpp:17
17          return 0;
(gdb) print s
$1 = "It ain't over 'til it's over"
(gdb) print v
$2 = std::vector of length 3, capacity 3 = {
  3.1415929999999999,
  3.1415929999999999,
  3.1415929999999999
}
(gdb) print l
$3 = std::list = {
  [0] = 3,
  [1] = 1,
  [2] = 4
}
(gdb) print m
$4 = std::map with 3 elements = {
  [-9] = 5,
  [2] = -987,
  [7] = 2
}
```

Note how we got nice pretty-printed output for C++ STL containers at no additional cost.

## Remote debugging

In real life, we might not want to, or be able to run gdb on the
target. Enter remote debugging. In this simple example, let's assume
that we can directly access the target host via raw TCP; in reality
we might have to complicate this by setting up some SSH tunneling
(a.k.a. port forwarding), but that is outside the scope of this
article (you can refer to [this tutorial][ssh-port-forwarding]).

On the remote side, we now launch the target via a remote stub
(gdbserver) that listens on a certain TCP port where we can connect
with gdb to control it.

```
centos7$ gdbserver :9999 ./test
Process ./test created; pid = 28782
Listening on port 9999
```

Note: gdbserver normally comes with gdb (it is part of the gdb source
tree); however, some Linux distributions package it separately. On
CentOS 7, it is available via the `gdb-gdbserver` package.

In our local gdb, we establish a connection to the remote target and
try to do the same sort of debugging:

```
macosx$ gdb -q
(gdb) target remote centos7:9999
Remote debugging using centos7:9999
Reading /home/tom/src/dbgex/test from remote target...
warning: File transfers from remote targets can be slow. Use "set sysroot"
to access files locally instead.
Reading /home/tom/src/dbgex/test from remote target...
Reading symbols from target:/home/tom/src/dbgex/test...done.
... output omitted ...
0x00007ffff7ddc170 in _start () from target:/lib64/ld-linux-x86-64.so.2
(gdb) break 17
Breakpoint 1 at 0x400c7f: file main.cpp, line 17.
(gdb) continue
Continuing.
Reading /lib64/libstdc++.so.6 from remote target...
... output omitted ...
warning: Breakpoint address adjusted from 0x7ffff7b31a50 to 0xf7b31a50.
Breakpoint 1, main () at main.cpp:17
17 return 0;
(gdb) print s
$1 = {static npos = <optimized out>,
 _M_dataplus = {<std::allocator<char>> = {<__gnu_cxx::new_allocator<char>> =
 {<No data fields>}, <No data fields>}, _M_p = 0x607028
 "It ain't over 'til it's over"}}
(gdb) print v
$2 = {<std::_Vector_base<double, std::allocator<double> >> = {
 _M_impl = {<std::allocator<double>> = {<__gnu_cxx::new_allocator<double>> =
 {<No data fields>}, <No data fields>}, _M_start = 0x607050, _M_finish =
 0x607068, _M_end_of_storage = 0x607068}}, <No data fields>}
(gdb) print l
$3 = {<std::_List_base<int, std::allocator<int> >> = {
 _M_impl = {<std::allocator<std::_List_node<int> >> =
 {<__gnu_cxx::new_allocator<std::_List_node<int> >> = {<No data fields>},
 <No data fields>}, _M_node = {_M_next = 0x607070, _M_prev = 0x6070b0}}},
 <No data fields>}
(gdb) print m
$4 = {_M_t = {
 _M_impl = {<std::allocator<std::_Rb_tree_node<std::pair<int const, int> > >>
 = {<__gnu_cxx::new_allocator<std::_Rb_tree_node<std::pair<int const, int> >
 >> = {<No data fields>}, <No data fields>},
 _M_key_compare = {<std::binary_function<int, int, bool>> =
 {<No data fields>}, <No data fields>},
 _M_header = {_M_color = std::_S_red, _M_parent = 0x607100,
 _M_left = 0x607130, _M_right = 0x6070d0},
 _M_node_count = 3}}}
```

Oh bloody fun! The pretty-printing we got for free in our local gdb is
now broken.

## Fixing the pretty-printer

The pretty-printer in gdb is implemented as a bunch of python modules
run as gdb extensions. We need to migrate that stuff from the remote
side (where gdb transparently found it) to our local side.

Let's look at the gdb datadir on both sides. This is visible as part
of the output of `show configuration` (if your gdb is recent enough).

Baseline on the Mac:

```
macosx$ gdb
GNU gdb (GDB) 8.1
Copyright (C) 2018 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law. Type "show copying"
and "show warranty" for details.
This GDB was configured as "x86_64-apple-darwin16.7.0".
Type "show configuration" for configuration details.
...
(gdb) show configuration
This GDB was configured as follows:
  configure --host=x86_64-apple-darwin16.7.0 --target=x86_64-apple-darwin16.7.0
            --with-auto-load-dir=:${prefix}/share/auto-load
            --with-auto-load-safe-path=:${prefix}/share/auto-load
            --with-expat
            --with-gdb-datadir=/usr/local/Cellar/gdb/8.1/share/gdb (relocatable)
            --with-jit-reader-dir=/usr/local/Cellar/gdb/8.1/lib/gdb (relocatable)
            --without-libunwind-ia64
            --with-lzma
            --with-python=/System/Library/Frameworks/Python.framework/Versions/2.7
            --without-guile
            --with-separate-debug-dir=/usr/local/Cellar/gdb/8.1/lib/debug (relocatable)
            --without-babeltrace
```

As seen above, *$gdb-datadir* is
`/usr/local/Cellar/gdb/8.1/share/gdb` and its content is:

```
macosx$ ls -R /usr/local/Cellar/gdb/8.1/share/gdb
python  syscalls  system-gdbinit

./python:
gdb

./python/gdb:
FrameDecorator.py   __init__.py   frames.pyc   printing.pyc  types.pyc
FrameDecorator.pyc  __init__.pyc  function     prompt.py     unwinder.py
FrameIterator.py    command       printer      prompt.pyc    xmethod.py
FrameIterator.pyc   frames.py     printing.py  types.py

./python/gdb/command:
__init__.py   frame_filters.py     prompt.py          unwinders.py
__init__.pyc  frame_filters.pyc    prompt.pyc         unwinders.pyc
explore.py    pretty_printers.py   type_printers.py   xmethods.py
explore.pyc   pretty_printers.pyc  type_printers.pyc  xmethods.pyc

./python/gdb/function:
__init__.py   as_string.py   caller_is.py   strfns.py
__init__.pyc  as_string.pyc  caller_is.pyc  strfns.pyc

./python/gdb/printer:
__init__.py  __init__.pyc  bound_registers.py  bound_registers.pyc

./syscalls:
aarch64-linux.xml  gdb-syscalls.dtd    mips-o32-linux.xml  s390-linux.xml
amd64-linux.xml    i386-linux.xml      ppc-linux.xml       sparc-linux.xml
arm-linux.xml      mips-n32-linux.xml  ppc64-linux.xml     sparc64-linux.xml
freebsd.xml        mips-n64-linux.xml  s390x-linux.xml

./system-gdbinit:
elinos.py  wrs-linux.py
```

On the remote side (note that we are using devtoolset-6 on our CentOS 7):

```
centos7$ gdb
GNU gdb (GDB) Red Hat Enterprise Linux 7.12.1-47.el7
...
(gdb) show configuration
This GDB was configured as follows:
  configure --host=x86_64-redhat-linux-gnu --target=x86_64-redhat-linux-gnu
            --with-auto-load-dir=$debugdir:$datadir/auto-load:/usr/share/gdb/auto-load
            --with-auto-load-safe-path=$debugdir:$datadir/auto-load:/usr/share/gdb/auto-load
            --with-expat
            --with-gdb-datadir=/opt/rh/devtoolset-6/root/usr/share/gdb
            --with-jit-reader-dir=/opt/rh/devtoolset-6/root/usr/lib64/gdb
            --without-libunwind-ia64
            --with-lzma
            --with-python=/usr
            --without-guile
            --with-separate-debug-dir=/usr/lib/debug
            --with-system-gdbinit=/opt/rh/devtoolset-6/root/etc/gdbinit
            --without-babeltrace
```

The *$gdb-datadir* is `/opt/rh/devtoolset-6/root/usr/share/gdb`. What
do we have in there?

```
centos7$ ls -R /opt/rh/devtoolset-6/root/usr/share/gdb
/opt/rh/devtoolset-6/root/usr/share/gdb:
auto-load  python  syscalls

/opt/rh/devtoolset-6/root/usr/share/gdb/auto-load:
bin  lib  lib64  sbin  usr

/opt/rh/devtoolset-6/root/usr/share/gdb/auto-load/usr:
bin  lib  lib64  sbin

/opt/rh/devtoolset-6/root/usr/share/gdb/auto-load/usr/bin:

/opt/rh/devtoolset-6/root/usr/share/gdb/auto-load/usr/lib:
libstdc++.so.6.0.19-gdb.py   libstdc++.so.6.0.19-gdb.pyo
libstdc++.so.6.0.19-gdb.pyc

/opt/rh/devtoolset-6/root/usr/share/gdb/auto-load/usr/lib64:
libstdc++.so.6.0.19-gdb.py   libstdc++.so.6.0.19-gdb.pyo
libstdc++.so.6.0.19-gdb.pyc

/opt/rh/devtoolset-6/root/usr/share/gdb/auto-load/usr/sbin:

/opt/rh/devtoolset-6/root/usr/share/gdb/python:
gdb  libstdcxx

/opt/rh/devtoolset-6/root/usr/share/gdb/python/gdb:
command             frames.py     printer       types.py      xmethod.pyc
FrameDecorator.py   frames.pyc    printing.py   types.pyc     xmethod.pyo
FrameDecorator.pyc  frames.pyo    printing.pyc  types.pyo
FrameDecorator.pyo  function      printing.pyo  unwinder.py
FrameIterator.py    __init__.py   prompt.py     unwinder.pyc
FrameIterator.pyc   __init__.pyc  prompt.pyc    unwinder.pyo
FrameIterator.pyo   __init__.pyo  prompt.pyo    xmethod.py

/opt/rh/devtoolset-6/root/usr/share/gdb/python/gdb/command:
explore.py         __init__.py          prompt.py          xmethods.py
explore.pyc        __init__.pyc         prompt.pyc         xmethods.pyc
explore.pyo        __init__.pyo         prompt.pyo         xmethods.pyo
frame_filters.py   pahole.py            unwinders.py
frame_filters.pyc  pahole.pyc           unwinders.pyc
frame_filters.pyo  pahole.pyo           unwinders.pyo
ignore_errors.py   pretty_printers.py   type_printers.py
ignore_errors.pyc  pretty_printers.pyc  type_printers.pyc
ignore_errors.pyo  pretty_printers.pyo  type_printers.pyo

/opt/rh/devtoolset-6/root/usr/share/gdb/python/gdb/function:
as_string.py   caller_is.pyc  __init__.pyo  strfns.py
as_string.pyc  caller_is.pyo  in_scope.py   strfns.pyc
as_string.pyo  __init__.py    in_scope.pyc  strfns.pyo
caller_is.py   __init__.pyc   in_scope.pyo

/opt/rh/devtoolset-6/root/usr/share/gdb/python/gdb/printer:
bound_registers.py   bound_registers.pyo  __init__.pyc
bound_registers.pyc  __init__.py          __init__.pyo

/opt/rh/devtoolset-6/root/usr/share/gdb/python/libstdcxx:
__init__.py  __init__.pyc  __init__.pyo  v6

/opt/rh/devtoolset-6/root/usr/share/gdb/python/libstdcxx/v6:
__init__.py   __init__.pyo  printers.pyc  xmethods.py   xmethods.pyo
__init__.pyc  printers.py   printers.pyo  xmethods.pyc

/opt/rh/devtoolset-6/root/usr/share/gdb/syscalls:
aarch64-linux.xml  freebsd.xml       ppc64-linux.xml  s390x-linux.xml
amd64-linux.xml    gdb-syscalls.dtd  ppc-linux.xml
arm-linux.xml      i386-linux.xml    s390-linux.xml
```

Observe that we have an `auto-load` directory under *$gdb-datadir* and a
`libstdcxx` directory under *$gdb-datadir*/python/gdb and these were
missing on our Mac. Move these over (with their recursive subtrees) to
the corresponding location under *$gdb-datadir* on our local host. Be
careful, there are relative symlinks under `auto-load`, so use a copy
method that preserves those. (I created tarballs that I could easily
transfer and then untar, but `rsync -avz` should be fine, too.)

### Loading the STL pretty-printers

It is not enough to have the libstdc++ pretty-printer code lying
around, we need to instruct gdb to load it on startup. Add this to
your `~/.gdbinit`:

```
python
import sys
sys.path.insert(0, '/usr/local/Cellar/gdb/8.1/share/gdb/python/libstdcxx')
from libstdcxx.v6.printers import register_libstdcxx_printers
register_libstdcxx_printers (None)
end
```

As you can probably tell, this is a piece of python extension code for
gdb (between `python`...`end`) that inserts the given path into
`sys.path` (so the succeeding statement will work), imports the
`register_libstdcxx_printers` function and then evaluates it. If your
directory setup is different, make sure to provide the correct path to
the `libstdcxx` directory.

## Verification

Start the remote stub as before via the same gdbserver command.
Perform the same remote debugging session. This time, the output
should be different:

```
macosx$ gdb -q
(gdb) target remote centos7:9999
Remote debugging using centos7:9999
...
0x00007ffff7ddc170 in _start () from target:/lib64/ld-linux-x86-64.so.2
(gdb) break 17
Breakpoint 1 at 0x400c7f: file main.cpp, line 17.
(gdb) continue
...
warning: Breakpoint address adjusted from 0x7ffff7b31a50 to 0xf7b31a50.
Breakpoint 1, main () at main.cpp:17
17 return 0;
(gdb) print s
$1 = "It ain't over 'til it's over"
(gdb) print v
$2 = std::vector of length 3, capacity 3 = {3.1415929999999999,
  3.1415929999999999, 3.1415929999999999}
(gdb) print l
$3 = std::list = {[0] = 3, [1] = 1, [2] = 4}
(gdb) print m
$4 = std::map with 3 elements = {[-9] = 5, [2] = -987, [7] = 2}
```

## Conclusion

This might seem fairly obscure and low-level, but it pays off in the
end. Sometimes it is just not possible to run a debugger directly on
the target; other times, it is merely a convenience issue: you can
drive your locally running gdb from an IDE that is also running on the
machine in front of you. The remote stub only implements the lowest
level of debugger functionality (hooking into a process and
controlling its execution, managing hardware breakpoints, watches
etc.), while all the "business knowledge" of debugging (interpreting
stack frames, pretty-printing data structures etc.) resides in the gdb
proper.

An obvious advantage of the remote debugging setup is that, as gdb
executes locally, you only need to set it up once and enjoy the
results with all remote targets you connect to. There are other
benefits, too, mostly if the target is a resource-constrained embedded
system: with remote debugging, you can run a stripped binary on the
target, while giving gdb local access to a binary compiled with
symbolic information.  However, if this is a deciding factor, you
probably won't even be able to run gdbserver and will have to
implement a compiled-in stub.


Further notes:

- It is often necessary to provide command line arguments to the
  program being debugged. You cannot do that directly when the program
  is run via gdb. When debugging locally, supply the arguments to the
  `run` command instead as you launch the executable in gdb. When
  debugging remotely, supply the arguments to gdbserver when setting
  up the remote stub, such as:

  `$ gdbserver :9999 ./test arg1 arg2 ...`

- We have not covered more conventional aspects of setting up a remote
  debugging session, such as providing gdb access to the remote
  application's source code and binaries (including libs). These are
  of course necessary, otherwise even simple things (e.g. the `list`
  command in gdb) won't work. The usual solution is a filesystem
  shared over the network, customarily via NFS. Refer to the gdb
  commands `directory` and `set sysroot` to set this up.

- Observant readers may have noticed that the local and remote
  machines run a different version of gdb (and gdbserver): 7.12 vs
  8.1. This is not a problem as long as they speak the same gdb remote
  protocol, which is remarkably stable.


[homebrew-gnu-dev]:     https://www.topbug.net/blog/2013/04/14/install-and-use-gnu-command-line-tools-in-mac-os-x/
[brew-install-gdb]:     https://medium.com/@spe_/debugging-c-c-programs-remotely-using-visual-studio-code-and-gdbserver-559d3434fb78
[codesign-gdb]:         https://sourceware.org/gdb/wiki/BuildingOnDarwin
[ssh-port-forwarding]:  /2008/03/More-Robust-Remote-X-Tunneling
