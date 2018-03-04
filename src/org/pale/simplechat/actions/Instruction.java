package org.pale.simplechat.actions;

import java.lang.reflect.InvocationTargetException;

import org.pale.simplechat.Conversation;

public interface Instruction {
	void execute(Conversation c) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ActionException;
}
