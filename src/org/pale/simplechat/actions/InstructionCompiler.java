package org.pale.simplechat.actions;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.pale.simplechat.Bot;
import org.pale.simplechat.BotConfigException;
import org.pale.simplechat.Category;
import org.pale.simplechat.Conversation;
import org.pale.simplechat.Logger;
import org.pale.simplechat.Pair;
import org.pale.simplechat.ParserError;
import org.pale.simplechat.Pattern;
import org.pale.simplechat.values.CatValue;
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


	// the registry of commands
	private static Map<String,Method> cmds = new HashMap<String,Method>();

	// static ctor to register all builtins
	static {
		Logger.log(Logger.CONFIG, "registering builtins");
		register(org.pale.simplechat.commands.Categories.class);
		register(org.pale.simplechat.commands.Debugging.class);
		register(org.pale.simplechat.commands.Lists.class);
		register(org.pale.simplechat.commands.OutputString.class);
		register(org.pale.simplechat.commands.Stack.class);
		register(org.pale.simplechat.commands.Strings.class);
		register(org.pale.simplechat.commands.Topics.class);
		register(org.pale.simplechat.commands.Types.class);
		register(org.pale.simplechat.commands.Maths.class);

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
				Logger.log(Logger.CONFIG,"registered "+name);
			}
		}		
	}


	InstructionCompiler(Bot bot,StreamTokenizer tok) throws IOException, ParserError {
		for(;;){
			int t = tok.nextToken();
			if(t == ';' || t == StreamTokenizer.TT_EOF)
				break;
			switch(t){
			case '\"':
			case '\'':
				insts.add(new StringInstruction(bot,tok.sval));
				break;
			case '`':
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new ParserError("expected a single word after `");
				insts.add(new LiteralInstruction(new StringValue(tok.sval)));
				break;
			case '~':
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new ParserError("expected a category name after ~");
				else {
					Category c = bot.getCategory(tok.sval);
					if(c==null)
						throw new ParserError("unknown category: "+tok.sval);
					insts.add(new LiteralInstruction(new CatValue(tok.sval,c)));
				}
				break;
			case '$':
				if(tok.nextToken()!=StreamTokenizer.TT_WORD)
					throw new ParserError("expected a varname after $");
				insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.PATVAR));
				break;
			case '?':
				switch(tok.nextToken()){
				case StreamTokenizer.TT_WORD:
					insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.CONVVAR));
					break;
				case '@':
					if(tok.nextToken()!=StreamTokenizer.TT_WORD)
						throw new ParserError("expected a varname after ?@");
					insts.add(new GetVarInstruction(tok.sval,GetVarInstruction.Type.INSTVAR));
					break;
				default: throw new ParserError("expected a varname or sigil after ?");
				}
				break;
			case '.': // append to print output
				insts.add(new Printing.PrintInstruction());
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
							throw new ParserError("expected a varname after !@");
						insts.add(new SetVarInstruction(tok.sval,SetVarInstruction.Type.INSTVAR));
						break;
					default: throw new ParserError("expected a varname or sigil after !");
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
				insts.add(new LiteralInstruction(new SubPatValue(parseSubPatterns(bot,tok))));
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
			case ':':
			{
				InstructionStream str = new InstructionStream(bot,tok);
				try {
					Conversation c = new Conversation(); // create dummy convo
					str.run(c, true);
					insts.add(new LiteralInstruction(c.pop()));
				} catch (ActionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new ParserError("error in running constant expression: "+e.getMessage());
				}
			}
			break;
			case StreamTokenizer.TT_WORD:
				// "if .. (else ..) then" handling
				if(tok.sval.equals("if")){
					cstack.push(insts.size()); // remember where we are..
					insts.add(new Flow.IfInstruction()); // .. and compile an IF to fixup later
				} else if(tok.sval.equals("else")){
					int ref = cstack.pop(); // pop the IF..
					if(ref<0)throw new ParserError("'else' matching with 'cases'?");
					resolveJumpForwards(ref,1); // .. and resolve it to jump to just past here
					cstack.push(insts.size()); // push where we are
					insts.add(new Flow.JumpInstruction()); // and compile a jump to the end
				} else if(tok.sval.equals("then")){
					int ref = cstack.pop(); // pop the IF or ELSE location
					if(ref<0)throw new ParserError("'then' matching with 'cases'?");
					resolveJumpForwards(ref,0); // and resolve it to jump here (there is no THEN instruction)

					// "loop..endloop" and leave handling
				} else if(tok.sval.equals("each")){
					if(tok.nextToken()!=StreamTokenizer.TT_WORD || !tok.sval.equals("loop"))
						throw new ParserError("each must be followed by loop");
					insts.add(new Flow.IterLoopStartInstruction());
					cstack.push(insts.size()); // remember the instruction after the loop start point
					loopStack.push(leaveList); // remember the current leave list (which might be null)
					leaveList = new ArrayList<Integer>(); // create a new leave list for this loop
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.IterLoopLeaveIfDone());
					break;
				} else if(tok.sval.equals("i")){
					insts.add(new Flow.LoopGetInstruction(0));
				} else if(tok.sval.equals("j")){
					insts.add(new Flow.LoopGetInstruction(1));
				} else if(tok.sval.equals("k")){
					insts.add(new Flow.LoopGetInstruction(2));
				} else if(tok.sval.equals("loop")){
					insts.add(new Flow.LoopStartInstruction()); // compile a loop start
					cstack.push(insts.size()); // remember the instruction after the loop start point
					loopStack.push(leaveList); // remember the current leave list (which might be null)
					leaveList = new ArrayList<Integer>(); // create a new leave list for this loop
				} else if(tok.sval.equals("endloop")){
					if(leaveList==null)throw new ParserError("endloop when not in a loop");
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
					if(leaveList==null)throw new ParserError("leave when not in a loop");
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.LeaveInstruction());
				} else if(tok.sval.equals("ifleave")){
					if(leaveList==null)throw new ParserError("ifleave when not in a loop");
					leaveList.add(insts.size()); // add to the leave list to fixup in endloop
					insts.add(new Flow.IfLeaveInstruction());

					// "cases {..if..case} {..if..case} {.. otherwise}" handling
					// we construct a linked list of case jumps through the offset pointer of the jumps,
					// terminated by the end list marker -1.
				} else if(tok.sval.equals("cases")){
					cstack.push(-1); // push the end list marker - the first "case" will pop it.
				} else if(tok.sval.equals("case")){
					int ref = cstack.pop(); // get the corresponding "if"
					if(ref<0)throw new ParserError("'case' should have an 'if'");
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
							throw new ParserError("bad case construction");
						Flow.JumpInstruction j = (Flow.JumpInstruction)insts.get(ref);
						if(!j.isCaseJump)
							throw new ParserError("bad case construction");
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
				else if(bot.getFunc(tok.sval)!=null){
					insts.add(new FuncCallInstruction(bot.getFunc(tok.sval)));
				}
				else
					throw new ParserError("cannot find action cmd or function: "+tok.sval);
			}
		}

		// flow control termination checks
		if(leaveList!=null)throw new ParserError("loop left unclosed");
		if(!cstack.isEmpty())throw new ParserError("flow control statement left unclosed");
	}



	private static String[] parseLocalList(StreamTokenizer tok,String fname,int terminator) throws IOException, ParserError{
		List<String> ss = new ArrayList<String>();
		for(;;){
			int t = tok.nextToken();
			if(t==StreamTokenizer.TT_WORD)
				ss.add(tok.sval);
			else if(t==terminator)break;
			else if(t!=',')
				throw new ParserError("bad local specification in function '"+fname+"'");
		}
		return ss.toArray(new String[ss.size()]);
	}

	/// parses functions and adds them to the function list. Assumes the function introducer (probably ':')
	/// and name have been read and we're ready for the arglist and func body.
	/// Bot and name are passed in if we are compiling a named function, so that they
	/// can be set prior to compilation of the instructions to permit recursion. 

	private static Function parseFunction(Bot bot,String name,StreamTokenizer tok) throws IOException, ParserError {
		String[] argarray = null;
		String[] locarray = null;

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
		if(bot!=null && name!=null)
			bot.putFunc(name,f); // add the name to the bot HERE - before compilation
		// now compile it
		InstructionStream insts = new InstructionStream(bot,tok);
		// and set the instructions
		f.setInsts(insts);
		return f; // if we're doing an anonymous function we'll need this.
	}

	/// parses a function with a name, assuming we're just waiting for the name
	public static void parseNamedFunction(Bot bot,StreamTokenizer tok) throws IOException, ParserError{
		if(tok.nextToken()!=StreamTokenizer.TT_WORD)
			throw new ParserError("expected name of function after ':'");
		String name = tok.sval;
		if(bot.getFunc(name)!=null)
			throw new ParserError("function already exists: "+name);
		InstructionCompiler.parseFunction(bot,name,tok);
	}

	/// used to fix up an existing jump instruction to jump to the current instruction
	/// with an offset. 
	private void resolveJumpForwards(int refToIf, int i) throws ParserError {
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
	private List<Pair> parseSubPatterns(Bot bot,StreamTokenizer tok) throws ParserError, IOException  {
		List<Pair> subpatterns = new ArrayList<Pair>();
		for(;;){
			int tt = tok.nextToken();
			if(tt=='}')break;
			if(tt != '\"' && tt != '\'')
				throw new ParserError("error in parsing subpattern, expected a pattern string");
			Pattern pat = new Pattern(bot,null,tok.sval);
			InstructionStream act = new InstructionStream(bot,tok);
			Pair p = new Pair(pat,act);
			subpatterns.add(p);
		}
		return subpatterns;
	}


}
