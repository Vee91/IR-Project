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

public class OrderedBestMatch
{

    private static Map<String, List<DTF>> ii;
    private static Map<Integer, String> docIdMap;
    private static Map<Integer, Integer> termCnt;

    public static List<Integer> getAllDocIds(Map<String, List<DTF>> queryTermHits)
    {
        Set<Integer> allDocs = new HashSet<Integer>();

        for(String queryTerm: queryTermHits.keySet())
        {
            List<DTF> dtfs = queryTermHits.get(queryTerm);
            if(dtfs != null)
            {
                Set<Integer> docs = new HashSet<Integer>();

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

                allDocs.addAll(docs);
            }

        }

        return new ArrayList<Integer>(allDocs);
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

    public static void getOrderedBestMatch(Map<String, List<DTF>> invIndex, Map<Integer, String> docIdNameMapping, Map<Integer, Integer> termCount, String[] query, int k)
    {
        ii = invIndex;
        docIdMap = docIdNameMapping;
        termCnt = termCount;

        // get docs for each query term into map
        Map<String, List<DTF>> queryTermHits = new LinkedHashMap<String, List<DTF>>(); // linked to keep insertion order
        for(String queryTerm: query)
            queryTermHits.put(queryTerm, ii.get(queryTerm));


        // get all doc Ids (unique)
        List<Integer> allDocs = getAllDocIds(queryTermHits);

        // holds docIds vs longest chain of ordered query terms within proximity N
        // chain of longest ordered query terms to be used for scoring docs
        Map<Integer, Integer> results = new TreeMap<Integer, Integer>();

        for(Integer docId: allDocs)
        {
            List<QueryPositionHelper> qph = new ArrayList<QueryPositionHelper>();
            int queryTermId = 1; // for ordering

            for (Entry<String, List<DTF>> queryTerm : queryTermHits.entrySet())
            {
                System.out.println("queryTerm: " + queryTerm.getKey());
                if(queryTerm.getValue() != null)
                {
                    for(DTF d: queryTerm.getValue())
                    {
                        if(docId == d.getdId())
                        {
                            for(Integer pos: d.getPositions())
                                qph.add(new QueryPositionHelper(queryTermId, pos));

                            break;
                        }
                    }
                }

                queryTermId++;
            }

            // sort qph list on positions
            Collections.sort(qph, new Comparator<QueryPositionHelper>(){
                public int compare(QueryPositionHelper o1, QueryPositionHelper o2){
                    if(o1.getPosition() < o2.getPosition())
                        return -1;
                    else
                        return 1;
                }
            });

            // test - print dph
            System.out.println("DPH List for: " + docIdMap.get(docId));
            for(QueryPositionHelper q: qph)
            {
                System.out.println(q.getQueryId() + "-" + q.getPosition());
            }
            //

            int qphCounter = 0;
            // finding longest query term match for ranking accordingly
            int longestMatch = 1; // since doc is here, there will be atleast 1 query term present

            while(qphCounter < qph.size())
            {
                int matchLength = 1;
                int currentDphIndex = qphCounter+1;

                while(currentDphIndex < qph.size())
                {
                    QueryPositionHelper currentQph = qph.get(currentDphIndex);
                    QueryPositionHelper previousQph = qph.get(currentDphIndex - 1);

                    //if(currentQph.getQueryId() < (previousQph.getQueryId() + 1) ||  // < operator if terms in btw can be omitted
                    if(currentQph.getQueryId() != (previousQph.getQueryId() + 1) || // query order not followed
                            (currentQph.getPosition() - previousQph.getPosition()) > k) // proximity constraint not followed
                    {
                        break;
                    }

                    matchLength++;
                    currentDphIndex++;
                }

                if(matchLength > longestMatch)
                    longestMatch = matchLength;

                qphCounter++;
            }

            // add docId to result
            results.put(docId, longestMatch);

        }

        // print results
        System.out.println("Results: ");
        for (Entry<Integer, Integer> e : results.entrySet())
        {
            System.out.println(e.getKey() + " - " + docIdMap.get(e.getKey()) + " - " + e.getValue());
        }
        System.out.println();

        // ranked results
        List<Ranks> rankedResults = getTfIdfScores(results, query, queryTermHits);

        //test print
        System.out.println("\nRanked results");

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
