package project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

public class ExactMatch
{
    private static Map<String, List<DTF>> ii;
    private static Map<Integer, String> docIdMap;
    private static Map<Integer, Integer> termCnt;


    public static List<Integer> getDocsWithAllTerms(Map<String, List<DTF>> queryTermHits)
    {
        Set<Integer> commonDocs = new HashSet<Integer>();
        boolean first = true;

        for(String queryTerm: queryTermHits.keySet())
        {

            System.out.println("For query Term: " + queryTerm);
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

                if(first)
                {
                    commonDocs.addAll(docs);
                    first = false;
                }
                else
                    commonDocs.retainAll(docs); // set intersection
            }
        }

        return new ArrayList<Integer>(commonDocs);
    }

    public static List<Ranks> getTfIdfScores(List<Integer> results, String[] query, Map<String, List<DTF>> queryTermHits)
    {
        // results - docId vs no of queryTerms present in the document
        int corpusSize = docIdMap.size();
        List<Ranks> rankedResults = new ArrayList<Ranks>();

        for(Integer dId: results)
        {
            int docId = dId;

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

            rankedResults.add(new Ranks(docId, tfIdf));
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

    public static void getExactMatches(Map<String, List<DTF>> invIndex, Map<Integer, String> docIdNameMapping, Map<Integer, Integer> termCount, String[] query)
    {
        ii = invIndex;
        docIdMap = docIdNameMapping;
        termCnt = termCount;

        // get docs for each query term from inverted index into a map
        Map<String, List<DTF>> queryTermHits = new LinkedHashMap<String, List<DTF>>();
        for(String queryTerm: query)
            queryTermHits.put(queryTerm, ii.get(queryTerm));

        // get docIds which contains all terms (intersection of sets)
        List<Integer> commonDocs = getDocsWithAllTerms(queryTermHits);

        //test
        StringBuilder sb = new StringBuilder();
        for(Integer p: commonDocs)
        {
            sb.append(p + " - " + docIdMap.get(p));
            sb.append(",");
        }
        System.out.println("Common DocIds: " + sb.toString());
        //

        List<Integer> exactMatchDocs = new ArrayList<Integer>();

        for(Integer docId: commonDocs)
        {
            // for each document, check if query terms are in correct order

            List<QueryPositionHelper> qph = new ArrayList<QueryPositionHelper>();
            int queryTermId = 1; // for ordering

            // loading a list which contains qph objects for all query terms [query Id, position]
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

            //test - print qph
            System.out.println("QPH List");
            for(QueryPositionHelper q: qph)
            {
                System.out.println(q.getQueryId() + "-" + q.getPosition());
            }
            System.out.println();
            //

            int qphCounter = 0;
            while(qphCounter < qph.size())
            {
                // if match for queryTermId 1 - Check for subsequent terms with delta in positions as 1
                if(qph.get(qphCounter).getQueryId() == 1)
                {
                    boolean exactMatch = true;
                    int currentIndex = qphCounter+1; // out of bounds handle
                    int query1Position = qph.get(qphCounter).getPosition() + 1;
                    for(int i=2 ; i < query.length + 1; i++)
                    {
                        QueryPositionHelper temp = null;
                        if(currentIndex < qph.size())
                            temp = qph.get(currentIndex);
                        else
                        {
                            exactMatch = false;
                            break;
                        }

                        if(temp.getQueryId() != i && temp.getPosition() != query1Position)
                        {
                            exactMatch = false;
                            break;
                        }
                        currentIndex++;
                        query1Position++;
                    }

                    if(exactMatch)
                    {
                        exactMatchDocs.add(docId);
                        break;
                    }
                }

                qphCounter++;
            }

        }

        System.out.println("Exact matches:");
        for(Integer i: exactMatchDocs)
        {
            System.out.println(i + " " + docIdMap.get(i));
        }
        System.out.println();

        List<Ranks> rankedResults = getTfIdfScores(exactMatchDocs, query, queryTermHits);

        System.out.println("\nRanked results:");

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
