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

import me.champeau.ld.learn.util.LearningException;

import java.util.*;

/**
 * A gram tree is used to learn n-grams from texts, and is able to score a text. The n-gram data is represented as a
 * lexical tree. The representation is rather compact, but one could do better with annotated-DFAs.
 * <p/>
 * Not thread-safe.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class GramTreeBuilder extends AbstractGramTree {

    private static final long serialVersionUID = 4421643808498040212L;
    private boolean built = false;
    private double truncationThreshold = 1.0;

    /**
     * Builds an n-gram tree
     *
     * @param min minimal n-gram size
     * @param max maximum n-gram size
     */
    public GramTreeBuilder(int min, int max) {
        super(min, max);
        root = new NodeBuilder('\u0000');
    }

    public void setTruncationThreshold(final double truncationThreshold) {
        if (truncationThreshold<0 || truncationThreshold>1.0d) {
            throw new IllegalArgumentException("Truncation threshold must be comprised between 0.0 and 1.0");
        }
        this.truncationThreshold = truncationThreshold;
    }

    /**
     * Adds n-grams statistics to the n-gram tree.
     *
     * @param text character sequence to learn n-grams from.
     */
    public void learn(CharSequence text) {
        if (built) throw new IllegalStateException("N-Gram tree has already been built");
        NGramTokenizer tokenizer = new NGramTokenizer(text, min, max);
        for (CharSequence token : tokenizer) {
            addGram(token);
        }
    }

    /**
     * Adds a single n-gram to the n-gram tree.
     *
     * @param gram n-gram to be added to the tree.
     */
    private void addGram(CharSequence gram) {
        if (built) throw new IllegalStateException("N-Gram tree has already been built");
        NodeBuilder cur = (NodeBuilder) root;
        for (int i = 0; i < gram.length(); i++) {
            char c = gram.charAt(i);
            NodeBuilder next = (NodeBuilder) cur.getChild(c);
            if (next == null) next = cur.addTransition(c);
            cur = next;
            if (i == gram.length() - 1) cur.inc();
        }
        gramcount++;
    }

    /**
     * Optimizes the n-gram tree memory consumption.
     *
     * @return an immutable gram tree
     */
    public AbstractGramTree build() {
        built = true;
        final NodeBuilder nodeBuilder = (NodeBuilder) root;
        ArrayList<Integer> freqs = new ArrayList<Integer>();
        nodeBuilder.collectFreqs(freqs);
        Collections.sort(freqs);
        root = nodeBuilder.build(freqs.get((int) (freqs.size()*(1.0-truncationThreshold))));
        return new GramTreeImpl(root, min, max, gramcount);
    }


    /**
     * A node of the n-gram tree. Consists of a character, its frequency, and the list of followers.
     */
    private static class NodeBuilder extends AbstractNode implements Comparable<NodeBuilder> {
        private final static int DEFAULT_ALLOC = 64;

        int childcount;

        private NodeBuilder(char c) {
            this.c = c;
            this.freq = 0;
            this.childcount = 0;
        }

        public AbstractNode getChild(char c) {
            if (children == null) return null;
            for (int i = 0; i < childcount; i++) {
                if (children[i].c == c) return children[i];
                if (children[i].c > c) return null;
            }
            return null;
        }

        public NodeBuilder addTransition(char c) {
            NodeBuilder child = new NodeBuilder(c);
            if (children == null) {
                children = new NodeBuilder[DEFAULT_ALLOC];
            }
            if (childcount == children.length - 1) {
                // reallocate
                NodeBuilder[] realloc = new NodeBuilder[children.length + DEFAULT_ALLOC];
                System.arraycopy(children, 0, realloc, 0, children.length);
                children = realloc;
            }
            children[childcount] = child;
            childcount++;
            Arrays.sort(children, 0, childcount);
            return child;
        }

        private void inc() {
            if (freq==Integer.MAX_VALUE) {
                throw new LearningException("Maximum frequency is reached. N-Gram is too frequent in the corpus. Try to use a smaller corpus.");
            }
            freq++;
        }

        public int compareTo(NodeBuilder o) {
            return c - o.c;
        }

        /**
         * Builds an immutable n-gram tree from this builder data. The minimal frequency value is used to discard
         * n-grams which are supposed to be irrelevant for a language, reducing the total amount of memory required
         * to model a language.
         * @param minFreq the minimum number of occurrences an n-gram must have been seen to be considered relevant
         * @return an immutable n-gram tree
         */
        private AbstractNode build(final int minFreq) {
            if (childcount == 0) return new MinimalNode(c, freq, null);
            List<AbstractNode> children2 = new LinkedList<AbstractNode>();
            for (int i = 0; i < childcount; i++) {
                if (children[i].freq>=minFreq) children2.add(((NodeBuilder) children[i]).build(minFreq));
            }
            return new MinimalNode(c, freq, children2.isEmpty()?null:children2.toArray(new AbstractNode[children2.size()]));
        }

        public void collectFreqs(ArrayList<Integer> freqs) {
            freqs.add(freq);
            for (int i=0; i<childcount;i++) {
                ((NodeBuilder)children[i]).collectFreqs(freqs);
            }
        }

    }

    private static class MinimalNode extends AbstractNode {

        public MinimalNode(final char c, final int freq, final AbstractNode[] children) {
            this.c = c;
            this.freq = freq;
            this.children = children;
        }
    }
}
