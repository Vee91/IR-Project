import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;

import kotlin.Pair;

public class Indexer {

	public static void main(String[] args) {
		positionalIndex(1);
	}

	private static void positionalIndex(int nGram) {

		Tokenizer.tokenize("case_folding");
		File[] files = new File("corpus/").listFiles();
		String indexLoc = ".unigram_positional";

		DB db = DBMaker.fileDB(indexLoc).make();

		// inverted index storage
		Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
		Map<Integer, String> docMap = new HashMap<Integer, String>();
		Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();

		// calculate inverted index
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isFile()) {
				docMap.put(i, f.getName());
				String text = getText(f);
				StringTokenizer tokens = new StringTokenizer(text);
				List<String> terms = new ArrayList<String>();
				while (tokens.hasMoreTokens()) {
					String s = tokens.nextToken();
					terms.add(s);
				}
				termCount.put(i, terms.size());
				positionalInvertedIndex(ii, terms, i, nGram);
			}
		}

		List<Pair<String, List<DTF>>> indexSource = new ArrayList();
		List<Pair<Integer, String>> docMapSource = new ArrayList();
		List<Pair<Integer, Integer>> termCountSource = new ArrayList();
		for (Entry<String, List<DTF>> e : ii.entrySet()) {
			indexSource.add(new Pair(e.getKey(), e.getValue()));
		}
		for (Entry<Integer, String> e : docMap.entrySet()) {
			docMapSource.add(new Pair(e.getKey(), e.getValue()));
		}
		for (Entry<Integer, Integer> e : termCount.entrySet()) {
			termCountSource.add(new Pair(e.getKey(), e.getValue()));
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

	private static void positionalInvertedIndex(Map<String, List<DTF>> ii, List<String> terms, int docId, int nGram) {

		for (int i = 0; i < terms.size() - nGram + 1; i++) {
			String term = "";
			if (nGram == 1) {
				term += terms.get(i + 0);
			}
			if (ii.get(term) == null) {
				List<DTF> dtf = new ArrayList();
				DTF temp = new DTF(docId, 1, i);
				dtf.add(temp);
				ii.put(term, dtf);
			} else {
				List<DTF> dtf = ii.get(term);
				int pos = i;
				if (nGram == 1) {
					DTF temp = dtf.get(dtf.size() - 1);
					if (temp.getdId() == docId) {
						pos -= dtf.get(dtf.size() - 1).getPos();
						for (int j = dtf.size() - 1; j >= 0; j--) {
							DTF d = dtf.get(j);
							if (d.getdId() == docId) {
								d.setTf(d.getTf() + 1);
							} else {
								break;
							}
						}
						DTF n = new DTF(docId, temp.getTf(), pos);
						dtf.add(n);
					} else {
						DTF d = new DTF(docId, 1, pos);
						dtf.add(d);
					}
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
				data += line;
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
