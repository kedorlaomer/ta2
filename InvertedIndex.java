import java.util.*;
import java.io.*;

public class InvertedIndex
{
    RandomAccessFile pmids, tokenids;
    HopscotchPersistent index;

    public InvertedIndex() throws IOException
    {
        index = new HopscotchPersistent(new RandomAccessFile("index.dat", "r"));
        pmids = new RandomAccessFile("pmids.dat", "r");
        tokenids = new RandomAccessFile("tokenids.dat", "r");
    }

    /*
     * Gibt alle PMIDs (in sortierter Reihenfolge) zurück, in
     * welchen token vorkommt.
     */

    public List<Integer> pmidsForToken(String token) throws IOException
    {
        PointerPair pair = index.get(token);
        pmids.seek(pair.a);

        int howMany = pmids.readInt();
        List<Integer> rv = new ArrayList<Integer>(howMany);

        for (int i = 0; i < howMany; i++)
        {
            rv.add(pmids.readInt());
            pmids.readInt(); // ignoriere Zeiger nach tokenids.dat
        }

        return rv;
    }

    /*
     * Ähnlich wie pmidsForToken, nur dass hier auch die Zeiger
     * nach tokenids.dat zurückgegeben werden.
     */

    public List<PointerPair> infoForToken(String token) throws IOException
    {
        PointerPair pair = index.get(token);
        pmids.seek(pair.a);

        int howMany = pmids.readInt();
        List<PointerPair> rv = new ArrayList<PointerPair>(howMany);

        for (int i = 0; i < howMany; i++)
            rv.add(new PointerPair(pmids.readInt(), pmids.readInt()));

        return rv;
    }

    /*
     * Findet die Token-IDs für dasjenige Token, dessen Zeiger
     * nach tokenids.dat position lautet.
     */

    public List<Integer> tokenidsFromPosition(int position) throws IOException
    {
        tokenids.seek(position);

        int howMany = tokenids.readInt();
        List<Integer> rv = new ArrayList<Integer>(howMany);

        for (int i = 0; i < howMany; i++)
            rv.add(tokenids.readInt());

        return rv;
    }

    public static void main(String[] argv)
    {
        String filename;
        if (argv.length != 1)
        {
            System.err.println("usage: java InvertedIndex <file to index>");
            System.exit(2);
        }

        filename = argv[0];

        try
        {
            XmlParser parser = new XmlParser(filename);
            IndexCreator ic = new IndexCreator(parser.get());
            System.err.println("file read");
            ic.writeIndices();
            System.err.println("index created");
            InvertedIndex index = new InvertedIndex();

            String token = "medical";
            List<PointerPair> infos = index.infoForToken(token);

            for (PointerPair info : infos)
                System.out.println(info);
        }

        catch (Throwable exc)
        {
            exc.printStackTrace();
        }
    }
}
