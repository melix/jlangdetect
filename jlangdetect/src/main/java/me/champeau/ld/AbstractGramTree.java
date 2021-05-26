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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A gram tree is used to learn n-grams from texts, and is able to score a text.
 * The n-gram data is represented as a lexical tree. The representation is rather compact, but one
 * could do better with annotated-DFAs.
 *
 * Not thread-safe.
 *
 */
public abstract class AbstractGramTree implements Serializable {
private final static Logger theLogger = LoggerFactory.getLogger(AbstractGramTree.class);
    
    private static final long serialVersionUID = 3284917449023378874L;
    protected AbstractNode root;
    protected long gramcount;
    protected int min;
    protected int max;

    protected AbstractGramTree(int min, int max) {
        this.max = max;
        this.min = min;
        gramcount = 0;
    }

    protected AbstractGramTree(int min, int max, long gramcount) {
        this.max = max;
        this.min = min;
        this.gramcount = gramcount;
    }

    /**
     * Returns a score for the input sequence against this n-gram tree.
     *
     * @param text the text to be checked
     * @return a score
     */
    public double scoreText(CharSequence text) {
        NGramTokenizer tokenizer = new NGramTokenizer(text, min, max);
        double tot = 0;
        for (CharSequence charSequence : tokenizer) {
            double s = scoreGram(charSequence);
            if (theLogger.isDebugEnabled()) {
                theLogger.debug(charSequence + " scores " + s);
            }
            tot += s;
        }
        double score = tot / Math.log(gramcount);
        if (theLogger.isDebugEnabled()) {
            theLogger.debug(text + ", total " + tot + "/" + Math.log(gramcount) + "=" + score);
        }
        return score;
    }

    private double scoreGram(CharSequence gram) {
        AbstractNode cur = root;
        for (int i=0; i<gram.length();i++) {
            char c = gram.charAt(i);
            AbstractNode next = cur.getChild(c);
            if (next==null) return 0;
            cur = next;
        }
        return Math.log(cur.freq);
    }

    /**
     * Base class for an n-gram tree node.
     */
    protected static abstract class AbstractNode implements Serializable {
        protected int freq;
        protected AbstractNode[] children;
        char c;

        public AbstractNode getChild(char c) {
            if (children==null) return null;
            final int childcount = children.length;
            for (int i=0; i<childcount;i++) {
                if (children[i].c==c) return children[i];
                if (children[i].c>c) return null;
            }
            return null;
        }

    }
}
