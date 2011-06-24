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

package me.champeau.ld;

import java.util.Arrays;
import java.io.Serializable;

/**
 * A gram tree is used to learn n-grams from texts, and is able to score a text.
 * The n-gram data is represented as a lexical tree. The representation is rather compact, but one
 * could do better with annotated-DFAs.
 *
 * Not thread-safe.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class GramTree implements Serializable {

	private final Node root;
	private long gramcount;
	private int min,max;

	/**
	 * Builds an n-gram tree
	 * @param min minimal n-gram size
	 * @param max maximum n-gram size
	 */
	public GramTree(int min, int max) {
		root = new Node('\u0000');
		gramcount = 0;
		this.min = min;
		this.max = max;
	}

	/**
	 * Adds n-grams statistics to the n-gram tree.
	 * @param text character sequence to learn n-grams from.
	 */
	public void learn(CharSequence text) {
		NGramTokenizer tokenizer = new NGramTokenizer(text, min, max);
		for (CharSequence token : tokenizer) {
			addGram(token);
		}
	}

	/**
	 * Adds a single n-gram to the n-gram tree.
	 * @param gram n-gram to be added to the tree.
	 */
	private void addGram(CharSequence gram) {
		Node cur = root;
		for (int i=0; i<gram.length();i++) {
			char c = gram.charAt(i);
			Node next = cur.getChild(c);
			if (next==null) next = cur.addTransition(c);
			cur = next;
			if (i==gram.length()-1) cur.inc();			
		}
		gramcount++;
	}

	/**
	 * Optimizes the n-gram tree memory consumption.
	 */
	public void compress() {
		root.compress();
	}

	/**
	 * Returns a score for the input sequence against this n-gram tree.
	 * @param text the text to be checked
	 * @param explain if true, will output explanations about the score
	 * @return a score
	 */
	public double scoreText(CharSequence text, boolean explain) {
		NGramTokenizer tokenizer = new NGramTokenizer(text, min, max);
		double tot = 0;
		for (CharSequence charSequence : tokenizer) {
			double s = scoreGram(charSequence);
			if (explain) {
				System.out.println(charSequence + " scores "+s);
			}
			tot += s;
		}
		double score = tot / Math.log(gramcount);
		if (explain) {
			System.out.println(text+", total "+tot + "/"+Math.log(gramcount)+"="+score);
		}
		return score;
	}

	private double scoreGram(CharSequence gram) {
		Node cur = root;
		for (int i=0; i<gram.length();i++) {
			char c = gram.charAt(i);
			Node next = cur.getChild(c);
			if (next==null) return 0;
			cur = next;
		}
		return Math.log(cur.freq);
	}

	/**
	 * A node of the n-gram tree. Consists of a character, its frequency, and the list of followers.
	 */
	private static class Node implements Serializable, Comparable<Node> {
		private final static int DEFAULT_ALLOC = 64;

		char c;
		Node[] children;
		long freq;
		int childcount;

		private Node(char c) {
			this.c = c;
			this.freq = 0;
			this.childcount = 0;
		}

		public Node addTransition(char c) {
			Node child = new Node(c);
			if (children==null) {
				children = new Node[DEFAULT_ALLOC];
			}
			if (childcount==children.length-1) {
				// reallocate
				Node[] realloc = new Node[children.length+DEFAULT_ALLOC];
				System.arraycopy(children, 0, realloc, 0, children.length);
				children = realloc;
			}
			children[childcount] = child;
			childcount++;
			Arrays.sort(children, 0, childcount);
			return child;
		}

		private void inc() {
			freq++;
		}

		public int compareTo(Node o) {
			return c-o.c;
		}

		public Node getChild(char c) {
			for (int i=0; i<childcount;i++) {
				if (children[i].c==c) return children[i];
				if (children[i].c>c) return null;
			}
			return null;
		}

		private void compress() {
			if (childcount==0) return;
			Node[] children2 = new Node[childcount];
			System.arraycopy(children,0,children2,0,childcount);
			children = children2;
			for (Node child : children) {
				child.compress();
			}
		}
	}
}
