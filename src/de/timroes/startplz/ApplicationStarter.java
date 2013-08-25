/*
 * Copyright 2013 Tim Roes <mail@timroes.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.timroes.startplz;

import de.timroes.startplz.ui.SearchFrame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import javax.swing.JFrame;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public class ApplicationStarter {
    
	private final static int SINGLE_INSTANCE_PORT = 27262;
	private final static String SINGLE_INSTANCE_KEY = ApplicationStarter.class.getName().concat("\n");
	
	private static SearchFrame singleInstanceFrame;
	
	public static void main(String[] args) {
	
		if(isApplicationRunning()) {
			
		} else {
			// If no other instance is running, start the application UI and the single
			// instance server, waiting for other instances to run.
			Log.d("No instance found running. Start a new instance.");
			createApplicationInstance(
					!Arrays.asList(args).contains("--hidden"),
					Arrays.asList(args).contains("--closing"));
			try {
				startSingleInstanceServer();
			} catch (IOException ex) {
				Log.w("Could not start the single instance server. "
						+ "The program won't run in background.", ex);
				singleInstanceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		}
		
	}
	
	private static boolean isApplicationRunning() {
		Socket clientSocket = null;
		OutputStream out = null;
		try {
			clientSocket = new Socket(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), 
					SINGLE_INSTANCE_PORT);
			out = clientSocket.getOutputStream();
			out.write(SINGLE_INSTANCE_KEY.getBytes());
		} catch(IOException ex) {
			Log.d("Could not found socket for running instance.", ex);
			return false;
		} finally {
			try {
				if(out != null) { out.close(); }
			} catch (IOException ex) {
				Log.w("Could not close socket to running instance.", ex);
			}
			try {
				if(clientSocket != null) { clientSocket.close(); }
			} catch(IOException ex) {
				Log.w("Could not close socket to running instance.", ex);
			}
		}
		return true;
	}
	
	private static void createApplicationInstance(boolean showWindow, boolean shouldWindowClose) {
		singleInstanceFrame = new SearchFrame();
		singleInstanceFrame.setDefaultCloseOperation(
				shouldWindowClose ? JFrame.EXIT_ON_CLOSE : JFrame.HIDE_ON_CLOSE);
		if(showWindow) {
			showApplicationInstance();
		}
	}
	
	private static void showApplicationInstance() {
		singleInstanceFrame.setVisible(true);
		singleInstanceFrame.clearInstance();
	}

	private static void startSingleInstanceServer() throws IOException {
		final ServerSocket socket = new ServerSocket(SINGLE_INSTANCE_PORT, 10, 
				InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean closed = false;
				while(!closed) {
					if(socket.isClosed()) {
						closed = true;
					} else {
						try {
							// Wait for a new client (another instance started)
							Socket client = socket.accept();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(client.getInputStream()));
							String msg = reader.readLine();
							if(SINGLE_INSTANCE_KEY.trim().equals(msg.trim())) {
								Log.d("Another instance tried to start. Show the running instance.");
								showApplicationInstance();
							}
							reader.close();
							client.close();
						} catch (IOException ex) {
							Log.w("Single instance server socket error", ex);
							closed = true;
							singleInstanceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						}
						
					}
				}
			}
		}, "SingleInstanceServer").start();
	}
    
}
