package org.pale.chattest;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.pale.simplechat.Bot;
import org.pale.simplechat.BotConfigException;
import org.pale.simplechat.BotInstance;

public class MainWindow implements ActionListener {

	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;
	private BotInstance instance;
	private Object source;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
					window.textField.requestFocusInWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		Path homedir = Paths.get("/home/white/git/SimpleChat/testbot");
		Bot b;
		try {
			b = new Bot(homedir);
			instance = new BotInstance(b);
			source = new Object();
			initialize();
		} catch (BotConfigException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
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
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String s = instance.handle(textField.getText(), source);
		textArea.append(s+"\n");
		textField.setText("");
	}

}
