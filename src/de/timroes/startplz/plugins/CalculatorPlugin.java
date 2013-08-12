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

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.timroes.startplz.Plugin;
import de.timroes.startplz.Result;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public class CalculatorPlugin extends Plugin {
	
	@Override
	public List<? extends Result> search(String query) {
		
		List<CalculationResult> result = new ArrayList<CalculationResult>(1);
		
		try {
			Calculable calc = new ExpressionBuilder(query).build();
			double res = calc.calculate();
			result.add(new CalculationResult(res, query));
		} catch (Exception ex) {
			// Invalid expression. Don't return any result.
		}
		
		return result;
		
	}

	private class CalculationResult extends Result {

		private double result;
		private String expression;
		
		public CalculationResult(double result, String expression) {
			this.result = result;
			this.expression = expression;
		}
		
		@Override
		public String getID() {
			return CalculationResult.class.getName();
		}

		/**
		 * Pin the calculation result to the top.
		 * @return 100.0
		 */
		@Override
		public double getWeight() {
			return 100.0;
		}

		@Override
		public String getTitle() {
			return String.valueOf(result);
		}

		@Override
		public String getSubtitle() {
			return "= " + expression;
		}

		@Override
		public ImageIcon getIcon() {
			return createPluginIcon("calc.png");
		}

		/**
		 * Copies the result to the system clipboard and selection.
		 */
		@Override
		public void execute() {
			StringSelection sel = new StringSelection(String.valueOf(result));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
			Toolkit.getDefaultToolkit().getSystemSelection().setContents(sel, null);
		}
		
	}
	
}
