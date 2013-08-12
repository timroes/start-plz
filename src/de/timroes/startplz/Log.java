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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public class Log {
	
	private static final Logger log = Logger.getLogger(Log.class.getPackage().toString());

	{
		log.setLevel(Level.FINEST);		
	}
	
	/**
	 * Logs a debug message.
	 * 
	 * @param msg The message to log.
	 * @param params Objects to fill in the placeholders.
	 */
	public static void d(String msg, Object... params) {
		log.fine(String.format(msg, params));
	}
	
	/**
	 * Logs a debug message.
	 * 
	 * @param msg The mssage to log.
	 */
	public static void d(String msg) {
		d(msg, (Object[])null);
	}
	
	/**
	 * Logs a warning.
	 * 
	 * @param msg The message to log.
	 * @param params Objects to fill in the placeholders.
	 */
	public static void w(String msg, Object... params) {
		log.warning(String.format(msg, params));
	}
	
	/**
	 * Logs a warning.
	 * 
	 * @param msg The message to log.
	 */
	public static void w(String msg) {
		w(msg, (Object[])null);
	}
	
	public static void w(String msg, Throwable ex) {
		w(msg);
		w(String.valueOf(ex.getMessage()));
		for(StackTraceElement e : ex.getStackTrace()) {
			w(e.toString());
		}
	}
	
	public static void e(String msg, Object... params) {
		log.severe(String.format(msg, params));
	}
	
	public static void e(String msg) {
		e(msg, (Object[])null);
	}
	
	public static void e(String msg, Throwable ex) {
		e(msg);
		e(ex.getMessage());
		for(StackTraceElement e : ex.getStackTrace()) {
			e(e.toString());
		}
	}
	
}
