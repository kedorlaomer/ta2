import java.io.*;
import java.util.*;


public class FindWords
{
    private File contentFile;
    public String contentFilePath;

    public boolean phraseSearchMode;
    public List<String> searchTokens;

    public Integer queryOccurency;
    private HashMap<Integer, List<Integer>> tokenPositionInDocs;

    public final String WORDS_DELIMITER = " ";
    public final String INDEX_ARG = "--index";


    private void init()
    {

        this.queryOccurency = 0;
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

        if (this.searchTokens.size() == 0)
        {
            System.err.println("Give a search query.");
            System.exit(1);
        }

        this.handleIndexArg();
    }


    private boolean createIndexes()
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

    private void setTokensOccurency()
    {
        for (Map.Entry<Integer, List<Integer>> entry : this.tokenPositionInDocs.entrySet())
        {
            this.queryOccurency += this.phraseSearchMode? entry.getValue().size() : 1;
        }
    }

    public void setResult()
    {
        try
        {
            InvertedIndex index = new InvertedIndex();

            List<PointerPair> tokenInfos;
            List<Integer> currentPositions = new ArrayList<Integer>();
            List<Integer> newPositions = new ArrayList<Integer>();
            boolean breakCycle = false;

            for (int i=0; i<this.searchTokens.size(); i++)
            {
                tokenInfos = null;
                try
                {
                    tokenInfos = index.infoForToken(this.searchTokens.get(i));
                }
                catch (IOException exc)
                {
                    exc.printStackTrace();
                }
                if (tokenInfos == null)
                    break;

                for (PointerPair info : tokenInfos)
                {
                    if (i == 0)
                        this.tokenPositionInDocs.put(info.a, index.tokenidsFromPosition(info.b));
                    else if (this.tokenPositionInDocs.containsKey(info.a))
                    {
                        if (this.phraseSearchMode)
                        {
                            currentPositions = index.tokenidsFromPosition(info.b);
                            for (int position : this.tokenPositionInDocs.get(info.a))
                                if (currentPositions.contains(position + 1))
                                    newPositions.add(position);
                            this.tokenPositionInDocs.put(info.a, newPositions);
                            
                            currentPositions = new ArrayList<Integer>();
                            newPositions = new ArrayList<Integer>();
                        }
                    }
                    else
                        this.tokenPositionInDocs.remove(info.a);
                }
                if (this.tokenPositionInDocs.size() == 0)
                    break;
            }

            this.setTokensOccurency();
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
            System.out.println(entry.getKey());

        if (this.phraseSearchMode)
            System.out.println(
                "Phrase Search. Total number of occurency: " + this.queryOccurency);
        else
            System.out.println(
                "Total number of occurency matching the query: " + this.queryOccurency);
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
        // finder.createIndexes();
        finder.setResult();
        finder.printResults();
        finder.printDebugInfo();
    }
}
