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

package me.champeau.ld;
/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 27/06/11
 * Time: 09:51
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A language detector which includes the {@link EuroparlDetector Europarl detection resources} plus extra languages :
 * <ul>
 *     <li>Russian</li>
 *     <li>Chinese</li>
 *     <li>Japanese</li>
 *     <li>Korean</li>
 * </ul>
 *
 * The extra languages have been learnt thanks to the Project Gutenberg (www.gutenberg.org) resources which are not
 * sufficient for excellent accuracy but should be enough for most needs.
 *
 * Note that due to the lack of royalty-free corpora, those language profiles are subject to caution. For example,
 * russian and bulgarian can look very similar, and the detector is likely to fail.
 * 
 */
public class UberLanguageDetector extends LangDetector {
    private final static Logger theLogger = LoggerFactory.getLogger(UberLanguageDetector.class);

    public final static String[] EXTRA_LANGUAGES = {"ru","zh","ja","ko"};
    private final static UberLanguageDetector INSTANCE = new UberLanguageDetector();


    protected UberLanguageDetector() {
        super(EuroparlDetector.getInstance());
        ClassLoader loader = EuroparlDetector.class.getClassLoader();
        for (String lang : EXTRA_LANGUAGES) {
            try {
                register(lang, new ObjectInputStream(new BufferedInputStream(loader.getResourceAsStream("jlangdetect-extra/" + lang + "_tree.bin"))));
            } catch (IOException e) {
                theLogger.warn("Unable to read Europarl resources for language " + lang);
            }
        }
    }

    public static UberLanguageDetector getInstance() {
        return INSTANCE;
    }

    @Override
    public void register(final String lang, final AbstractGramTree tree) {
        if (INSTANCE!=null) throw new IllegalStateException("Cannot add languages to Europarl detector once loaded");
        super.register(lang, tree);
    }

    @Override
    public void register(final String lang, final ObjectInputStream in) {
        if (INSTANCE!=null) throw new IllegalStateException("Cannot add languages to Europarl detector once loaded");
        super.register(lang, in);
    }

}
