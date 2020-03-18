package org.pale.chattest;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.StringValue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Stubs for minecraft stuff
 */
public class Stubs {
    // mctime (string -- timestring) get minecraft time. Input is digital,approx or todstring; anything else gives minecraft ticks.
    @Cmd
    public static void mctime(Conversation c) throws ActionException {
        String type = c.popString();
        c.push(new StringValue("morning"));
    }

    static Instant startTime = Instant.now();

    // get REAL time in seconds since server boot (well, plugin start)
    @Cmd public static void realnow(Conversation c) throws ActionException {
        long t = ChronoUnit.SECONDS.between(startTime, Instant.now());
        c.push(new IntValue((int)t));
    }

    // get BUKKIT TIME in ticks for the NPC's world
    @Cmd public static void now(Conversation c) throws ActionException {
        c.push(new IntValue(1000));
    }

    // broadcast (string --) write a message directly to all players. Useful in debugging, can be used inside a timer.
    @Cmd public static void broadcast(Conversation c) throws ActionException {
        String msg = c.popString();
        System.out.println(msg);
    }

    // rain (-- boolean 1 or 0) is it raining/snowing
    @Cmd public static void rain(Conversation c) throws ActionException {
        c.push(new IntValue(0));
    }


    // take (count itemname -- result) attempt to move items from the player's main hand, typically
    // from a RIGHTCLICK event.
    // Results: NOTENOUGH (player doesn't have the number we requested)
    //          UNKNOWN (couldn't parse the itemname specified into a minecraft item id)
    //          NOITEM (player tried to give me nothing, i.e. was holding air)
    //          WRONG (player tried to give the wrong item)
    //			OK ( everything worked)
    // Note that this does not add items to the bot, it just removes them from the player!
    @Cmd public static void take(Conversation c) throws ActionException {
        String itemName = c.popString();
        int count = c.pop().toInt();

        c.push(new StringValue("OK"));
    }


    // give (count itemname -- result) attempt to add items to the player. Does not remove items from the bot.
    // If there's no room in the player's inventory, the items will be put on the ground.
    // Results: UNKNOWN (couldn't parse the itemname specified into a minecraft item id)
    //			OK ( everything worked)
    //
    @Cmd public static void give(Conversation c) throws ActionException {
        String itemName = c.popString();
        int count = c.pop().toInt();

        c.push(new StringValue("OK"));
    }

    @Cmd public static void itemheld(Conversation c) throws ActionException {

    }

    // matname (string -- string) convert a material name to a standard Minecraft name (or none)
    @Cmd public static void matname(Conversation c) throws ActionException {
        String name = c.popString();
        c.push(new StringValue(name));
    }

    // addtimer (seconds name -- id) add a timer function, throws exception if no func exists
    @Cmd public static void addtimer(Conversation c) throws ActionException {
        String name = c.popString();
        int interval = c.pop().toInt();
        c.push(new IntValue(-1));
    }

    // removetimer (id --) remove a timer
    @Cmd public static void removetimer(Conversation c) throws ActionException {
        int id = c.pop().toInt();
    }

    // json (string -- jsonbuilder) create a new JSON chat text with an initial element
    @Cmd public static void json(Conversation c) throws ActionException {
    }

    // jsoncol (jsonbuilder colorname -- jsonbuilder) tint the last added item
    @Cmd public static void jsoncol(Conversation c) throws ActionException {
        String col = c.popString().toLowerCase();
    }

    // jsonbold (jsonbuilder bool -- jsonbuilder) turn bold on/off
    @Cmd public static void jsonbold(Conversation c) throws ActionException {
        boolean on = c.popBoolean();
    }

    // jsonitalic (jsonbuilder bool -- jsonbuilder) italic on/off
    @Cmd public static void jsonitalic(Conversation c) throws ActionException {
        boolean on = c.popBoolean();
    }

    // jsonclick (jsonbuilder text -- jsonbuilder) make the last added item clickable, which will say the thing
    @Cmd public static void jsonclick(Conversation c) throws ActionException {
        String txt = c.popString().toLowerCase();
    }



    //  sendjson (jsonbuilder --) sends a JSON chat text, but only to a player
    @Cmd public static void jsonsend(Conversation c) throws ActionException {
        Value v = c.pop();
    }

    // lastseen (playername -- time) return how long ago (in mins) a player was seen, or -1.
    @Cmd public static void lastseen(Conversation c) throws ActionException {
        String name = c.popString();
        c.push(new IntValue(200));
    }
}