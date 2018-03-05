package org.pale.simplechat.actions;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;
import org.pale.simplechat.Pattern;
import org.pale.simplechat.Pair;
import org.pale.simplechat.PatternParseException;
import org.pale.simplechat.TopicSyntaxException;

/**
 * An action to be performed when a pattern is matched.
 * @author white
 *
 */
public class Action {

	static TreeMap<String,Method> cmds = new TreeMap<String,Method>();
	static {
		register(Action.class);
	}
	
	// call this to register new commands!
	public static void register(Class<?> c){
		// register @cmd methods
		for(Method m: Commands.class.getDeclaredMethods()){
			Cmd cmd = m.getAnnotation(Cmd.class);
			if(cmd!=null){
				Class<?> params[] = m.getParameterTypes();
				if(params.length!=1 || !params[0].equals(Conversation.class))
					throw new RuntimeException("Bad @Cmd method : "+m.getName());
				String name = cmd.name();
				if(name.equals(""))name = m.getName();
				cmds.put(name,m);
				Logger.log("registered "+name);
			}
		}		
	}

	private List<Instruction> insts = new ArrayList<Instruction>();


	public Action(StreamTokenizer tok) throws TopicSyntaxException, IOException, PatternParseException {
		// here we go, the action parser. This builds an executable list of instructions which operate
		// on a stack.
		
		Stack<Integer> cstack = new Stack<Integer>();

		for(;;){
			int t = tok.nextToken();
			if(t == ';')
				break;
			switch(t){
			case '\"':
			case '\'':
				insts.add(new LiteralInstruction(new Value(tok.sval)));
				break;
			case '$':
			case '?':
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new TopicSyntaxException("expected a varname after $ or ?");
				insts.add(new GetVarInstruction(tok.sval,
						t == '$' ? GetVarInstruction.Type.PATVAR : GetVarInstruction.Type.CONVVAR));
				break;
			case StreamTokenizer.TT_NUMBER:
				insts.add(new LiteralInstruction(new Value(tok.nval)));
				break;
			case '+':
				insts.add(new BinopInstruction(BinopInstruction.Type.ADD));
				break;
			case '-':
				insts.add(new BinopInstruction(BinopInstruction.Type.SUB));
				break;
			case '*':
				insts.add(new BinopInstruction(BinopInstruction.Type.MUL));
				break;
			case '/':
				insts.add(new BinopInstruction(BinopInstruction.Type.DIV));
				break;
			case '%':
				insts.add(new BinopInstruction(BinopInstruction.Type.MOD));
				break;
			case '{': // parse a subpattern list to deal with responses!
				insts.add(new LiteralInstruction(new Value(parseSubPatterns(tok))));
				break;
			case '!':
				if(tok.nextToken()=='=')
					insts.add(new BinopInstruction(BinopInstruction.Type.NEQUAL));
				else {
					tok.pushBack(); // we'll use the next token for something, but not yet
					throw new TopicSyntaxException("!= badly formed");
				}
				break;
			case '=':
				insts.add(new BinopInstruction(BinopInstruction.Type.EQUAL));
				break;
			case '>':
				if(tok.nextToken()=='=')
					insts.add(new BinopInstruction(BinopInstruction.Type.GE));
				else {
					tok.pushBack();
					insts.add(new BinopInstruction(BinopInstruction.Type.GT));
				}
				break;
			case '<':
				if(tok.nextToken()=='=')
					insts.add(new BinopInstruction(BinopInstruction.Type.LE));
				else {
					tok.pushBack();
					insts.add(new BinopInstruction(BinopInstruction.Type.LT));
				}
				break;
					
				
			case StreamTokenizer.TT_WORD:
				if(tok.sval.equals("if")){
					cstack.push(insts.size());
					insts.add(new Flow.IfInstruction());
				} else if(tok.sval.equals("else")){
					int refToIf = cstack.pop();
					resolveJumpForwards(refToIf,1);
					cstack.push(insts.size());
					insts.add(new Flow.JumpInstruction());
				} else if(tok.sval.equals("then")){
					int ref = cstack.pop();
					resolveJumpForwards(ref,0);
				} else if(tok.sval.equals("loop")){
					// TODO loops
				} else if(tok.sval.equals("endloop")){
					// TODO loops
				} else if(tok.sval.equals("leave")){
					// TODO loops
				} else if(tok.sval.equals("ifleave")){
					// TODO loops					
				}
				// TODO if .. else .. then and loops!
				else if(cmds.containsKey(tok.sval)){
					insts.add(new MethodCallInstruction(tok.sval,cmds.get(tok.sval)));
				} else
					throw new TopicSyntaxException("cannot find action cmd: "+tok.sval);
			}
		}
	}

	/// used to fix up an existing jump instruction to jump to the current instruction
	/// with an offset. 
	private void resolveJumpForwards(int refToIf, int i) throws TopicSyntaxException {
		insts.get(refToIf).setJump((insts.size()-refToIf)+i);
	}

	/**
	 * Parse subpatterns to match after this response.
	 * Syntax is { "pattern string" action... ; "pattern string" action...; }
	 * We have already parsed the opening. Naturally can nest.
	 * @param tok
	 * @throws TopicSyntaxException 
	 * @throws IOException 
	 * @throws PatternParseException 
	 */
	private List<Pair> parseSubPatterns(StreamTokenizer tok) throws TopicSyntaxException, IOException, PatternParseException {
		List<Pair> subpatterns = new ArrayList<Pair>();
		for(;;){
			int tt = tok.nextToken();
			if(tt=='}')break;
			if(tt != '\"' && tt != '\'')
				throw new TopicSyntaxException("error in parsing subpattern, expected a pattern string");
			Pattern pat = new Pattern(null,tok.sval);
			Action act = new Action(tok);
			Pair p = new Pair(pat,act);
			subpatterns.add(p);
		}
		return subpatterns;
	}

	public void run(Conversation c) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ActionException {
		int i=0; // instruction number (there might be jumps, see)
		while(i<insts.size()){
			Logger.log("Running instruction at "+i+": "+insts.get(i).getClass().getSimpleName());

			// each instruction returns the next execution address's offset (usually 1!)
			i += insts.get(i).execute(c);
		}
	}
}
