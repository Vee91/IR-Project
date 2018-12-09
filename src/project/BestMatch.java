package project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class BestMatch
{
    private static Map<String, List<DTF>> ii;
    private static Map<Integer, String> docIdMap;
    private static Map<Integer, Integer> termCnt;

    public static List<Integer> getAllDocIds(Map<String, List<DTF>> queryTermHits)
    {
        List<Integer> allDocs = new ArrayList<Integer>();

        for(String queryTerm: queryTermHits.keySet())
        {
            List<DTF> dtfs = queryTermHits.get(queryTerm);
            if(dtfs != null)
            {
                List<Integer> docs = new ArrayList<Integer>();

                for(DTF d: dtfs)
                    docs.add(d.getdId());

                //test
                StringBuilder sb = new StringBuilder();
                for(Integer p: docs)
                {
                    sb.append(p);
                    sb.append(",");
                }
                System.out.println("DocIds: " + sb.toString());
                //

                allDocs.addAll(docs);
            }

        }

        Collections.sort(allDocs);

        return allDocs;
    }

    public static List<Ranks> getTfIdfScores(Map<Integer, Integer> results, String[] query, Map<String, List<DTF>> queryTermHits)
    {
        // results - docId vs no of queryTerms present in the document
        int corpusSize = docIdMap.size();
        List<Ranks> rankedResults = new ArrayList<Ranks>();

        for(Entry<Integer, Integer> r : results.entrySet())
        {
            int docId = r.getKey();

            double tfIdf = 0.0;

            for(String q: query)
            {
                double tf = 0.0;
                double idf = 0.0;

                List<DTF> dtfs = queryTermHits.get(q);

                if(dtfs != null)
                {
                    for(DTF d: dtfs)
                    {
                        if(docId == d.getdId())
                        {
                            tf = (double) d.getTf() / termCnt.get(docId);
                            idf = 1 + Math.log(corpusSize / (double) (dtfs.size() + 1));
                        }
                    }
                }

                tfIdf += (tf*idf);
            }
            double score = tfIdf * r.getValue();

            rankedResults.add(new Ranks(docId, score));
        }

        // sort and rank docs
        Collections.sort(rankedResults, new Comparator<Ranks>(){
            public int compare(Ranks o1, Ranks o2){
                if(o1.getScore() > o2.getScore())
                    return -1;
                else
                    return 1;
            }
        });

        for(int i = 0; i < rankedResults.size(); i++)
        {
            Ranks temp = rankedResults.get(i);
            temp.setRank(i+1);
            rankedResults.set(i, temp);
        }

        return rankedResults;
    }

    public static void getBestMatch(Map<String, List<DTF>> invIndex, Map<Integer, String> docIdNameMapping, Map<Integer, Integer> termCount, String[] query)
    {
        // TODO Auto-generated method stub
        ii = invIndex;
        docIdMap = docIdNameMapping;
        termCnt = termCount;

        // get docs for each query term into map
        Map<String, List<DTF>> queryTermHits = new LinkedHashMap<String, List<DTF>>(); // linked to keep insertion order
        for(String queryTerm: query)
            queryTermHits.put(queryTerm, ii.get(queryTerm));

        List<Integer> allDocs = getAllDocIds(queryTermHits);

        //test
        System.out.println("Printing allDocs:");
        StringBuilder sb = new StringBuilder();
        for(Integer i : allDocs)
        {
            sb.append(i + ", ");
        }
        System.out.println(sb.toString());
        //

        Map<Integer, Integer> results = new TreeMap<Integer, Integer>();

        Set<Integer> uniqueDocIds = new HashSet<Integer>(allDocs);
        for(Integer docId: uniqueDocIds)
        {
            results.put(docId, Collections.frequency(allDocs, docId));
        }

        //test
        for(Entry<Integer, Integer> r : results.entrySet())
        {
            System.out.println("Key: " + r.getKey() + "-" + docIdMap.get(r.getKey()) + " Value: " + r.getValue());
        }
        //

        // get into list of ranks object
        List<Ranks> rankedResults = getTfIdfScores(results, query, queryTermHits);

        //test print
        System.out.println("Ranked results");

        List<Ranks> topRankedResults = new ArrayList<Ranks>();
        if(rankedResults.size() < 100)
            topRankedResults = rankedResults;
        else
            topRankedResults = rankedResults.subList(0, 100);

        for(Ranks r: topRankedResults)
        {
            System.out.println("Rank: " + r.getRank() + " - Document ID: " + docIdMap.get(r.getDocId()) + " - Score: " + r.getScore());
        }

        return;
    }

}
