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

package com.champeau.ld;

import me.champeau.ld.AbstractGramTree;
import me.champeau.ld.EuroparlDetector;
import me.champeau.ld.LangDetector;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;

import static org.testng.Assert.assertEquals;
/**
 * User: cedric
 * Date: 21 sept. 2008
 * Time: 16:19:48
 */

/**
 * A very simple test class aimed at testing that simple Europarl learning should be sufficient for regular
 * european languages.
 */
public class TestLangDetection {

    @Test
	public void shouldDetectLanguages() {
		String[][] texts = new String[][] {
				new String[] {"un texte en français","fr"},
				new String[] {"a text in english","en"},
				new String[] {"un texto en español","es"},
				new String[] {"un texte un peu plus long en français","fr"},
				new String[] {"a text a little longer in english","en"},
				new String[] {"a little longer text in english","en"},
				new String[] {"un texto un poco más largo en español","es"},
				new String[] {"J'aime les bisounours !","fr"},
				new String[] {"Bienvenue à Montmartre !", "fr"},
				new String[] {"Welcome to London !", "en"},
				new String[] {"un piccolo testo in italiano", "it"},
				new String[] {"Du kan blive medlem ved at melde dig ind her.", "da"},
				new String[] {"Kaasotsustamismenetlusel vastu võetud aktide allkirjastamine", "et"},
				new String[] {"μια μικρή ελληνική γλώσσα", "el"},
				new String[] {"На 16 юни в 11.00 ч. сутринта местно време в щата Аляска, САЩ, е усетено", "bg"},
				new String[] {"Směrnice navíc zakáže nadstandardní zpoplatnění tzv. zákaznických linek.", "cs"},
				new String[] {"Emellett igény fogalmazódott meg az iparági önszabályozási és belső konfliktuskezelési, valamint a magyar vasút külföldi vasutak szervezeteivel fennálló kapcsolat-tartási feladatok ellátására is.", "hu"},
				new String[] {"een kleine Nederlandse tekst", "nl"},
				new String[] {"Matching sur des lexiques", "fr"},
				new String[] {"Tarybos perduoti susitarimų tekstai", "lt"},
				new String[] {"Koplēmuma procedūrā pieņemto tiesību aktu", "lv"},
				new String[] {"Utworzenie komisji śledczej i komisji tymczasowej", "pl"},
				new String[] {"Cursul dat rezoluţiilor Parlamentului: a se vedea procesul-verbal", "ro"},
				new String[] {"Skončenie rokovania", "sk"},
                new String[] {"Fru talman! Rörande en ordningsfråga.", "sv"},
                new String[] {"Homofobija v Evropi", "sl"},
				new String[] {"Matching on lexicons", "en"},
				new String[] {"Une première optimisation consiste à ne tester que les sous-chaînes de taille compatibles avec le lexique.", "fr"},
				new String[] {"A otimização é a primeira prova de que não sub-canais compatível com o tamanho do léxico.", "pt"},
				new String[] {"Ensimmäinen optimointi ei pidä testata, että osa-kanavien kanssa koko sanakirja.", "fi"},
		};

        EuroparlDetector detector = EuroparlDetector.getInstance();

		for (String[] text : texts) {
			String det = detector.detectLang(text[0]);
			System.out.println("langof(\""+text[0]+"\") = " + det + " : " + (det.equals(text[1])?"OK":"Error"));
			assertEquals(det,text[1]);
		}

    }

    @Test
    public void testScores() {
        final Collection<LangDetector.Score> scores = EuroparlDetector.getInstance().scoreLanguages("\u0001");
        System.out.println("scores = " + scores);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldFailRegisteringLanguage() {
        EuroparlDetector.getInstance().register("lang", new AbstractGramTree(0,0,0) {});
    }
}
