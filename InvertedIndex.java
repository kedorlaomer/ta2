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
        try
        {
            InvertedIndex index = new InvertedIndex();
            List<Integer> l = index.tokenidsFromPosition(20);
            for (int i : l)
                System.out.println(i);
        }

        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }
}
