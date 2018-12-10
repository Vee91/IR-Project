package project;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PseudoRelevanceFeedback {

    public PseudoRelevanceFeedback()
    {

    }

    public void runPsuedoRelevance(List<Query>evaluatedQueries,Map<Integer,String> docidMap) throws IOException {
        expanding(evaluatedQueries,docidMap);
    }

    public void expanding(List<Query>evaluatedQueries,Map<Integer,String> docidMap) throws IOException {
        Map<String,String> content=tokenize("both");
        for (Query q:evaluatedQueries)
        {
            List<Ranks> rk=q.getOutput();
            Comparator<Ranks> comparator = Comparator.comparing(e -> e.getScore());
            rk.sort(comparator.reversed());
            int k=5;
            List<Ranks> top5RankedDocs=rk.subList(0,3);
            Map<String,Integer> wordCount=new HashMap<String,Integer>();
            for (Ranks docRnk:
                    top5RankedDocs) {
                String docContent=content.get(docidMap.get(docRnk.getDocId()));
                String[] splitContent=docContent.split(" ");
                for (String word:
                     splitContent) {
                    if (wordCount.containsKey(word))
                        wordCount.put(word, wordCount.get(word) + 1);
                    else
                        wordCount.put(word,1);
                }
            }
            List<String> stopWords=Utilities.getStopWords();
            Iterator<Map.Entry<String,Integer>> it=wordCount.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String,Integer> item=it.next();
                if(stopWords.contains(item.getKey()))
                    it.remove();
            }
            wordCount = sortByValues(wordCount);
            wordCount.entrySet().stream().limit(5).forEach(x->q.setQuery(q.getQuery()+" "+x.getKey()));
        }
    }
    private static HashMap sortByValues(Map map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        }.reversed());

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }


    public static Map<String,String> tokenize(String i) {
        File[] files = new File("docs/raw/").listFiles();
        String pattern = "([a-zA-Z]+|\\s+)([\\p{Punct}]+)([a-zA-Z]*)";
        String bib = "([0-9]+\\s+)([0-9]+\\s+)([0-9]+)"; // remove numbers at the bottom
        Map<String,String> docIdAndContent=new HashMap<String,String>();
        int count = 0;
        for (File f : files) {
            if (f.isFile()) {
                try {
                    String str = "";
                    Document doc = Jsoup.parse(f, "UTF-8");
                    formatDoc(doc);
                    // default. Do both casefolding and punctuation handling
                    if (i.equals("both")) {
                        str = doc.text().toLowerCase();
                        while (!str.equals(str.replaceAll(pattern, "$1$3"))) {
                            str = str.replaceAll(pattern, "$1$3");
                        }
                        str = str.replaceAll(bib, "");
                    }
                    // no case folding
                    else if (i.equals("case_folding")) {
                        str = doc.text().toLowerCase();
                        str = str.replaceAll(bib, "");
                    } else if (i.equals("punct")) {
                        while (!str.equals(str.replaceAll(pattern, "$1$3"))) {
                            str = str.replaceAll(pattern, "$1$3");
                        }
                        str = str.replaceAll(bib, "");
                    }

                    // no case folding or punctuation handling
                    else if (i.equals("false")) {
                        str = doc.text();
                    }

                    docIdAndContent.put(f.getName(), str);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Parsed " + count + " files");
        return docIdAndContent;
    }

    private static void formatDoc(Document document) {
        document.getElementsByAttributeValue("role", "navigation").remove(); // for external links
        document.getElementsByAttributeValue("class", "mwe-math-element").remove(); // for formulae
        document.getElementsByAttributeValueContaining("class", "reflist").remove(); // for references
        document.getElementsByAttributeValueContaining("class", "mw-redirect").remove(); // for redirect
        document.getElementsByAttributeValue("class", "printfooter").remove();
        document.getElementsByAttributeValue("class", "catlinks").remove();
        document.getElementsByAttributeValue("style", "display:none").remove();
        document.getElementsByAttributeValue("id", "mw-navigation").remove();
        document.getElementsByAttributeValue("role", "contentinfo").remove();
        document.select("table").remove(); // remove tables
        document.select("img").remove(); // remove images
        document.select("form").remove(); // remove forms
        document.select("input").remove(); // remove input
        document.select("sup").remove(); // remove citations
    }
}
