AIML is terribly verbose, and writing complex conversational structures
is a nightmare. Yes, you can do it - and it's really powerful - but
the XML nature of everything and the way topics work really burns my
brain. Chatscript is easier, much easier, but it's in C++. And my essential
usecase is Minecraft chatbots, so I can't use that. 
So this is a simplified (hah) thing. This documentation is really 
just for me, at the moment.

## Bots
- The primary object is a `Bot`, which has a set of topics (see below)
- Each Bot can have multiple `BotInstance` objects, which are a single
speaker.

## Creating a bot
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

## Regex substitutions
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

## Topics
All bots load a `main.topic` file from their directory. This
may include other topic files with an `include` line.
The topic file must start with its name in a `name` line,
and consists of pattern/action pairs.
Each topic is run in descending priority order, and tries to match
its patterns in turn. When it matches a pattern, it runs the action
associated. If a "special" set of pattern/action pairs is in operation
(such as returned from an action subpattern, see below) that will
be tried first.

Initially all topics are at the same priority, so matching can be
a bit random. Priorities are set with a `priority` command within
a topic file. Here's a topic file:

```
name main
priority 1000

# this is a named pattern/action pair. The string is the the pattern,
# the bit between it and the semicolon is the action. This one stacks
# the output "Hi, how are you?", and then sets up a subpattern tree
# and tells the system to use it to parse responses to this output.

+hellopattern "([hello hi] .*)"
    "hi how are you?"
    {
        # each subpattern is a pattern/action pair.
        # the pattern is this bit...

        "([good fine well] .*)"

            # and this is the action, which just stacks an output
            "Glad to hear it.";

        "([bad (not too)] .*)"
            "Oh, I'm sorry";
    } next;
    
# this anonymous pattern catches everything, and runs when nothing
# else in the topic has matched. It captures the input as "$foo"
# and this gets used to generate the output.

+"$foo=.*"
    "I don't know how to respond to " $foo +;
```


## Patterns
For matching, the input is lower-cased, all punctuation is removed
and finally it is split into words. Pattern matching is done per-word.
The entire pattern must be in a pair of quotes. Most patterns
will be sequences, so you'll see a lot of `"(...)"`.

### pattern elements
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

### Star gotchas
A pattern like `(bar foo)+ bar` may cause problems, because when presented
with a string like "bar foo bar" immediately match the end token (bar)
and so fail. Make sure your end pattern is not the start of a star sequence
pattern. I'm sure there's a clever way around this.

### Other gotchas
- Negate nodes are "fun".

### Labels
Putting `$labelname=` before a pattern element marks it so that
the data it matches will be stored in a variable. In the case of '*' and
'+', the variable `$labelname_ct` is set to the match count.


### Examples
```
$all=(hello $name=.+)
(foo* hello $name=.*)
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
- binops : for manipulating numbers and concatenating strings

