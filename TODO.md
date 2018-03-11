# Todo
- pattern matching case keep
- categories
- global categories and functions (i.e. shared by all bots)
- loop iterator
- pattern "subroutines"?
- try to write Shopkeeper and Soldier

## Global conf notes
Have a set of files in the top level.
The `global.conf` file is very similar to the bot config file, but the
topic lists are dealt with differently.

### Substitutions
These have the same `subs <filename>` format in the config file, and are
read from the root directory. Within each bot, a `subs global` line
is required to put the substitutions into the bot. This means that you
can run the global substitutions at any point. For example:
----
subs "basic.subs"
subs "myspecialsubs.com"
subs global
----
will link in the global substitutions to run last, immediately before
topic matching.

### Init actions
The global init action always runs before the bots own init actions.

### Functions
Global functions can be replaced - overridden - by bot functions of
the same name. This is not true within a bot; attempting to redefine
a bot function will result in an error.

### Topics
This is the biggest syntactic difference between global and bot config
files: global topic lists are named, for example:
----
topics top {main test}
topics bottom {bottom}
----
rather than the bot config's
----
topics {main test}
topics {bottom}
----
To use these global topic lists, a bot needs to put them into its
own config file with a `topics global` command thus:
----
topics {main test}
topics global top
topics {bottom}
topics global bottom
----
The ordering of the commands allows the global topics to be at different
priorities for different bots. Promoting and demoting, enabling and
disabling, all work the same way as for bot-local topics, and as in
bot-local topics, they work on a per-instance basis.

