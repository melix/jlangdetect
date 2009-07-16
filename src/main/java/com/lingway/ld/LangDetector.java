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

import java.util.Map;
import java.util.HashMap;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * This class wraps several n-gram trees in order to detect languages. The detection algorithm is really
 * simple : it queries the registered gram trees, and returns the language associated with the one which
 * returns the best score.
 * 
 * Such an algorithm requires that the corpus used for training look as identical as possible. Parallel corpus
 * are good candidates.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class LangDetector {
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

	public String detectLang(CharSequence aText, boolean explain) {
		double best = 0;
		String bestLang = null;
		for (Map.Entry<String, GramTree> entry : statsMap.entrySet()) {
			if (explain) {
				System.out.println("---------- testing : "+entry.getKey()+" -------------");
			}
			double score = entry.getValue().scoreText(aText, explain);
			if (explain) {
				System.out.println("---------- result : "+entry.getKey()+" : "+score+" -------------");
			}
			if (score>best) {
				best = score;
				bestLang = entry.getKey();
			}
		}
		return bestLang;
	}
}

