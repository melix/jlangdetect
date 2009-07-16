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

import java.util.Iterator;

/**
 * Iterates over a char sequence to produce n-grams. Requires both minimal and maximal gram length.
 */
public class NGramIterator implements Iterator<CharSequence> {
	private final CharSequence buffer;
	private final int max;

	private int pos;
	private int window;

	public NGramIterator(CharSequence buffer, int min, int max) {
		this.buffer = buffer;
		this.max = max;
		pos = -1;
		window = min;
	}

	public boolean hasNext() {
		boolean ok = pos+window<buffer.length();
		if (!ok) {
			ok = (window+1<=max) && (window+1<buffer.length());
		}
		return ok;
	}

	public CharSequence next() {
	    pos++;
		if (pos+window>buffer.length()) {
			pos = 0;
			window++;
		}
		if ((window>max)||(pos+window>buffer.length())) return null;
		return buffer.subSequence(pos, pos+window);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
