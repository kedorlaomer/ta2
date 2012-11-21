import java.io.*;
import java.util.*;

public class WordFinder implements Constants
{
    private InvertedIndex index = null;

    public static void main(String[] argv) throws IOException
    {
        WordFinder finder = new WordFinder();
        List<PointerPair[]> pairs = finder.querySearch(argv);
        for (PointerPair[] pair : pairs)
        {
            int howMany = 0;

            howMany = finder.countPhraseOccurences(pair);

            if (howMany > 0)
                System.out.println(pair[0].a + ": " + howMany + (howMany == 1? " time" : " times"));
        }

        System.out.println(finder.countPhraseOccurences(pairs) + " in total.");
    }

    public WordFinder() throws IOException
    {
        index = new InvertedIndex();
    }

    /*
     * Gibt eine Liste aller PMIDs von Dokumenten zurück, welche
     * alle Tokens enthalten. Diese sind als PointerPair[]
     * kodiert, wobei
     */

    private List<PointerPair[]> querySearch(String[] tokens) throws IOException
    {
        List<PointerPair>[] fromIndex = (List<PointerPair>[])
            java.lang.reflect.Array.newInstance(List.class, new int[] {tokens.length});
        List<PointerPair[]> rv = new LinkedList<PointerPair[]>();

        /* 
         * positions[i] zeigt in fromIndex[i] und gibt an, das wievielte Element
         * von fromIndex[i] wir betrachten
         */

        int[] positions = new int[tokens.length];

        int maxPMID = -1; // PMIDs sind immer positiv

        for (int i = 0; i < tokens.length; i++)
        {
            positions[i] = 0;
            fromIndex[i] = index.infoForToken(tokens[i]);
        }

        do
        {
            for (int i = 0; i < tokens.length; i++)
            {
                int otherPMID = fromIndex[i].get(positions[i]).a;

                if (otherPMID > maxPMID)
                {
                    maxPMID = otherPMID;
                    i = 0;
                }

                else if (otherPMID < maxPMID)
                {
                    while (otherPMID < maxPMID && positions[i] != fromIndex[i].size()-1)
                    {
                        positions[i]++;
                        otherPMID = fromIndex[i].get(positions[i]).a;
                    }

                    if (otherPMID < maxPMID)
                    {
                        for (int j = 0; j < tokens.length; j++)
                            if (positions[j] != fromIndex[j].size()-1)
                            {
                                positions[j]++;
                                break;
                            }
                    }
                }
            }

            boolean shouldReturn = true;
            for (int i = 0; i < tokens.length && shouldReturn; i++)
                shouldReturn = maxPMID == fromIndex[i].get(positions[i]).a;

            if (shouldReturn)
            {
                PointerPair[] pp = new PointerPair[tokens.length];
                for (int i = 0; i < tokens.length; i++)
                    pp[i] = fromIndex[i].get(positions[i]);

                rv.add(pp);
                maxPMID++;
            }

            boolean finished = false;
            for (int i = 0; i < tokens.length && !finished; i++)
                finished = positions[i] == fromIndex[i].size()-1;

            if (finished)
                return rv;
        }
        while (true);
    }

    /*
     * pairs sollte nur PointerPairs enthalten, welche zu demselben Dokument
     * (selbe PMID, selbes pairs[i].a) gehören; diese Funktion zählt, wie oft
     * diese Vorkommen direkt aufeinander folgen, d. h., ob sie eine Phrase
     * bilden (gemäß der Ordnung, in welcher die Wörter in pairs vorkommen)
     */

    private int countPhraseOccurences(PointerPair[] pairs) throws IOException
    {
        List<Integer>[] tokenids = (List<Integer>[])
            java.lang.reflect.Array.newInstance(List.class, new int[] {pairs.length});
        int[] positions = new int[pairs.length];
        int rv = 0;

        for (int i = 0; i < pairs.length; i++)
        {
            positions[i] = 0;
            tokenids[i] = index.tokenidsFromPosition(pairs[i].b);
        }

        int maxPos = -1;

        do
        {
            for (int i = 0; i < pairs.length; i++)
            {
                int otherPos = tokenids[i].get(positions[i])-i;
                if (otherPos > maxPos)
                {
                    maxPos = otherPos;
                    i = 0;
                }

                else if (otherPos < maxPos)
                {
                    while (otherPos < maxPos && positions[i] != tokenids[i].size()-1)
                    {
                        positions[i]++;
                        otherPos = tokenids[i].get(positions[i])-i;
                    }

                    if (otherPos < maxPos)
                    {
                        for (int j = 0; j < pairs.length; j++)
                            if (positions[j] != tokenids[j].size()-1)
                            {
                                positions[j]++;
                                break;
                            }
                    }
                }
            }

            boolean shouldReturn = true;
            for (int i = 0; i < pairs.length && shouldReturn; i++)
                shouldReturn = maxPos+i == tokenids[i].get(positions[i]);

            if (shouldReturn)
            {
                rv++;
                maxPos++;
            }

            boolean finished = false;
            for (int i = 0; i < pairs.length && !finished; i++)
                finished = positions[i] == tokenids[i].size()-1;

            if (finished)
                return rv;

        } 
        while (true);
    }

    private int countPhraseOccurences(List<PointerPair[]> list) throws IOException
    {
        int rv = 0;
        for (PointerPair[] pairs : list)
            rv += countPhraseOccurences(pairs);

        return rv;
    }
}

/* 
 * Dokument 142570 ist problematisch; allogeneic und lymphocytes in phrase
 * query 
 */
