package org.pale.simplechat.values;

import org.pale.simplechat.actions.ActionException;
import org.pale.simplechat.actions.Value;
import org.pale.simplechat.actions.Function;
import org.pale.simplechat.actions.BinopInstruction.Type;

public class FunctionValue extends Value {
    Function f;
    
    public FunctionValue(Function f){
        this.f = f;
    }
    
    public Function getFunction(){
        return f;
    }
    
    @Override public boolean equals(Object ob){
        return this==ob;
    }
    
    @Override public String str(){
        return f.getName();
    }
    @Override public Value binop(Type t, Value snd) throws ActionException {
        throw new ActionException("binary operations not valid for functions");
    }
}
