package com.kq4iyo.imgview;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;

public class Launcher {

	private Window window;
	
	private static boolean doDebug = true;
	
	public Launcher(String imagePath)
	{
		String filename = imagePath;
		if(imagePath == "")
			filename = "C:/Users/overs/Pictures/PNG_Test.png";
		File f = new File(filename);
		ImageOpener testImage = new ImageOpener(f);
		
		window = new Window(1024,720,f.getName());
		
		long timePerFrame = 1000/5, lastPrintTime = 0; // 5 FPS (still image)
		int frames = 0;
		boolean hasAdjustedSize = false;
		
		while (true) {
			long startTime = System.currentTimeMillis();
			
			if(testImage.imageWidth > 0 && testImage.imageWidth != window.getWidth() && !hasAdjustedSize)
			{
				window.setSize(testImage.imageWidth, testImage.imageHeight);
				hasAdjustedSize = true;
			}
			
			Graphics g = window.beginPaint();
			if(g != null)
			{

				g.clearRect(0, 0, 1024, 720);
				g.setColor(Color.black);
				g.fillRect(0, 0, 1024, 720);
				if(testImage.ready())
				{
					double scaleWidth = (double)(window.getWidth()) / (double)(testImage.imageWidth);
					double scaleHeight = (double)(window.getHeight()) / (double)(testImage.imageHeight);
					
					Color[] pixels = testImage.pixelColors;
					for(int y = 0; y < testImage.imageHeight; y++) {
						for(int x = 0; x < testImage.imageWidth; x++) {
							Color p = pixels[y * testImage.imageHeight + x];
							g.setColor(p);
							g.fillRect(x, y, 1, 1);
							
						}
					}
				}
				
				window.endPaint(g);
			}
			
			long endTime = System.currentTimeMillis();
			while(endTime - startTime <= timePerFrame) {
				endTime = System.currentTimeMillis();
			}
			
			if(endTime - lastPrintTime >= 1000) {
				lastPrintTime = endTime;
				if(Launcher.debug())
					System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ FPS: " + frames);
				frames = 0;
			}else {
				frames++;
			}
		}
	}
	
	
	public static void main(String[] args) {
		if (args.length == 1)
			new Launcher(args[0]);
		else
			new Launcher("");
	}
	
	public static String getProcessID() {
		String toReturn = "";
		
		long pid = ProcessHandle.current().pid();
		toReturn += String.valueOf(pid);
		
		return toReturn;
	}
	
	public static boolean debug() {
		return doDebug;
	}
	
}
