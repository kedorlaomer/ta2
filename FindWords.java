import java.io.*;
import java.util.*;


public class FindWords
{
    private File contentFile;
    public String contentFilePath;

    public boolean phraseSearchMode;
    public List<String> searchTokens;

    public Integer queryOccurrences;
    public Integer phraseOccurrences;
    private HashMap<Integer, List<Integer>> tokenPositionInDocs;

    public final String WORDS_DELIMITER = " ";
    public final String INDEX_ARG = "--index";


    private void init()
    {
        this.queryOccurrences = 0;
        this.phraseOccurrences = 0;
        this.phraseSearchMode = false;
        this.searchTokens = new ArrayList<String>();
        this.tokenPositionInDocs = new HashMap<Integer, List<Integer>>();
    }


    private FindWords()
    {
        this.init();
    }


    private void handleIndexArg() 
    {
        if (this.contentFilePath == null)
            return;

        contentFile = new File(this.contentFilePath);
        if (!this.contentFile.exists())
        {
            this.contentFile = null;
            System.err.println("Index file do not exists: " + this.contentFilePath);
        }
    }


    private void handleArgs(String[] args)
    {
        //TODO: Find a better name!
        boolean isNextArgContentFilePath = false;
        for (String arg: args)
            if (arg.equals(INDEX_ARG))
                isNextArgContentFilePath = true;
            else
            {
                if (isNextArgContentFilePath)
                {
                    this.contentFilePath = arg;
                    isNextArgContentFilePath = false;
                }
                else if (!this.phraseSearchMode)
                {
                    if (arg.indexOf(WORDS_DELIMITER) != -1)
                    {
                        // When we have found a phrase, we are ignoring the rest
                        this.searchTokens.clear();

                        for (String word: arg.split("\\s+"))
                            this.searchTokens.add(word);

                        this.phraseSearchMode = true;
                    } 
                    else
                        this.searchTokens.add(arg);
                }
            }

        if (this.searchTokens.size() == 0 && contentFilePath == null)
        {
            System.err.println("Give a search query or specify a content file.");
            System.exit(2);
        }

        this.handleIndexArg();
    }


    private boolean createIndecesIfNeed()
    {
        if (this.contentFile == null) 
            return false;

        try
        {
            XmlParser parser = new XmlParser(this.contentFile.getPath());
            IndexCreator ic = new IndexCreator(parser.get());
            System.out.println("file read");
            ic.writeIndices();
            System.out.println("index created");
        }
        catch (Throwable exc)
        {
            exc.printStackTrace();
        }
        return true;
    }


    private void setTokensOccurrences()
    {
        if (this.phraseSearchMode)
            for (Map.Entry<Integer, List<Integer>> entry : this.tokenPositionInDocs.entrySet())
                this.phraseOccurrences += entry.getValue().size();
        this.queryOccurrences = this.tokenPositionInDocs.size();
    }


    private void setIntersectedPmids(HashMap<Integer, List<Integer>> docs, String word)
    {
        ArrayList<Integer> intersectedPmids;
        ArrayList<Integer> newPositions;
        HashMap<Integer, List<Integer>> tokenInDocsCopy;

        tokenInDocsCopy = new HashMap<Integer, List<Integer>>(this.tokenPositionInDocs);
        intersectedPmids = new ArrayList(tokenInDocsCopy.keySet());
        intersectedPmids.retainAll(docs.keySet());

        if (intersectedPmids.size() == 0)
        {
            this.tokenPositionInDocs.clear();
            return;
        }

        for (Map.Entry<Integer, List<Integer>> entry : tokenInDocsCopy.entrySet())
        {
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            newPositions = new ArrayList<Integer>();

            if (intersectedPmids.contains(key))
            {
                if (this.phraseSearchMode)
                {
                    for (int position : value)
                        if (docs.get(key).contains(position + 1))
                            newPositions.add(position);
                    if (newPositions.size() > 0)
                        this.tokenPositionInDocs.put(key, newPositions);
                    else
                        this.tokenPositionInDocs.remove(key);
                }
            }
            else
                this.tokenPositionInDocs.remove(key);
        }
    }


    public void setResult()
    {
        try
        {
            InvertedIndex index = new InvertedIndex();

            List<PointerPair> tokenInfos;
            List<Integer> currentPositions;
            HashMap<Integer, List<Integer>> currentTokenPositionInDocs;
            boolean breakCycle = false;

            for (int i=0; i<this.searchTokens.size(); i++)
            {
                currentTokenPositionInDocs = new HashMap<Integer, List<Integer>>();
                currentPositions = new ArrayList<Integer>();

                tokenInfos = index.infoForToken(this.searchTokens.get(i));
                if (tokenInfos == null)
                {
                    this.tokenPositionInDocs.clear();
                    break;
                }

                for (PointerPair info : tokenInfos)
                {
                    if (this.phraseSearchMode)
                        currentPositions = index.tokenidsFromPosition(info.b);
                    currentTokenPositionInDocs.put(info.a, currentPositions);
                }
                if (i == 0)
                    this.tokenPositionInDocs = currentTokenPositionInDocs;
                else
                    this.setIntersectedPmids(currentTokenPositionInDocs, this.searchTokens.get(i));

                if (this.tokenPositionInDocs.size() == 0)
                    break;
            }

            this.setTokensOccurrences();
        }
        catch(IOException exc)
        {
            exc.printStackTrace();
        }
    }


    public void printResults()
    {
        System.out.println("The Pub Med ID of every match: ");
        for (Map.Entry<Integer, List<Integer>> entry : this.tokenPositionInDocs.entrySet())
            System.out.print(entry.getKey() + " ");
        System.out.println();

        System.out.println(
            "Total number of occurrences matching the query: " + this.queryOccurrences);

        if (this.phraseSearchMode)
            System.out.println(
                "Phrase Search. Total number of occurrences: " + this.phraseOccurrences);
    }


    public void printDebugInfo()
    {
        System.out.println("-------------DEBUG-START");
        System.out.println("query: " + this.searchTokens);
        System.out.println("phrase search mode: " + this.phraseSearchMode);
        System.out.println("content file: " + this.contentFile);
        System.out.println("-------------DEBUG-END");
    }


    public static void main(String[] args)
    {
        FindWords finder = new FindWords();

        finder.handleArgs(args);
        finder.createIndecesIfNeed();
        finder.setResult();
        finder.printResults();
        // finder.printDebugInfo();
    }
}
