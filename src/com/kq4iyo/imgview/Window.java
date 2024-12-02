package com.kq4iyo.imgview;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Window {

	private JFrame frame;
	private Canvas canvas;
	private JMenuBar menu;
	private JMenu fileMenu;
	
	public Window(int w, int h, String title) {
		frame = new JFrame();
		frame.setResizable(true);
		frame.setSize(w,h);
		frame.setLocationRelativeTo(null);
		frame.setTitle("Image " + title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menu = new JMenuBar();
		
		
		fileMenu = new JMenu("File");
		JMenuItem openFileItem = new JMenuItem("Open From File");
		JMenuItem openURLItem = new JMenuItem("Open From URL");
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(openFileItem);
		fileMenu.add(openURLItem);
		fileMenu.add(exitItem);
		
		ActionListener listener = new MenuButtonListener();
		
		openFileItem.addActionListener(listener);
		openURLItem.addActionListener(listener);
		exitItem.addActionListener(listener);
		
		menu.add(fileMenu);
		
		frame.setJMenuBar(menu);
		
		canvas = new Canvas();
		
		frame.add(canvas);
		frame.setVisible(true);
	}
	
	public void setSize(int width, int height) {
		//frame.pack();
		frame.setSize(width, height+menu.getSize().height);
		canvas.setSize(width, height-menu.getSize().height);
	}
	
	public int getWidth() {
		return canvas.getWidth();
	}
	
	public int getHeight() {
		return canvas.getHeight();
	}
	
	public Graphics beginPaint() {
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs == null) {
			canvas.createBufferStrategy(2);
			return null;
		}
		Graphics g = bs.getDrawGraphics();
		return g;
	}
	
	public void endPaint(Graphics g) {
		BufferStrategy bs = canvas.getBufferStrategy();
		if(bs == null || g == null)
			return;
		
		g.dispose();
		bs.show();
	}
	
}
