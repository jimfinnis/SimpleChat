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
import org.pale.simplechat.actions.Flow.JumpInstruction;

/**
 * An action to be performed when a pattern is matched.
 * @author white
 *
 */
public class Action {

	// limit on how many instructions can run
	private static final int LIMIT = 10000;
	
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
		
		Stack<Integer> cstack = new Stack<Integer>(); // compile stack for flow control
		// this is the current leave list - a list of the offsets of leave instructions to be resolved when a loop ends 
		List<Integer> leaveList = null;
		// this is a stack of leave lists
		Stack<List<Integer>> loopStack = new Stack<List<Integer>>();
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
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new TopicSyntaxException("expected a varname after $");
				insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.PATVAR));
				break;
			case '?':
				switch(tok.nextToken()){
				case StreamTokenizer.TT_WORD:
					insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.CONVVAR));
					break;
				case '@':
					if(tok.nextToken()!=StreamTokenizer.TT_WORD)
						throw new TopicSyntaxException("expected a varname after ?@");
					insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.INSTVAR));
					break;
					default: throw new TopicSyntaxException("expected a varname or sigil after ?");
				}
				break;
			case '!':
				if(tok.nextToken()=='=')
					insts.add(new BinopInstruction(BinopInstruction.Type.NEQUAL));
				else {
					tok.pushBack();
					switch(tok.nextToken()){
					case StreamTokenizer.TT_WORD:
						insts.add(new SetVarInstruction(tok.sval,SetVarInstruction.Type.CONVVAR));
						break;
					case '@':
						if(tok.nextToken()!=StreamTokenizer.TT_WORD)
							throw new TopicSyntaxException("expected a varname after !@");
						insts.add(new SetVarInstruction(tok.sval,SetVarInstruction.Type.INSTVAR));
						break;
					default: throw new TopicSyntaxException("expected a varname or sigil after !");
					}
				}
				break;
			case StreamTokenizer.TT_NUMBER:
				// this is a real pain, but there's no way of knowing whether the tokeniser got (say) 2 or 2.0.
				if(tok.nval == Math.floor(tok.nval))
					insts.add(new LiteralInstruction(new Value((int)tok.nval)));
				else
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
				// "if .. (else ..) then" handling
				if(tok.sval.equals("if")){
					cstack.push(insts.size()); // remember where we are..
					insts.add(new Flow.IfInstruction()); // .. and compile an IF to fixup later
				} else if(tok.sval.equals("else")){
					int ref = cstack.pop(); // pop the IF..
					if(ref<0)throw new TopicSyntaxException("'else' matching with 'cases'?");
					resolveJumpForwards(ref,1); // .. and resolve it to jump to just past here
					cstack.push(insts.size()); // push where we are
					insts.add(new Flow.JumpInstruction()); // and compile a jump to the end
				} else if(tok.sval.equals("then")){
					int ref = cstack.pop(); // pop the IF or ELSE location
					if(ref<0)throw new TopicSyntaxException("'then' matching with 'cases'?");
					resolveJumpForwards(ref,0); // and resolve it to jump here (there is no THEN instruction)
					
				// "loop..endloop" and leave handling
				} else if(tok.sval.equals("loop")){
					insts.add(new Flow.LoopStartInstruction()); // compile a loop start
					cstack.push(insts.size()); // remember the instruction after the loop start point
					loopStack.push(leaveList); // remember the current leave list (which might be null)
					leaveList = new ArrayList<Integer>(); // create a new leave list for this loop
				} else if(tok.sval.equals("endloop")){
					if(leaveList==null)throw new TopicSyntaxException("endloop when not in a loop");
					// iterate through the leave list, fixing up the jumps
					for(int leaveOffset : leaveList){
						// resolve the leave to point to just after the jump we're about to compile
						resolveJumpForwards(leaveOffset, 1);
					}
					// pop the leave list.
					leaveList = loopStack.pop();
					// now compile the jump back to the loop start, using the value stacked on the cstack.
					insts.add(new Flow.JumpInstruction(cstack.pop() - insts.size()));
				} else if(tok.sval.equals("leave")){
					if(leaveList==null)throw new TopicSyntaxException("leave when not in a loop");
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.LeaveInstruction());
				} else if(tok.sval.equals("ifleave")){
					if(leaveList==null)throw new TopicSyntaxException("ifleave when not in a loop");
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.IfLeaveInstruction());
					
				// "cases {..if..case} {..if..case} {.. otherwise}" handling
				// we construct a linked list of case jumps through the offset pointer of the jumps,
				// terminated by the end list marker -1.
				} else if(tok.sval.equals("cases")){
					cstack.push(-1); // push the end list marker - the first "case" will pop it.
				} else if(tok.sval.equals("case")){
					int ref = cstack.pop(); // get the corresponding "if"
					if(ref<0)throw new TopicSyntaxException("'case' should have an 'if'");
					resolveJumpForwards(ref,1); // resolve the "if"
					ref = cstack.pop(); // this will pop the marker the first time round, then the next thing in the list.
					cstack.push(insts.size()); // push the location we're about to write..
					// .. which is a specially marked jump to be fixed up (analogous to OP_DUMMYCASE in Angort)
					Flow.JumpInstruction j = new Flow.JumpInstruction(ref); // "offset" is actually index of next item in liked list
					j.isCaseJump = true;
					insts.add(j);
				} else if(tok.sval.equals("otherwise")){
					// pop the first case jump
					int ref = cstack.pop();
					while(ref>=0){
						// follow the list made through case jumps
						// make sure it's a jump, and the right kind of jump
						if(!(insts.get(ref) instanceof Flow.JumpInstruction))
							throw new TopicSyntaxException("bad case construction");
						Flow.JumpInstruction j = (Flow.JumpInstruction)insts.get(ref);
						if(!j.isCaseJump)
							throw new TopicSyntaxException("bad case construction");
						int next = j.offset; // get the next location in the list.
						j.isCaseJump = false; // turn it back into an ordinary jump
						resolveJumpForwards(ref,0); // resolve the jump destination
						ref = next; // get the next jump to resolve.
					}
					
				// "stop" and other quick flow control stuff
				} else if(tok.sval.equals("stop")){
					insts.add(new Flow.StopInstruction());
				}
				// TODO if .. else .. then and loops!
				else if(cmds.containsKey(tok.sval)){
					insts.add(new MethodCallInstruction(tok.sval,cmds.get(tok.sval)));
				} else
					throw new TopicSyntaxException("cannot find action cmd: "+tok.sval);
			}
		}
		
		// flow control termination checks
		if(leaveList!=null)throw new TopicSyntaxException("loop left unclosed");
		if(!cstack.isEmpty())throw new TopicSyntaxException("flow control statement left unclosed");
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
		c.exitflag = false;
		c.reset(); // reset runtime
		int totalinsts = 0;
		while(i<insts.size() && !c.exitflag){
			ActionLog.write("Running instruction at "+i+": "+insts.get(i).getClass().getSimpleName());

			// each instruction returns the next execution address's offset (usually 1!)
			i += insts.get(i).execute(c);
			if(totalinsts++ > LIMIT)throw new ActionException("Instruction limit exceeded");
		}
	}
}
