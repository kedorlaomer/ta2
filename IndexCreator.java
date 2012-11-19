import java.io.*;
import java.util.*;

/* erzeugt die drei Indizes index.dat, pmids.dat, tokenids.dat */

public class IndexCreator implements Comparator<PointerPair>
{
    private Map<String, List<PointerPair>> index;
    public IndexCreator(Map<String, List<PointerPair>> index)
    {
        this.index = index;
    }

    /*
     * Das Format von index.dat ist in HopscotchResident
     * dokumentiert.
     *
     * Die übrigen beiden Indices bestehen nur aus Folgen von
     * Integern.
     *
     * Das Format von pmids.dat lautet wie folgt: Jeder Eintrag
     * beginnt mit einer Anzahl, die angibt, wieviele Werte
     * folgen. Jeder Wert besteht aus zwei Integer p und q,
     * wobei p eine PMID und q ein Index in die Datei
     * tokenids.dat ist. Ein Eintrag ist folgendermaßen zu
     * interpretieren: Das entsprechende Token taucht in den
     * Dokumenten mit den aufgelisteten PMIDs auf; ferner gibt
     * der Index in tokenids.dat an, wo in dem Dokument das Wort
     * auftaucht.
     *
     * Das Format von tokenids.dat lautet wie folgt: Jeder
     * Eintrag beginnt mit einer Anzahl, die angibt, wieviele
     * Werte folgen. Jeder Wert besteht aus einem Integer, der
     * angibt, als wievieltes Token im Dokument das gegebene
     * Token auftaucht. Beachte: Das höherwertigste Bit des
     * Index wird mit Constants.ABSTRACT_MASK abmaskiert.
     *
     * Die Einträge in pmids.dat sind aufsteigend nach PMID
     * sortiert; die Einträge in tokenids.dat sind aufsteigend
     * nach ID sortiert (dabei wird das höherwertigste Bit
     * beachtet).
     */

    public void writeIndices() throws IOException
    {
        HopscotchResident<String, PointerPair> resident = new HopscotchResident<String, PointerPair>((int) (1.1*index.size()));
        DataOutputStream pmidStream = new DataOutputStream(new FileOutputStream(new File("pmids.dat")));
        DataOutputStream tokenidStream = new DataOutputStream(new FileOutputStream(new File("tokenids.dat")));

        for (Map.Entry<String, List<PointerPair>> entry : index.entrySet())
        {
            String token = entry.getKey();
            List<PointerPair> pointers = entry.getValue();
            PointerPair pair = new PointerPair(pmidStream.size(), tokenidStream.size());
            resident.put(token, pair);
            System.err.println("'" + token + "'");

            /*
            SortedSet<Integer> setOfPMIDs = new TreeSet<Integer>();
            for (PointerPair p : pointers)
                setOfPMIDs.add(p.a);

            /*
             * Schreibe Länge von setOfPMIDs nach pmidStream;
             * soviele PMIDs folgen noch. Beachte: Die Anzahl
             * Vorkommen von token im Korpus lässt sich nicht
             * allein dadurch errechnen, da ein Wort mehrmals
             * vorkommen kann.
             */

//          pmidStream.writeInt(setOfPMIDs.size());

//          for (int pmid : setOfPMIDs)
//          {
//              SortedSet<Integer> setOfTokenIDs = new TreeSet<Integer>();
//              for (PointerPair p : pointers)
//                  if (p.a == pmid)
//                      setOfTokenIDs.add(p.b);

//              /* 
//               * nun schreibe pmid und die Position in
//               * tokenidStream nach pmidStream; schreibe alle
//               * Token-IDs (zuvor ihre Anzahl) nach
//               * tokenidStream
//               */

//              pmidStream.writeInt(pmid);
//              pmidStream.writeInt(tokenidStream.size());

//              tokenidStream.writeInt(setOfTokenIDs.size());

//              for (int tokenID : setOfTokenIDs)
//                  tokenidStream.writeInt(tokenID);
//          }
        }

        pmidStream.close();
        tokenidStream.close();

        /* erzeuge index.dat */
        DataOutputStream indexStream = new DataOutputStream(new FileOutputStream(new File("index.dat")));
        resident.serialize(indexStream);
        indexStream.close();
    }

    /* vergleiche x, y gemäß ihrer PMIDs */
    public int compare(PointerPair x, PointerPair y)
    {
        return x.a - y.a;
    }

    private static PointerPair pair(int a, int b)
    {
        return new PointerPair(a, b);
    }

    private static List<PointerPair> list(PointerPair[] pointers)
    {
        return Arrays.asList(pointers);
    }

    /* trivialer Test */
    public static void main(String[] argv)
    {
        Map<String, List<PointerPair>> map = new Hashtable();
        map.put("ich", list(new PointerPair[] {pair(1, 12), pair(1, 44), pair(2, 13)}));
        map.put("the", list(new PointerPair[] {pair(5, 96), pair(5, 97)}));
        map.put("es", list(new PointerPair[] {}));

        try
        {
            IndexCreator creator = new IndexCreator(map);
            creator.writeIndices();
        }

        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
