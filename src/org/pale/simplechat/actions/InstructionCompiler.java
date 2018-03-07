package org.pale.simplechat.actions;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.pale.simplechat.BotConfigException;
import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;
import org.pale.simplechat.Pair;
import org.pale.simplechat.Pattern;
import org.pale.simplechat.PatternParseException;
import org.pale.simplechat.values.DoubleValue;
import org.pale.simplechat.values.IntValue;
import org.pale.simplechat.values.StringValue;
import org.pale.simplechat.values.SubPatValue;

/**
 * This class compiles a stream of instructions.
 * @author white
 *
 */
public class InstructionCompiler {
	// when we've finished the constructor this will have useful data
	List<Instruction> insts = new ArrayList<Instruction>();
	
	
	private Stack<Integer> cstack = new Stack<Integer>(); // compile stack for flow control
	// this is the current leave list - a list of the offsets of leave instructions to be resolved when a loop ends 
	private List<Integer> leaveList = null;
	// this is a stack of leave lists
	private Stack<List<Integer>> loopStack = new Stack<List<Integer>>();


	/// map of functions we might use,
	private static Map<String, Function> funcMap = new HashMap<String,Function>();
	
	private static Map<String,Method> cmds = new HashMap<String,Method>();
	static {
		register(Commands.class);
	}
	
	// call this to register new commands!
	public static void register(Class<?> c){
		// register @cmd methods
		for(Method m: c.getDeclaredMethods()){
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


	InstructionCompiler(StreamTokenizer tok) throws IOException, BotConfigException, PatternParseException{
		for(;;){
			int t = tok.nextToken();
			if(t == ';')
				break;
			switch(t){
			case '\"':
			case '\'':
				insts.add(new LiteralInstruction(new StringValue(tok.sval)));
				break;
			case '$':
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new BotConfigException("expected a varname after $");
				insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.PATVAR));
				break;
			case '?':
				switch(tok.nextToken()){
				case StreamTokenizer.TT_WORD:
					insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.CONVVAR));
					break;
				case '@':
					if(tok.nextToken()!=StreamTokenizer.TT_WORD)
						throw new BotConfigException("expected a varname after ?@");
					insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.INSTVAR));
					break;
					default: throw new BotConfigException("expected a varname or sigil after ?");
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
							throw new BotConfigException("expected a varname after !@");
						insts.add(new SetVarInstruction(tok.sval,SetVarInstruction.Type.INSTVAR));
						break;
					default: throw new BotConfigException("expected a varname or sigil after !");
					}
				}
				break;
			case StreamTokenizer.TT_NUMBER:
				// this is a real pain, but there's no way of knowing whether the tokeniser got (say) 2 or 2.0.
				if(tok.nval == Math.floor(tok.nval))
					insts.add(new LiteralInstruction(new IntValue((int)tok.nval)));
				else
					insts.add(new LiteralInstruction(new DoubleValue(tok.nval)));
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
				insts.add(new LiteralInstruction(new SubPatValue(parseSubPatterns(tok))));
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
					
			case '[':
				insts.add(new Lists.NewListInstruction());
				if(tok.nextToken()!=']')tok.pushBack(); // skip ] in [].
				break;
			case ']':
			case ',':
				insts.add(new Lists.AppendInstruction());
				break;
			case StreamTokenizer.TT_WORD:
				// "if .. (else ..) then" handling
				if(tok.sval.equals("if")){
					cstack.push(insts.size()); // remember where we are..
					insts.add(new Flow.IfInstruction()); // .. and compile an IF to fixup later
				} else if(tok.sval.equals("else")){
					int ref = cstack.pop(); // pop the IF..
					if(ref<0)throw new BotConfigException("'else' matching with 'cases'?");
					resolveJumpForwards(ref,1); // .. and resolve it to jump to just past here
					cstack.push(insts.size()); // push where we are
					insts.add(new Flow.JumpInstruction()); // and compile a jump to the end
				} else if(tok.sval.equals("then")){
					int ref = cstack.pop(); // pop the IF or ELSE location
					if(ref<0)throw new BotConfigException("'then' matching with 'cases'?");
					resolveJumpForwards(ref,0); // and resolve it to jump here (there is no THEN instruction)
					
				// "loop..endloop" and leave handling
				} else if(tok.sval.equals("loop")){
					insts.add(new Flow.LoopStartInstruction()); // compile a loop start
					cstack.push(insts.size()); // remember the instruction after the loop start point
					loopStack.push(leaveList); // remember the current leave list (which might be null)
					leaveList = new ArrayList<Integer>(); // create a new leave list for this loop
				} else if(tok.sval.equals("endloop")){
					if(leaveList==null)throw new BotConfigException("endloop when not in a loop");
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
					if(leaveList==null)throw new BotConfigException("leave when not in a loop");
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.LeaveInstruction());
				} else if(tok.sval.equals("ifleave")){
					if(leaveList==null)throw new BotConfigException("ifleave when not in a loop");
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.IfLeaveInstruction());
					
				// "cases {..if..case} {..if..case} {.. otherwise}" handling
				// we construct a linked list of case jumps through the offset pointer of the jumps,
				// terminated by the end list marker -1.
				} else if(tok.sval.equals("cases")){
					cstack.push(-1); // push the end list marker - the first "case" will pop it.
				} else if(tok.sval.equals("case")){
					int ref = cstack.pop(); // get the corresponding "if"
					if(ref<0)throw new BotConfigException("'case' should have an 'if'");
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
							throw new BotConfigException("bad case construction");
						Flow.JumpInstruction j = (Flow.JumpInstruction)insts.get(ref);
						if(!j.isCaseJump)
							throw new BotConfigException("bad case construction");
						int next = j.offset; // get the next location in the list.
						j.isCaseJump = false; // turn it back into an ordinary jump
						resolveJumpForwards(ref,0); // resolve the jump destination
						ref = next; // get the next jump to resolve.
					}
					
				// "stop" and other quick flow control stuff
				} else if(tok.sval.equals("stop")){
					insts.add(new Flow.StopInstruction());
				}
				// custom commands!
				else if(cmds.containsKey(tok.sval)){
					insts.add(new MethodCallInstruction(tok.sval,cmds.get(tok.sval)));
				}
				// and finally user functions
				else if(funcMap.containsKey(tok.sval)){
					insts.add(new FuncCallInstruction(funcMap.get(tok.sval)));
				}
				else
					throw new BotConfigException("cannot find action cmd or function: "+tok.sval);
			}
		}
		
		// flow control termination checks
		if(leaveList!=null)throw new BotConfigException("loop left unclosed");
		if(!cstack.isEmpty())throw new BotConfigException("flow control statement left unclosed");
	}
	
	private static String[] parseLocalList(StreamTokenizer tok,String fname,int terminator) throws IOException, BotConfigException{
		List<String> ss = new ArrayList<String>();
		for(;;){
			int t = tok.nextToken();
			if(t==StreamTokenizer.TT_WORD)
				ss.add(tok.sval);
			else if(t==terminator)break;
			else if(t!=',')
				throw new BotConfigException("bad local specification in function '"+fname+"'");
		}
		return ss.toArray(new String[ss.size()]);
	}
	
	/// parses functions and adds them to the function list. Assumes the function introducer (probably ':')
	/// has been read and we're ready for the name.
	
	public static void parseFunction(StreamTokenizer tok) throws IOException, BotConfigException, PatternParseException{
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new BotConfigException("expected name of function after ':'");
		String name = tok.sval;
		String[] argarray = null;
		String[] locarray = null;
		
		if(funcMap.containsKey(name))
			throw new BotConfigException("function already exists: "+name);
				
		if(tok.nextToken()=='|'){
			// we're getting an args,locals specification
			argarray = parseLocalList(tok,name,':');
			locarray = parseLocalList(tok,name,'|');
			// create the function
		} else {
			tok.pushBack(); // no arg list, put the token back
		}
		// define the function (before compiling, so we can recurse)
		Function f = new Function(name,argarray,locarray);
		funcMap.put(name,f);
		// now compile it
		InstructionStream insts = new InstructionStream(tok);
		// and set the instructions
		f.setInsts(insts);
	}
	
	/// used to fix up an existing jump instruction to jump to the current instruction
	/// with an offset. 
	private void resolveJumpForwards(int refToIf, int i) throws BotConfigException {
		insts.get(refToIf).setJump((insts.size()-refToIf)+i);
	}
	
	/**
	 * Parse subpatterns to match after this response.
	 * Syntax is { "pattern string" action... ; "pattern string" action...; }
	 * We have already parsed the opening. Naturally can nest.
	 * @param tok
	 * @throws BotConfigException 
	 * @throws IOException 
	 * @throws PatternParseException 
	 */
	private List<Pair> parseSubPatterns(StreamTokenizer tok) throws BotConfigException, IOException, PatternParseException {
		List<Pair> subpatterns = new ArrayList<Pair>();
		for(;;){
			int tt = tok.nextToken();
			if(tt=='}')break;
			if(tt != '\"' && tt != '\'')
				throw new BotConfigException("error in parsing subpattern, expected a pattern string");
			Pattern pat = new Pattern(null,tok.sval);
			InstructionStream act = new InstructionStream(tok);
			Pair p = new Pair(pat,act);
			subpatterns.add(p);
		}
		return subpatterns;
	}


}
