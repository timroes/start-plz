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

import de.timroes.startplz.Plugin;
import de.timroes.startplz.PluginManager;
import de.timroes.startplz.Result;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * The command plugin offers several commands the user can type to control the
 * application.
 * 
 * @author Tim Roes <mail@timroes.de>
 */
public class CommandPlugin extends Plugin {

	@Override
	public List<? extends Result> search(String query) {
		List<Result> result = new ArrayList<Result>(1);
		if("exit".equals(query)) {
			// Typing 'exit' will quit the application.
			result.add(new Result() {
				
				@Override
				public double getWeight() {
					return 1.0;
				}
				
				@Override
				public String getTitle() {
					return "Exit";
				}

				@Override
				public String getSubtitle() {
					return "Close this application";
				}

				@Override
				public ImageIcon getIcon() {
					return createPluginIcon("exit.png");
				}

				@Override
				public void execute() {
					System.exit(0);
				}
				
			});
		} else if("refresh".equals(query)) {
			// Typing 'refresh' call a manual refresh on all plugins.
			result.add(new Result() {

				@Override
				public double getWeight() {
					return 1.0;
				}

				@Override
				public String getTitle() {
					return "Refresh";
				}

				@Override
				public String getSubtitle() {
					return "Refreshes all data";
				}

				@Override
				public ImageIcon getIcon() {
					return createPluginIcon("refresh.png");
				}

				@Override
				public void execute() {
					PluginManager.get().refresh();
				}
				
			});
		}
		return result;
	}
	
}
