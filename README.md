# Patterns
## pattern elements
- plain words match themselves
- `^` negates the next pattern
- `[..]` matches any of the included patterns
- `(..)` matches all the included patterns in sequence
- `..*` matches anything (including nothing) until the previous pattern has a match;
it always succeeds
- `?..` matches the next pattern, but carries on if it fails
- `..+` matches at least one token until the previous pattern has a match;
- `^` negates the following pattern, but does not consume - it should be followed by what you want in that place.
A common pattern might be `^cat .` which will match "not a cat"

## Star gotchas
A pattern like `(bar foo)+ bar` may cause problems, because when presented
with a string like "bar foo bar" immediately match the end token (bar)
and so fail. Make sure your end pattern is not the start of a star sequence
pattern. I'm sure there's a clever way around this.

## Other gotchas
- Negate nodes are "fun".

## Labels
Putting `$labelname=` before a pattern element marks it so that
the data it matches will be stored in a variable. In the case of '*' and
'+', the variable `$labelname_ct` is set to the match count.


## Examples
```
$all=(hello $name=.+)
(foo* hello $name=.*)
```

# Topic patterns and actions
Top-level files are topic files, which must start with a name. They then
have a list of pattern/action pairs, each preceded by `+` which may
be followed by a name and a pattern string, or just a pattern string.
The pattern is a quoted string (almost always a sequence), and the action
is a set of stuff (see below) with a semicolon after. Here's a topic file with
one pattern:
```
name main

+hellopattern "([hello hi] .*)"
    "hi how are you?"
    {
        "([good fine well] .*)"
            "Glad to hear it.";
        "([bad (not too)] .*)"
            "Oh, I'm sorry";
    } next;
+".*"
```



# Actions
These are in the form of a sequence of instructions in an RPN language,
which should always leave a string on the stack. They are always terminated
by a semicolon. The simplest is just a string:
```
+([hello hi] $name=.*)
"Hi, how are you";
```
One special and complex instruction is an entire set of subpatterns and
actions. When these are set using the `next` command, the conversation will
try these patterns first. They are pattern/action pairs as normal, but
defined in curly brackets:
```
+pat "([hello hi] .*)"
    "hi how are you?"
    {
        "([good fine well] .*)"
            "Glad to hear it.";
        "([bad (not too)] .*)"
            "Oh, I'm sorry";
    }
```
Other actions:
- `?var` : get a conversation variable
- `$var` : get a pattern variable (i.e. one matched by a label in the pattern)

# Regex substitutions
Each bot can have a file (or set of files) containing regex substitutions
associated with it. These will be processed before any other input,
and are always processed. They are typically used to substitute
things like "I'm" and "I am" with "IAM" to make parsing easier.
Multiple bots can share substitution sets. The format for the files is
lines consisting of a regex and a replacement string, separated by default
by a colon. Two directives exist, which should be on their own lines.
The `#include` directive has a file argument and will include a file
of substitutions. The `#sep` directive has a string (actually regex)
argument and changes the separator for this file. The argument is separated
by a space. All other `#` lines are comments.

# Creating a bot
Typically goes like this:
```java
Substitutions subs = new Substitutions(Paths.get("/home/white/testbot"),"subs.subs");
Bot b = new Bot(Paths.get("/home/white/testbot"),subs);
instance = new BotInstance(b);
source = new Source();
```
The `Source` object is something to associate a conversation with.
It will typically be embedded in some entity in your code. It has
a `receive` method to override if you wish, but typically a conversation
is done by repeatedly calling
```java
String reply = instance.handle(string,source);
```
