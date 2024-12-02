package com.kq4iyo.imgview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class MenuButtonListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		Object sourceObject = e.getSource();
		if(sourceObject instanceof JMenuItem)
		{
				JMenuItem item = (JMenuItem)(sourceObject);
				String buttonText = item.getText();
				
				switch (buttonText) {
				case "Open From File":{
					
					if(Launcher.debug())
						System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ Opening Image From File");
					
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					
					int result = fileChooser.showOpenDialog(null);
					
					if(result == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile();
						if(Launcher.debug())
							System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ Selected File: " + selectedFile.getAbsolutePath());
						
						// Get the current JAR file
						String jarPath = "";
						ProtectionDomain pd = Launcher.class.getProtectionDomain();
						CodeSource cs = pd.getCodeSource();
						if(cs != null) {
							File jarFile = null;
							try {
								jarFile = new File(cs.getLocation().toURI().getPath());
							} catch (URISyntaxException e1) {
								e1.printStackTrace();
							}
							if(jarFile == null) {
								System.err.println("Unable To Locate The Jar File To Spawn A New Instance With");
							}
							jarPath = jarFile.getAbsolutePath();
						}
						if(jarPath.equals("") || jarPath == null) {
							System.err.println("Unable To Locate The Jar File To Spawn A New Instance With");
						}
						
						System.out.println("Jar Path: " + jarPath);
						
						ProcessBuilder processbuilder = new ProcessBuilder("java","-jar",jarPath,selectedFile.getAbsolutePath());
						try {
							processbuilder.start();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}else {
						if(Launcher.debug())
							System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ Cancelled Opening Image From File.");
					}
					
					break;
				}
				case "Open From URL":{
					if(Launcher.debug()){
						System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ Opening Image From URL");
					}
					
					String input = JOptionPane.showInputDialog(null, "Enter URL Of Image");
					if(input != null) {
						System.out.println("Attempting To Retreive Image File From URL: " + input);
						
						try {
							URL url = new URL(input);
							HttpURLConnection connection = (HttpURLConnection)url.openConnection();
							connection.setRequestMethod("GET");
							
							try(InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(System.getProperty("java.io.tmpdir") + "\\tmpimage.png")){
								
								byte[] buffer = new byte[4096];
								int bytesRead;
								while((bytesRead = in.read(buffer)) != -1) {
									out.write(buffer, 0, bytesRead);
								}
								
								System.out.println("Downloaded file to " + System.getProperty("java.io.tmpdir") + "\\tmpimage.png");
								// Get the current JAR file
								String jarPath = "";
								ProtectionDomain pd = Launcher.class.getProtectionDomain();
								CodeSource cs = pd.getCodeSource();
								if(cs != null) {
									File jarFile = null;
									try {
										jarFile = new File(cs.getLocation().toURI().getPath());
									} catch (URISyntaxException e1) {
										e1.printStackTrace();
									}
									if(jarFile == null) {
										System.err.println("Unable To Locate The Jar File To Spawn A New Instance With");
									}
									jarPath = jarFile.getAbsolutePath();
								}
								if(jarPath.equals("") || jarPath == null) {
									System.err.println("Unable To Locate The Jar File To Spawn A New Instance With");
								}
								
								System.out.println("Jar Path: " + jarPath);
								
								ProcessBuilder processbuilder = new ProcessBuilder("java","-jar",jarPath,System.getProperty("java.io.tmpdir") + "\\tmpimage.png");
								try {
									processbuilder.start();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						} catch (ProtocolException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						
					}
					
					break;
				}
				case "Exit":{
					if(Launcher.debug())
						System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ Exiting...");
					
					System.exit(0);
					break;
				}
				default:{
					if(Launcher.debug()) {
						System.out.println("[DEBUG:" + Launcher.getProcessID() + "] \\\\ Unknown button with text \"" + buttonText + "\" pressed. No action taken.");
					}
					
					break;
				}
				}
		}
		
	}

}
