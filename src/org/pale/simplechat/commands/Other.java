package org.pale.simplechat.commands;

import java.lang.reflect.InvocationTargetException;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Cmd;
import org.pale.simplechat.actions.Function;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.values.StringValue;
import org.pale.simplechat.values.FunctionValue;

public class Other {
    // (string -- ) call a named function (which itself might manipulate the stack a lot more!)
    @Cmd public static void call(Conversation c) throws ActionException {
        Value v = c.pop();
        Function f;
        if(v instanceof StringValue){
            String s = v.str();
            f = c.instance.bot.getFunc(s);
            if(f==null)
                throw new ActionException("bot does not declare function "+s);
        } else if(v instanceof FunctionValue) {
            f = ((FunctionValue)v).getFunction();
        } else
            throw new ActionException("value for 'call' must be a string or function");
        try {
            f.run(c);
        } catch (IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException e) {
            throw new ActionException("Error in "+v.str()+" -  "+e.toString());
        }
    }
}
