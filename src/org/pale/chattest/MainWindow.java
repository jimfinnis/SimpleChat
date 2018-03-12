package org.pale.chattest;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileSystemView;

import org.pale.simplechat.Bot;
import org.pale.simplechat.BotConfigException;
import org.pale.simplechat.BotInstance;

public class MainWindow implements ActionListener, MenuListener {

	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;
	private BotInstance instance = null;
	private Object source;
	private File botfile = null;
	private JMenuItem reloadMenuItem;
	
	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
					window.textField.requestFocusInWindow();
					if(args.length>0){
						window.loadBot(new File(args[0]));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public MainWindow() {
		System.out.println("init");
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		textField = new JTextField();
		frame.getContentPane().add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);
		
		textArea = new JTextArea();
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		
		textField.addActionListener(this);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		addMenus(menuBar);
	}
	
	private void loadBot(File f){
		reloadMenuItem.setEnabled(true);
		botfile = f;
		try {
			Bot b = new Bot(f.toPath());
			instance = new BotInstance(b);
		} catch (BotConfigException e) {
			JOptionPane.showMessageDialog(frame,"Could not load bot.\n"+e.getMessage());			
		}
	}

	private void addMenus(JMenuBar menuBar) {
		JMenu m;
		JMenuItem i;

		m = new JMenu("File");
		menuBar.add(m);
		m.addMenuListener(this);
		
		i = new JMenuItem("Load Bot");
		m.add(i);
		i.setAccelerator(KeyStroke.getKeyStroke('O',java.awt.Event.CTRL_MASK));
		i.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser c = new JFileChooser(FileSystemView.getFileSystemView());
				c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(c.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION){
					File f = c.getSelectedFile();
					loadBot(f);
				}
			}
		});
		
		reloadMenuItem= new JMenuItem("Reload current bot");
		m.add(reloadMenuItem);
		reloadMenuItem.setAccelerator(KeyStroke.getKeyStroke('R',java.awt.Event.CTRL_MASK));
		reloadMenuItem.setEnabled(false); // enabled when we load a bot
		reloadMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// the bot might not have loaded at all, so rather than reload(), we do the whole dance.
				loadBot(botfile);
			}
		});
		
		i = new JMenuItem("About");
		m.add(i);
		i.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame,"ChatTest for SimpleChat\n(C) Jim Finnis 2018");
			}
		});

		i = new JMenuItem("Quit");
		m.add(i);
		i.setAccelerator(KeyStroke.getKeyStroke('W',java.awt.Event.CTRL_MASK));
		i.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String s;
		if(instance != null){
			String in = textField.getText();
			if(in.substring(0,1).equals(":")) { // :name means run a func
				s = instance.runFunc(in.substring(1), source);
			} else {
				s = instance.handle(in, source);				
			}
		} else
			s = "No bot loaded yet.";
		textArea.append(s+"\n");
		textField.setText("");
	}

	@Override
	public void menuCanceled(MenuEvent arg0) {
	}

	@Override
	public void menuDeselected(MenuEvent arg0) {
	}

	@Override
	public void menuSelected(MenuEvent arg0) {
	}

}
