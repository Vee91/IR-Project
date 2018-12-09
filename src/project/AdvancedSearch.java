package project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancedSearch
{
    // Extra credit

    private static Map<String, List<DTF>> ii;
    private static Map<Integer, String> docIdMap;
    private static Map<Integer, Integer> termCount;

    public static void main(String[] args) throws IOException
    {
        // TODO Auto-generated method stub
        AdvancedSearchIndexer index = new AdvancedSearchIndexer();
        index.generateAdvanceSearchIndexer();

        ii = AdvancedSearchIndexer.getIi();
        docIdMap = AdvancedSearchIndexer.getDocIdMap();
        termCount = AdvancedSearchIndexer.getTermCount();

        boolean flag = true;
        while(flag)
        {
            System.out.println("\nSelect from the below options:");
            System.out.println("1 for 'Exact Match'");
            System.out.println("2 for 'Best Match'");
            System.out.println("3 for 'Ordered Best Match with proximity N'");
            System.out.println("4 to quit");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String s = br.readLine();

            System.out.println("---Enter comma-separated query terms");
            String query = br.readLine();
            String[] terms = query.split(",");
            String[] queryTerms = new String[terms.length];
            for(int i = 0 ; i < terms.length; i++)
                queryTerms[i] = terms[i].trim();

            switch(s)
            {
                case "1":
                    ExactMatch em = new ExactMatch();
                    em.getExactMatches(ii, docIdMap, termCount, queryTerms);
                    break;

                case "2":
                    BestMatch bm = new BestMatch();
                    bm.getBestMatch(ii, docIdMap, termCount, queryTerms);
                    break;

                case "3":
                    System.out.println("---Enter proximity parameter N");
                    int n = Integer.parseInt(br.readLine());
                    OrderedBestMatch obm = new OrderedBestMatch();
                    obm.getOrderedBestMatch(ii, docIdMap, termCount, queryTerms, n);
                    break;

                case "4":
                    flag = false;
                    break;

                default:
                    System.out.println("Option not recognized");
                    break;

            }


        }
        System.out.println("Exit");
    }

}
