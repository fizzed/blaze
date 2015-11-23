Blaze by Fizzed
=======================================

## Why Blaze?

Here's why we took the time to build this. Other than having a JVM on the machine,
that's all that's required to run a script.  All dependencies will be downloaded
and cached at runtime.  We also wanted to have great IDE support while writing
scripts.  Java and many JVM languages have fantastic IDE integration. 

Let's see why you'd consider Blaze against some of the more common options...

 - Shell: First, worry about which shell is installed.  Bash is fairly universal
   these days, but that's not always the case.  For running a couple commands,
   shell is great. Outside of that it's awful (e.g. try processing a directory
   of files that have spaces in the name).  Most importantly its not designed
   to be portable and your users will need to have all the *right* executables
   installed on their machine.

 - Python: Clearly the most popular choice for system-focused engineers. First,
   worry about which version of Python is installed.  There are whole debates on
   whether you should [use Python 3 vs 2](https://wiki.python.org/moin/Python2orPython3).
   Then you'll need to worry about the 3rd party packages that scripts use -- and
   potentially the versions of those packages.  A lot of packages also wrap
   native system libraries -- which create their own portability issues.

 - Ruby/Rakefile: Similar issues to Python. Which version of Ruby?  And you'll 
   need to worry about installing the dependencies.  Many of those 3rd party
   modules simply wrap native system libraries -- which create their own issues.

 - Node/Grunt/Gulp: You'll need to install Node, worry about the version of it,
   and also install any 3rd party modules, and then worry about the version of
   those as well.  The ecosystem of 3rd party, stable, mature libraries is 
   orders of magnitude smaller than Java.

 - Fantom/Scala/Kotlin/Nashorn: All great languages and many of them already support
   running themselves as scripts.  They all require the user to download the
   language & compiler and make it available on the command-line though. Plus, they don't
   solve the dependency issue -- the fact is to do anything meaningful,
   you'll want to use 3rd party libraries at runtime. Blaze helps to *bootstrap*
   running your script in these languages and APIs for doing scripting tasks
   like spawning other processes, globbing files, etc.

 - SBT/Ant/Maven/Gradle/Makefile: They sorta work, but they are designed for
   compiling projects.  Doing anything *scripty* ends up either being awkward or 
   downright maddening.  Plus, you'll still need your user to have them installed
   on their machine.

 - Groovy: Groovy has the @Grab feature to download dependencies, so it's probably
   the closest to Blaze.  However, you'll still need to have your user install
   Groovy, make it available on the command-line, and worry about which version
   of Groovy they have installed.  Troubleshooting and modifying how @Grab works
   is pretty ugly too.