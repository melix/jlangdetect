/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.lingway.ld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps several n-gram trees in order to detect languages. The detection algorithm is really simple : it
 * queries the registered gram trees, and returns the language associated with the one which returns the best score.
 * <p/>
 * Such an algorithm requires that the corpus used for training look as identical as possible. Parallel corpus are good
 * candidates.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class LangDetector {
	private final static org.apache.log4j.Logger theLogger = org.apache.log4j.Logger.getLogger(LangDetector.class);
	
	private Map<String, GramTree> statsMap = new HashMap<String, GramTree>();

	public LangDetector() {
	}

	public void register(String lang, ObjectInputStream in) {
		try {
			statsMap.put(lang, (GramTree) in.readObject());
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void register(String lang, GramTree tree) {
		statsMap.put(lang, tree);
	}

	/**
	 * Performs a language detection, but limits the detection to the set of provided languages. This is useful when the
	 * detector has been trained with many languages, but you wish to discriminate between a smaller set of possible
	 * languages (or, you know that the document is either in english or french).
	 *
	 * @param aText				the text for which to detect the language
	 * @param languageRestrictions the set of languages the detector should be limited to
	 * @return the detected language
	 */
	public String detectLang(CharSequence aText, Set<String> languageRestrictions) {
		return detectLang(aText, false, languageRestrictions);
	}

	/**
	 * Performs a language detection, using the whole set of possible languages.
	 *
	 * @param aText   the text for which to detect the language
	 * @param explain if set to true, outputs debug information
	 * @return the detected language
	 */
	public String detectLang(CharSequence aText, boolean explain) {
		return detectLang(aText, explain, statsMap.keySet());
	}

	/**
	 * Performs a language detection, but limits the detection to the set of provided languages. This is useful when the
	 * detector has been trained with many languages, but you wish to discriminate between a smaller set of possible
	 * languages (or, you know that the document is either in english or french).
	 *
	 * @param aText				the text for which to detect the language
	 * @param explain			  if set to true, outputs debug information
	 * @param languageRestrictions the set of languages the detector should be limited to
	 * @return the detected language
	 */
	public String detectLang(CharSequence aText, boolean explain, Set<String> languageRestrictions) {
		double best = 0;
		String bestLang = null;
		for (Map.Entry<String, GramTree> entry : statsMap.entrySet()) {
			final String currentLanguage = entry.getKey();
			if (languageRestrictions.contains(currentLanguage)) {
				if (explain) {
					theLogger.info("---------- testing : " + currentLanguage + " -------------");
				}
				double score = entry.getValue().scoreText(aText, explain);
				if (explain) {
					theLogger.info("---------- result : " + currentLanguage + " : " + score + " -------------");
				}
				if (score > best) {
					best = score;
					bestLang = currentLanguage;
				}
			}
		}
		return bestLang;
	}
}

