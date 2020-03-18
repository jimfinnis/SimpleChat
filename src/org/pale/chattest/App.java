package org.pale.chattest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.*;

import org.pale.simplechat.Bot;
import org.pale.simplechat.BotConfigException;
import org.pale.simplechat.BotInstance;
import org.pale.simplechat.Logger;
import org.pale.simplechat.actions.ActionLog;
import org.pale.simplechat.actions.InstructionCompiler;

public class App {

	private BotInstance instance = null;
	private Object source;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		InstructionCompiler.register(Stubs.class);
		App app = new App();
		if(args.length>0) {
			app.loadBot(new File(args[0]));
		}
		app.run();

	}

	boolean running=true;

	public void run(){
		Scanner input = new Scanner(System.in);
		String out="";
		while(running) {
			long startTime = System.currentTimeMillis();
			System.out.print("> ");
			String in = input.nextLine();
			if (in.substring(0, 1).equals("@")) { // @ means special command
				runCommand(in.substring(1));
			} else if(instance == null) {
				out = "no bot loaded";
			} else if (in.substring(0, 1).equals(":")) { // :name means run a func
				out = instance.runFunc(in.substring(1), source);
			} else {
				out = instance.handle(in, source);
			}
			long endTime = System.currentTimeMillis();
			out = out + " [" + (endTime - startTime) + "ms]";
			System.out.println(out);
		}
	}

	private void usage(){
		String s[] = {
				"@q         : quit",
				"@lBOT      : load bot",
				"@r         : reload bot",
				"@a         : show action log",
				"@dNUM      : set logging flags",
		};
		for(String x:s){
			System.out.println(x);
		}
	}
	private void runCommand(String s){
		switch(s.substring(0,1).toLowerCase()) {
			case "q":
				running = false; break;
			case "l":
				loadBot(new File(s.substring(1)));break;
			case "a":
				ActionLog.show();
				break;
			case "r":
				loadBot(null);
				break;
			case "d":
				Logger.setLog(Integer.parseInt(s.substring(1)));
				break;
			default:
				usage();break;
		}
	}

	private File prevFile=null;


	// the bot loader assumes that all bots, including any parent bots used by "inherit", are in the same directory.

	private void loadBot(File f) { // takes the directory
		if(f==null){
			if(prevFile == null){
				System.out.println("No bot to reload");
				return;
			}
			f = prevFile;
		} else
			prevFile = f;
		final String p = f.getParent(); // get parent directory
		Bot.setPathProvider(new Bot.PathProvider() {
			public Path path(String name) {
				System.out.println("Requesting name for " + name);
				String fn = p + "/" + name;
				return Paths.get(fn);
			}
		});
		try {
			System.out.println("Loading bot: "+f.getName());
			Bot b = Bot.loadBot(f.getName());
			instance = new BotInstance(b, f.getName());
			instance.runInits();
		} catch (BotConfigException e) {
			System.out.println("Cannot load bot: "+e.getMessage());
		}
	}
}
