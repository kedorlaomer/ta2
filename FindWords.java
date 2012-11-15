import java.io.*;
import java.util.*;


public class FindWords
{
    private static File contentFile;
    public static String contentFilePath;

    public static boolean phraseSearchMode = false;
    public static ArrayList<String> searchQuery;

    public static final String WORDS_DELIMITER = " ";
    public static final String INDEX_ARG = "--index";


    private FindWords(){}

    private static void handleIndexArg() 
    {
        if (contentFilePath == null)
            return;

        contentFile = new File(contentFilePath);
        if (!contentFile.exists())
        {
            contentFile = null;
            System.err.println("Index file do not exists: " + contentFilePath);
        }
    }

    private static void handleArgs(String[] args)
    {

        searchQuery = new ArrayList<String>();
        //TODO: Find a better name!
        boolean isNextArgContentFilePath = false;

        for (String arg: args)
            if (arg.equals(INDEX_ARG))
                isNextArgContentFilePath = true;
            else
            {
                if (isNextArgContentFilePath)
                {
                    contentFilePath = arg;
                    isNextArgContentFilePath = false;
                }
                else if (!phraseSearchMode)
                {
                    if (arg.indexOf(WORDS_DELIMITER) != -1)
                    {
                        // When we have found a phrase, we are ignoring the rest
                        searchQuery.clear();

                        for (String word: arg.split("\\s+"))
                            searchQuery.add(word);

                        phraseSearchMode = true;
                    } 
                    else
                        searchQuery.add(arg);
                }
            }

        if (searchQuery.size() == 0)
        {
            System.err.println("Give a search query.");
            System.exit(1);
        }

        FindWords.handleIndexArg();
    }

    private static boolean createIndexes()
    {
        if (contentFile == null) {
            return false;
        }

        // TODO: create Index Files here
        return true;
    }

    public static void printDebugInfo()
    {
        System.out.println("query: " + FindWords.searchQuery);
        System.out.println("phrase search mode: " + FindWords.phraseSearchMode);
        System.out.println("content file: " + FindWords.contentFile);
    }

    // public static void printResults(Object results)
    // {
    //     System.out.println("The Pub Med ID of every match: ");
    //     for (String pmid: results.pmids)
    //         System.out.println(pmid);

    //     if (phraseSearchMode)
    //     {
    //         System.out.println(
    //             "Phrase Search. Total number of occurency: " + results.phraseOccurency);
    //     }
    //     else
    //     {
    //         System.out.println(
    //             "Total number of occurency matching the query: " + results.queryOccurency);
    //     }
    // }

    public static void main(String[] args)
    {
        FindWords.handleArgs(args);

        boolean indexesCreated = FindWords.createIndexes();

        // Call search here
        // Object results = new Object();
        // FindWords.printResults(results);

        FindWords.printDebugInfo();
    }
}
