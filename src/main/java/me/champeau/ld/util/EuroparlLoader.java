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

package me.champeau.ld.util;

import me.champeau.ld.GramTree;
import me.champeau.ld.LangDetector;

import java.io.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import test.TestLangDetection;

/**
 * Parses the Europarl corpus (http://www.statmt.org/europarl/). This corpus consists of (parallel) translations
 * of European Parliament proceedings for the 1996-2006 period. It is a perfect candidate for our learning
 * algorithm, with up to 44 million words per language.
 *
 * Training takes less than 5 minutes on my computer, with a quad core processor. The loader has been optimized
 * for multi-core systems.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class EuroparlLoader {

	/**
	 * Reads a single EPPPC file, strips XML lines and returns a single string containing raw text.
	 * @param aFile a EPPPC text file, encoded in UTF-8.
	 * @return raw text
	 */
	private static String readSingleFile(File aFile) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(aFile), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("<")) {
					sb.append(line).append('\n');
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Unable to read file : " + aFile);
			return null;
		}
		return sb.toString();
	}

	/**
	 * Returns a map (lang code -> gram tree) of gram trees
	 * @param srcDir source directory where to find language specific directories
	 * @param dstDir output directory for compiled n-grams trees
	 * @param langs list of languages to be compiled
	 * @return the map of trees
	 */
	private static Map<String,GramTree> readCorpus(final File srcDir, final File dstDir, String[] langs) {
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);
		System.out.println("Parallel processing of "+threads+" languages over "+langs.length+"...");
		List<Future<?>> tasks = new LinkedList<Future<?>>();
		final Map<String,GramTree> trees = new ConcurrentHashMap<String, GramTree>();
		for (final String lang : langs) {
			tasks.add(service.submit(new Runnable() {
				public void run() {
					System.out.println("Processing directory " + lang);
					GramTree tree = new GramTree(1, 3);
					File sourceFiles = new File(srcDir, lang);
					File[] files = sourceFiles.listFiles();
					int cpt = 0;
					for (File file : files) {
						tree.learn(readSingleFile(file));
						cpt++;
						if (cpt % 20 == 0) {
							System.out.println("Processed " + (100 * cpt / files.length) + "% of " + lang);
						}
					}
					tree.compress();
					trees.put(lang, tree);
					System.out.println("Saving tree : "+lang);
					File dst = new File(dstDir, lang+"_tree.bin");
					try {
						ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dst)));
						out.writeObject(tree);
						out.close();
					} catch (IOException e) {
						System.err.println("Unable to write lang tree "+lang);
						e.printStackTrace();
					}
					System.out.println("Lang "+ lang+" complete !");
				}
			}));
		}
		// passive wait
		for (Future<?> task : tasks) {
			try {
				task.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		service.shutdown();
		return trees;
	}

	public static void main2(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage : java " + EuroparlLoader.class.getCanonicalName() + " <sourcedir> <destdir>");
			System.exit(-1);
		}
		File srcDir = new File(args[0]);
		File dstDir = new File(args[1]);
		dstDir.mkdirs();
		final String[] langs = srcDir.list();
		LangDetector detector = new LangDetector();
		Map<String,GramTree> trees = readCorpus(srcDir, dstDir, langs);
		for (Map.Entry<String, GramTree> entry : trees.entrySet()) {
			detector.register(entry.getKey(), entry.getValue());
		}

		TestLangDetection.test(detector);
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		File in = new File(args[0]);
		LangDetector detector = new LangDetector();
		for (File file : in.listFiles()) {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
			detector.register(file.getName().substring(0,2), (GramTree) ois.readObject());
			ois.close();
		}
		TestLangDetection.test(detector);
	}

}
