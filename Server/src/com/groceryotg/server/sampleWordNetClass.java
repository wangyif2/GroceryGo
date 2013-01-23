package com.groceryotg.server;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: robert
 * Date: 21/01/13
 */
public class sampleWordNetClass extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WordNetDatabase wordNetDatabase = WordNetDatabase.getFileInstance();
        NounSynset nounSynset;
        NounSynset[] hyponyms;

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets("fly", SynsetType.NOUN);
        for (int i = 0; i < synsets.length; i++) {
            nounSynset = (NounSynset) (synsets[i]);
            hyponyms = nounSynset.getHyponyms();
            System.err.println(nounSynset.getWordForms()[0] +
                    ": " + nounSynset.getDefinition() + ") has " + hyponyms.length + " hyponyms");
        }
    }
}
