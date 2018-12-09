package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;

import kotlin.Pair;

public class AdvancedSearchIndexer {
    private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
    private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
    private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();
    private static Map<String, Integer> docValueMap = new HashMap<String, Integer>();

    public static Map<String, List<DTF>> getIi() {
        return ii;
    }

    public static void setIi(Map<String, List<DTF>> ii) {
        AdvancedSearchIndexer.ii = ii;
    }

    public static Map<Integer, String> getDocIdMap() {
        return docIdMap;
    }

    public static void setDocIdMap(Map<Integer, String> docIdMap) {
        AdvancedSearchIndexer.docIdMap = docIdMap;
    }

    public static Map<Integer, Integer> getTermCount() {
        return termCount;
    }

    public static void setTermCount(Map<Integer, Integer> termCount) {
        AdvancedSearchIndexer.termCount = termCount;
    }

    public static void generateAdvanceSearchIndexer() {
        Tokenizer.tokenize("both");
        positionalIndex(1);
    }

    private static void positionalIndex(int nGram) {

        File[] files = new File("corpus/").listFiles();
        String indexLoc = ".unigram_positional_adv_search";

        DB db = DBMaker.fileDB(indexLoc).make();

        // calculate inverted index
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isFile()) {
                docIdMap.put(i, f.getName());
                docValueMap.put(f.getName(), i);
                String text = getText(f);
                StringTokenizer tokens = new StringTokenizer(text);
                List<String> terms = new ArrayList<String>();
                while (tokens.hasMoreTokens()) {
                    String s = tokens.nextToken();
                    terms.add(s);
                }
                termCount.put(i, terms.size());
                advSearchInvertedIndex(ii, terms, i, nGram);
            }
        }

        List<Pair<String, List<DTF>>> indexSource = new ArrayList();
        List<Pair<Integer, String>> docMapSource = new ArrayList();
        List<Pair<Integer, Integer>> termCountSource = new ArrayList();
        List<Pair<String, Integer>> docValueMapSource = new ArrayList();
        for (Entry<String, List<DTF>> e : ii.entrySet()) {
            indexSource.add(new Pair(e.getKey(), e.getValue()));
        }
        for (Entry<Integer, String> e : docIdMap.entrySet()) {
            docMapSource.add(new Pair(e.getKey(), e.getValue()));
        }
        for (Entry<Integer, Integer> e : termCount.entrySet()) {
            termCountSource.add(new Pair(e.getKey(), e.getValue()));
        }
        for (Entry<String, Integer> e : docValueMap.entrySet()) {
            docValueMapSource.add(new Pair(e.getKey(), e.getValue()));
        }

        // writing to encoded and compressed index files
        db.treeMap("invertedIndex").keySerializer(new SerializerCompressionWrapper(Serializer.STRING))
                .valueSerializer(new SerializerCompressionWrapper(Serializer.JAVA)).createFrom(indexSource.iterator());
        db.treeMap("docMap").keySerializer(new SerializerCompressionWrapper(Serializer.INTEGER))
                .valueSerializer(new SerializerCompressionWrapper(Serializer.STRING))
                .createFrom(docMapSource.iterator());
        db.treeMap("termCountMap").keySerializer(new SerializerCompressionWrapper(Serializer.INTEGER))
                .valueSerializer(new SerializerCompressionWrapper(Serializer.INTEGER))
                .createFrom(termCountSource.iterator());
        db.commit();
        db.close();
        System.out.println("Indexing done");
    }

    private static void advSearchInvertedIndex(Map<String, List<DTF>> ii, List<String> terms, int docId, int nGram) {

        for (int i = 0; i < terms.size() - nGram + 1; i++) {
            String term = "";
            if (nGram == 1) {
                term += terms.get(i + 0);
            }
            if (ii.get(term) == null) {
                DTF dtf = new DTF();
                dtf.setdId(docId);
                dtf.setTf(1);

                // change
                List<Integer> posList = new ArrayList<Integer>();
                posList.add(i);
                dtf.setPositions(posList);
                //

                List<DTF> l = new ArrayList<DTF>();
                l.add(dtf);
                ii.put(term, l);
            } else {
                boolean flag = false;
                DTF temp = null;
                List<DTF> dtf = ii.get(term);
                Iterator<DTF> itr = dtf.iterator();
                while (itr.hasNext()) {
                    DTF d = itr.next();
                    temp = d;
                    if (d.getdId() == docId) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    int tf = temp.getTf();
                    temp.setTf(tf + 1);

                    //change
                    List<Integer> posList = temp.getPositions();
                    if(posList != null && !posList.contains(i))
                    {
                        posList.add(i);
                        temp.setPositions(posList);
                    }
                    else
                    {
                        List<Integer> newPosList = new ArrayList<Integer>();
                        newPosList.add(i);
                        temp.setPositions(newPosList);
                    }

                    //
                } else {
                    DTF newdtf = new DTF();
                    newdtf.setdId(docId);
                    newdtf.setTf(1);

                    //change
                    List<Integer> posLst = new ArrayList<Integer>();
                    posLst.add(i);
                    newdtf.setPositions(posLst);
                    //

                    dtf.add(newdtf);
                }
            }
        }
    }

    private static String getText(File f) {
        String data = "";
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            while ((line = br.readLine()) != null) {
                data += line + " ";
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
