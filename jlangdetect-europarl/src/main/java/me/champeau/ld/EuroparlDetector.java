package me.champeau.ld;
/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 26/06/11
 * Time: 22:35
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * An extension of the language detector which automatically loads europarl resources from classpath, thus supports
 * detecting the language of documents in the following languages :
 * <p/
 * <ul>
 * <li>Bulgarian</li>
 * <li>Czech</li>
 * <li>Danish</li>
 * <li>German</li>
 * <li>Greek</li>
 * <li>English</li>
 * <li>Spanish</li>
 * <li>Estonian</li>
 * <li>Finnish</li>
 * <li>French</li>
 * <li>Hungarian</li>
 * <li>Italian</li>
 * <li>Lithuanian</li>
 * <li>Latvian</li>
 * <li>Dutch</li>
 * <li>Polish</li>
 * <li>Portuguese</li>
 * <li>Romanian</li>
 * <li>Slovak</li>
 * <li>Slovene</li>
 * <li>Swedish</li>
 * </ul>
 * <p/>
 * The detector has been trained thanks to the resources available from http://www.statmt.org/europarl/.
 * <p/>
 * See Europarl: A Parallel Corpus for Statistical Machine Translation, Philipp Koehn, MT Summit 2005
 *
 * @author Cedric Champeau
 */
public class EuroparlDetector extends LangDetector {
    private final static Logger theLogger = LoggerFactory.getLogger(EuroparlDetector.class);

    private final static String[] EUROPARL_LANGUAGES = {
            "bg",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "et",
            "fi",
            "fr",
            "hu",
            "it",
            "lt",
            "lv",
            "nl",
            "pl",
            "pt",
            "ro",
            "sk",
            "sl",
            "sv"
    };

    private final static EuroparlDetector INSTANCE = new EuroparlDetector();

    protected EuroparlDetector() {
        super();
        ClassLoader loader = EuroparlDetector.class.getClassLoader();
        for (String lang : EUROPARL_LANGUAGES) {
            try {
                register(lang, new ObjectInputStream(new BufferedInputStream(loader.getResourceAsStream("europarl-ld/" +lang+"_tree.bin"))));
            } catch (IOException e) {
                theLogger.warn("Unable to read Europarl resources for language "+lang);
            }
        }
    }

    public static EuroparlDetector getInstance() {
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
