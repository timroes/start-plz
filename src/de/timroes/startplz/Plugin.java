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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tim Roes <mail@timroes.de>
 */
public abstract class Plugin {
	
	/**
	 * Returns the highest similarity of a query to several strings. This method
	 * will calculate the similarity between the query and each of the strings
	 * and return the highest of these.
	 * 
	 * @param query The search query (the string to compare against every other).
	 * @param compareTo Several strings to compare {@code query} against.
	 * @return The highest similarity of these.
	 */
	public final double getMaximumStringSimilarity(String query, String... compareTo) {
		List<Double> weights = new LinkedList<Double>();
		for(String s : compareTo) {
			weights.add(getStringSimiliarity(query, s));
		}
		return Collections.max(weights);
	}
	
	/**
	 * Calculates the similarity of two strings. The returning distance will be 
	 * from 0.0 (nearly no similarity) up to 1.0. So this method can be used
	 * to calculate the weight for {@link Result Results}, that just need 
	 * String similarity.
	 * 
	 * The used algorithm is described in: http://www.catalysoft.com/articles/StrikeAMatch.html
	 * 
	 * @param str1 The first string.
	 * @param str2 The second string.
	 * @return The similarity of the two strings.
	 */
	public final double getStringSimiliarity(String str1, String str2) {
		if(str1 == null || str2 == null || str1.trim().isEmpty() || str2.trim().isEmpty()) {
			return 0.0;
		}
		List<String> pairs1 = wordLetterPairs(str1.toLowerCase());
		List<String> pairs2 = wordLetterPairs(str2.toLowerCase());
		int intersection = 0;
		int union = pairs1.size() + pairs2.size();
		for(int i = 0; i < pairs1.size(); i++) {
			String pair1 = pairs1.get(i);
			for(int j = 0; j < pairs2.size(); j++) {
				String pair2 = pairs2.get(j);
				if(pair1.equals(pair2)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}
		return (2.0 * intersection) / union;
	}
	
	private String[] letterPairs(String str) {
		int numPairs = str.length() - 1;
		String[] pairs = new String[numPairs];
		for(int i = 0; i < numPairs; i++) {
			pairs[i] = str.substring(i, i + 2);
		}
		return pairs;
	}
	
	private List<String> wordLetterPairs(String str) {
		ArrayList<String> allPairs = new ArrayList<String>();
		// Tokenize the string by whitespaces
		String[] words = str.split("\\s");
		for(int w = 0; w < words.length; w++) {
			if(words[w].isEmpty()) {
				continue;
			}
			// Find the pairs of character in each word
			String[] pairsInWord = letterPairs(words[w]);
			allPairs.addAll(Arrays.asList(pairsInWord));
		}
		return allPairs;
	}
	
	public abstract List<? extends Result> search(String query);
	
	/**
	 * Refresh the data of your plugin. This will be called in intervals, so
	 * every plugin can update it's data. You should update data in that method,
	 * that takes too long to fetch directly in {@link #search(java.lang.String)}.
	 * The method is also guaranteed to be called, when the program starts. So
	 * you don't need to call it yourself from your constructor.
	 * 
	 * If you don't want this method to be run parallel to {@link #search(java.lang.String)}
	 * make sure to make both methods synchronized in your implementation.
	 */
	public void refresh() { }
	
}
