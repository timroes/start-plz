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
package de.timroes.startplz.plugins;

import de.timroes.startplz.Log;
import de.timroes.startplz.Plugin;
import de.timroes.startplz.Result;
import de.timroes.startplz.plugins.util.IconThemeUtil;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import org.ini4j.Ini;

/**
 * List applications installed on the computer, and let the user start them.
 * 
 * @author Tim Roes <mail@timroes.de>
 */
public class ApplicationsPlugin extends Plugin {

	private List<ApplicationInfo> info = new LinkedList<ApplicationInfo>();
	private IconThemeUtil iconTheme = new IconThemeUtil();
	
	private final static String DESKTOP_SECTION = "Desktop Entry";
	private final static String NAME_ENTRY = "Name";
	private final static String CMD_ENTRY = "Exec";
	private final static String COMMENT_ENTRY = "Comment";
	private final static String ICON_ENTRY = "Icon";
	private final static String TYPE_ENTRY = "Type";
	
	private final static String HOME = System.getProperty("user.home");
	private final static String[] DESKTOP_FILE_PATHES = new String[] {
		HOME + "/.local/share/applications/", 
		"/usr/local/share/applications/",
		"/usr/share/applications/"
	};
	
	@Override
	public synchronized void refresh() {
		info.clear();
		iconTheme.reload();
		for(String dir : DESKTOP_FILE_PATHES) {
			readFromDirectory(new File(dir));
		}
	}
	
	/**
	 * Read all desktop files in a specific directory.
	 * 
	 * @param dir The directory to scan for desktop files.
	 */
	private void readFromDirectory(File dir) {

		if(!dir.exists() || !dir.isDirectory()) {
			return;
		}
		
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				readFromDirectory(f);
			} else if(f.isFile() && f.canRead() && f.getName().endsWith(".desktop")) {
				try {
					// Read desktop file
					Ini ini = new Ini(f);
					if(ini.get(DESKTOP_SECTION, TYPE_ENTRY).equals("Application")) {
						String name = ini.get(DESKTOP_SECTION, NAME_ENTRY);
						String cmd = ini.get(DESKTOP_SECTION, CMD_ENTRY);
						String comment = ini.get(DESKTOP_SECTION, COMMENT_ENTRY);
						File icon = iconTheme.getIconPath(ini.get(DESKTOP_SECTION, ICON_ENTRY), 32);
						if(!cmd.isEmpty() && !name.isEmpty()) {
							info.add(new ApplicationInfo(name, cmd, comment, icon));
						}
					}
				} catch (IOException ex) {
					Log.w("Could not read .desktop file.", ex);
				}
			}
		}
	}
	
	@Override
	public synchronized List<? extends Result> search(String query) {
		
		query = query.toLowerCase();
		
		List<ApplicationResult> result = new LinkedList<ApplicationResult>();
		
		for(ApplicationInfo i : info) {
			if(i.name.toLowerCase().contains(query) 
					|| i.cmd.toLowerCase().contains(query)
					|| (i.comment != null && i.comment.toLowerCase().contains(query))) {
				result.add(new ApplicationResult(i, 
						getMaximumStringSimilarity(query, i.name, i.cmd, i.comment)));
			}
		}
		
		return result;
		
	}
	
	/**
	 * Holds information about an application, read from a desktop file.
	 */
	private class ApplicationInfo {
		
		String name;
		String cmd;
		String comment;
		File icon;

		public ApplicationInfo(String name, String cmd, String comment, File icon) {
			this.name = name;
			this.cmd = cmd.replaceAll("%[fFuUdDnNickvm]", "");
			this.comment = comment;
			this.icon = icon;
		}
		
	}

	private class ApplicationResult extends Result {

		private ApplicationInfo app;
		private double weight;

		public ApplicationResult(ApplicationInfo app, double weight) {
			this.app = app;
			this.weight = weight;
		}

		@Override
		public double getWeight() {
			return weight;
		}
		
		@Override
		public String getTitle() {
			return app.name;
		}

		@Override
		public String getSubtitle() {
			return (app.comment != null && !app.comment.isEmpty()) ? app.comment : app.cmd;
		}

		@Override
		public ImageIcon getIcon() {
			if(app.icon != null && app.icon.exists()) {
				return new ImageIcon(app.icon.getAbsolutePath());
			} else {
				return null;
			}
		}

		@Override
		public void execute() {
			try {
				Runtime.getRuntime().exec(app.cmd);
			} catch (IOException ex) {
				Log.w("Could not start application.", ex);
			}
		}
		
	}
	
}
