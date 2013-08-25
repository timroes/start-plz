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

import javax.swing.ImageIcon;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public abstract class Result {

	@Override
	public String toString() {
		return "Result{title=" + getTitle() + "}";
	}
	
	/**
	 * Creates an {@link ImageIcon} for a plugin. The icon file which name
	 * must be provided, must be in the subpackage 'icons' of the plugins package.
	 * 
	 * @param filename The filename of the icon.
	 * @return The icon.
	 */
	public final ImageIcon createPluginIcon(String filename) {
		String iconPath = "/".concat(getClass().getPackage().getName().replace('.', '/')).concat("/icons/");
		return new ImageIcon(getClass().getResource(iconPath.concat(filename)));
	}
		
	/**
	 * Returns a unique ID for that result, that will be used by the {@link PluginManager}
	 * to store the amount of times, the user has chosen that icon. By default this
	 * will be the title of the result. You can overwrite this method, to return
	 * another ID, that makes more sense for your specific Result implementation.
	 * 
	 * @return 
	 */
	public String getID() {
		return getTitle();
	}
	
	/**
	 * The weight of a special result indicating how good the result matches
	 * the query. 1.0 is the best match and 0.0 the worst fitting match.
	 * The results will be shown in descending weight order to the user.
	 * The result of this method won't be limited to 0.0 to 1.0, so make sure
	 * your implementation sticks to that limit to assure a good order of results.
	 * 
	 * If you return 0.0 for a result, this result will always stay at the bottom
	 * (together with other 0.0 results), since even the plugin specific weight
	 * multiplied by the {@link PluginManager} won't change the 0.0 anymore.
	 * 
	 * If you really want to a result to top or bottom, you can exceptionally use
	 * values outside of that range.
	 * 
	 * @return A weight between 0.0 and 1.0.
	 */
	public abstract double getWeight();
	
	public abstract String getTitle();
	
	public String getSubtitle() {
		return null;
	}
	
	public abstract ImageIcon getIcon();
	
	public abstract void execute();

}
