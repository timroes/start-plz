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
package de.timroes.startplz.plugins.util;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.ini4j.Ini;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public class IconThemeUtil {
	
	private final static String[] EXTENSIONS = new String[] { "png", "svg", "xpm" };
	
	private final static String ICON_THEME = "hicolor";
	
	private List<File> iconBaseDirs = new LinkedList<File>();
	private List<IconTheme> themes = new LinkedList<IconTheme>();
	
	public void reload() {
		reloadIconBaseDirs();
		reloadThemes();
	}
	
	/**
	 * Adds a directory file to a list. This will only add the file to the list,
	 * if the file exists and is a directory.
	 * 
	 * @param dir The directory to add.
	 * @param list The list to add to.
	 */
	private void addDirToList(File dir, List<File> list) {
		if(dir.exists() && dir.isDirectory()) {
			list.add(dir);
		}
	}
	
	/**
	 * Reload all base directories for icons.
	 */
	private void reloadIconBaseDirs() {
		
		// Load dirs from XDG_DATA_DIRS
		String xdg_dir_env = System.getenv("XDG_DATA_DIRS");
		if(xdg_dir_env != null && !xdg_dir_env.isEmpty()) {
			String[] xdg_dirs = xdg_dir_env.split(":");
			for(String xdg_dir : xdg_dirs) {
				addDirToList(new File(xdg_dir, "icons"), iconBaseDirs);
				addDirToList(new File(xdg_dir, "pixmaps"), iconBaseDirs);
			}
		}
		
		// Load user's icon dir
		addDirToList(new File(System.getProperty("user.home"), ".icons"), iconBaseDirs);
		addDirToList(new File(System.getProperty("user.home"), ".local/share/icons"), iconBaseDirs);
		
	}
	
	private void reloadThemes() {
		themes.clear();
		loadTheme(ICON_THEME);
	}
	
	private void loadTheme(String themeName) {
		
		File themeIndex = null;
		for(File iconBaseDir : iconBaseDirs) {
			themeIndex = new File(iconBaseDir, themeName + File.separator + "index.theme");
			if(themeIndex.exists() && themeIndex.isFile()) {
				break;
			}
		}
		// No theme index found anywhere.
		if(themeIndex == null) {
			return;
		}
		
		IconTheme iconTheme = IconTheme.fromIndex(themeIndex);
		if(iconTheme != null) {
			themes.add(iconTheme);
			for(String subtheme : iconTheme.inherits) {
				loadTheme(subtheme);
			}
		}
		
		
	}
	
	/**
	 * Gets the absolute path to an icon name. If no icon could be found or
	 * {@code iconName} wasn't a valid input, {@code null} is returned.
	 * 
	 * @param iconName The name of the icon to look up.
	 * @return The absolute path of the icon or {@code null}.
	 */
	public File getIconPath(String iconName, int iconSize) {
		if(iconName == null) {
			return null;
		}
		// If iconname is already an absolute path, return it.
		File i = new File(iconName);
		if(i.isAbsolute()) {
			return i;
		}
		
		// Strip extension from iconName
		//iconName = iconName.replaceFirst("[.][^.]+$", "");
		
		// Lookup icon in theme
		for(IconTheme theme : themes) {
			File lookup = lookupIconInTheme(iconName, theme, iconSize);
			if(lookup != null && lookup.exists()) {
				return lookup;
			}
		}
		
		for(File iconDir : iconBaseDirs) {
			for(String ext : EXTENSIONS) {
				File icon = new File(iconDir, iconName + "." + ext);
				if(icon.exists()) {
					return icon;
				}
			}
		}

		// No icon found
		return null;
	}
	
	private File lookupIconInTheme(String iconName, IconTheme theme, int iconSize) {
		
	
		// Try to find an exact match
		for(IconTheme.Directory subdir : theme.directories) {
			for(File iconBaseDir : iconBaseDirs) {
				// Directory of icons
				File iconDir = new File(iconBaseDir, theme.name + File.separator + subdir.name);
				if(directoryMatchesSize(subdir, iconSize)) {
					// Directory has right size, find matching extension
					for(String ext : EXTENSIONS) {
						File icon = new File(iconDir, iconName + "." + ext);
						if(icon.exists()) {
							return icon;
						}
					}
				}	

			}
		}
	
		
		// Try to find an the best fitting match
		File best_match = null;
		int minDistance = Integer.MAX_VALUE;
		for(IconTheme.Directory subdir : theme.directories) {
			for(File iconBaseDir : iconBaseDirs) {
				// Directory of icons
				File iconDir = new File(iconBaseDir, theme.name + File.separator + subdir.name);
				int distance = directorySizeDistance(subdir, iconSize);
				if(distance < minDistance) {
					for(String ext : EXTENSIONS) {
						File icon = new File(iconDir, iconName + "." + ext);
						if(icon.exists()) {
							best_match = icon;
							minDistance = distance;
						}
					}
				}
			}
		}
		
		return best_match;
	}
	
	/**
	 * Checks whether a given subdirectory of a theme matches the requested icon size.
	 * Depending on the type declared for that subdirectory, this must not be
	 * the exact size, but be within a special threshold or minimum and maximum sizes.
	 * 
	 * @param directory The directory to check.
	 * @param iconSize The icon size requested.
	 * @return Whether the directory matches the icon size.
	 */
	private boolean directoryMatchesSize(IconTheme.Directory directory, int iconSize) {
		switch(directory.type) {
			case FIXED:
				return iconSize == directory.size;
			case SCALABLE:
				return directory.minSize <= iconSize && iconSize <= directory.maxSize;
			case TRESHOLD:
				return directory.size - directory.threshold <= iconSize
						&& iconSize <= directory.size + directory.threshold;
		}
		return false;
	}

	private int directorySizeDistance(IconTheme.Directory directory, int iconSize) {
		switch(directory.type) {
			case FIXED:
				return Math.abs(iconSize - directory.size);
			case SCALABLE:
				if(iconSize < directory.minSize) {
					return directory.minSize - iconSize;
				} else if(iconSize > directory.maxSize) {
					return iconSize - directory.maxSize;
				}
			case TRESHOLD:
				if(iconSize < directory.size - directory.threshold) {
					return directory.size - iconSize;
				} else if(iconSize > directory.size + directory.threshold) {
					return iconSize - directory.size;
				}
		}
		return Integer.MAX_VALUE;
	}

	
	private static class IconTheme {
		
		enum Type {
			FIXED, SCALABLE, TRESHOLD
		};
		
		String name;
		String[] inherits;
		Directory[] directories;
		
		private static class Directory {
			
			String name;
			Type type;
			int minSize;
			int maxSize;
			int size;
			int threshold;
			
		}
		
		public static IconTheme fromIndex(File themeIndex) {
			
			try {
				Ini themeIni = new Ini(themeIndex);
				
				IconTheme theme = new IconTheme();
				// Name is the folder name of the theme
				theme.name = themeIndex.getParentFile().getName();
				// Read directories vom theme file
				String[] themeDirs = themeIni.get("Icon Theme", "Directories").split(",");
				theme.directories = new Directory[themeDirs.length];
				for(int i = 0; i < themeDirs.length; i++) {
					
					Directory d = new Directory();
					String dir = themeDirs[i];
					
					d.name = dir;
					
					String type = themeIni.get(dir, "Type");
					if("Fixed".equals(type)) {
						d.type = Type.FIXED;
					} else if("Scalable".equals(type)) {
						d.type = Type.SCALABLE;
					} else {
						d.type = Type.TRESHOLD;
					}
					
					String minSize = themeIni.get(dir, "MinSize");
					d.minSize = (minSize == null) ? 0 : Integer.valueOf(minSize);
					
					String maxSize = themeIni.get(dir, "MaxSize");
					d.maxSize = (maxSize == null) ? 0 : Integer.valueOf(maxSize);
					
					String size = themeIni.get(dir, "Size");
					d.size = (size == null) ? 0 : Integer.valueOf(size);
					
					String threshold = themeIni.get(dir, "Threshold");
					d.threshold = (threshold == null) ? 2 : Integer.valueOf(threshold);
					
					theme.directories[i] = d;
					
				}
				
				String inherits = themeIni.get("Icon Theme", "Inherits");
				if(inherits != null && !inherits.isEmpty()) {
					theme.inherits = inherits.split(",");
				} else {
					theme.inherits = new String[0];
				}
				return theme;
			} catch (IOException ex) {
				return null;
			}
			
		}
		
	}
	
}
