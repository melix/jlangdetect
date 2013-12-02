/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.champeau.ld.learn.util;

import me.champeau.ld.AbstractGramTree;
import me.champeau.ld.GramTreeBuilder;
import me.champeau.ld.LangDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A simple learning tool which takes a directory as input, and another directory as output. The input directory
 * must consist of subdirectories which name correspond to a language to learn. Each language directory is supposed
 * to contain a list of plain text files encoded in UTF-8.
 *
 * The resulting output directory will
 * consist of files named : [lang]_tree.bin
 *
 * Training takes less than 1 minute/language on my computer, with a quad core processor. The loader has been optimized
 * for multi-core systems.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class DirectoryLearning {
    private final static Logger theLogger = LoggerFactory.getLogger(DirectoryLearning.class);

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
				sb.append(line).append('\n');
			}
			reader.close();
		} catch (IOException e) {
			theLogger.error("Unable to read file : " + aFile);
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
	private static Map<String,AbstractGramTree> readCorpus(final File srcDir, final File dstDir, String[] langs) {
		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);
		theLogger.info("Parallel processing of "+threads+" languages over "+langs.length+"...");
		List<Future<?>> tasks = new LinkedList<Future<?>>();
		final Map<String,AbstractGramTree> trees = new ConcurrentHashMap<String, AbstractGramTree>();
		for (final String lang : langs) {
			tasks.add(service.submit(new Runnable() {
				public void run() {
					theLogger.info("Processing directory " + lang);
					GramTreeBuilder tree = new GramTreeBuilder(1, 3);
                    tree.setTruncationThreshold(0.1d);
                    if (lang.equals("ru")) {
                        tree.setTruncationThreshold(0.2d);
                    }
					File sourceFiles = new File(srcDir, lang);
					File[] files = sourceFiles.listFiles();
					int cpt = 0;
					for (File file : files) {
                        final String text = readSingleFile(file);
                        final String[] lines = text.split("\n");
                        for (String line : lines) {
                            tree.learn(line);
                        }
						cpt++;
						if (cpt % 20 == 0) {
							theLogger.info("Processed " + (100 * cpt / files.length) + "% of " + lang);
						}
					}
                    final AbstractGramTree build = tree.build();
                    trees.put(lang, build);
					theLogger.info("Saving tree : "+lang);
					File dst = new File(dstDir, lang+"_tree.bin");
					try {
						ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dst)));
						out.writeObject(build);
						out.close();
					} catch (IOException e) {
						theLogger.error("Unable to write lang tree "+lang,e);
					}
					theLogger.info("Lang "+ lang+" complete !");
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

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage : java " + DirectoryLearning.class.getCanonicalName() + " <sourcedir> <destdir>");
			System.exit(-1);
		}
		File srcDir = new File(args[0]);
		File dstDir = new File(args[1]);
		dstDir.mkdirs();
		final String[] langs = srcDir.list();
		LangDetector detector = new LangDetector();
		Map<String,AbstractGramTree> trees = readCorpus(srcDir, dstDir, langs);
		for (Map.Entry<String, AbstractGramTree> entry : trees.entrySet()) {
			detector.register(entry.getKey(), entry.getValue());
		}
	}

}
