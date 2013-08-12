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

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public class PluginManager {
	
	//<editor-fold defaultstate="collapsed" desc="Singleton">
	private static PluginManager instance;
	
	public static PluginManager get() {
		if(instance == null) {
			instance = new PluginManager();
		}
		return instance;
	}
	//</editor-fold>
	
	private Set<Plugin> plugins;
	
	private PluginManager() {
		loadPlugins();
	}
	
	/**
	 * Registers a plugin to the {@link PluginManager}. The plugin will be queried
	 * for search results.
	 * 
	 * @param plugin The plugin to register.
	 */
	void registerPlugin(Plugin plugin) {
		plugins.add(plugin);
		loadPlugins();
	}
	
	/**
	 * Search in all plugins for a specific search query.
	 * Returns a list of results from all plugins ordered so that the first element
	 * in the list if the best fitting result.
	 * 
	 * @param query The search query.
	 * @return The results ordered descending by relevance.
	 */
	public List<Result> search(String query) {
		List<Result> results = new LinkedList<Result>();
		
		// Query all plugins for results
		for(Plugin p : plugins) {
			try {
				results.addAll(p.search(query));
			} catch(Exception ex) {
				Log.w("Exception in plugin", ex);
			}
		}
		
		// Sort results by weight
		Collections.sort(results, new Comparator<Result>() {
			@Override
			public int compare(Result r1, Result r2) {
				int weight = (int)Math.signum(r2.getWeight() - r1.getWeight());
				if(weight != 0) {
					return weight;
				}
				return r1.getTitle().compareTo(r2.getTitle());
			}
		});
		
		return results;
	}
	
	public void refresh() {
		for(Plugin p : plugins) {
			p.refresh();
		}
	}

	/**
	 * Loads all plugins from the plugins package.
	 */
	private void loadPlugins() {
		try {
			plugins = new HashSet<Plugin>();
			// Get all classes in plugin package
			ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
			String pluginPath = PluginManager.class.getPackage().getName().concat(".plugins");
			ImmutableSet<ClassInfo> classes = classPath.getTopLevelClasses(pluginPath);
			
			// Iterate over all classes in plugin package, and check if they
			// extend Plugin. If so instantiate them and add them to plugin list.
			for(ClassInfo info : classes) {
				try {
					Class<?> clazz = info.load();
					if(Plugin.class.isAssignableFrom(clazz)) {
						Plugin p = (Plugin)clazz.newInstance();
						p.refresh();
						plugins.add(p);
					}
				} catch(Exception ex) {
					Log.w("Skipping plugin due to errors.", ex);
				}
			}
			
		} catch (IOException ex) {
			Log.e("Plugins couldn't be loaded!", ex);
		}
	}
	
}
