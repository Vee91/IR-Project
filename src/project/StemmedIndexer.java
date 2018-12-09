package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;

import kotlin.Pair;

public class StemmedIndexer {

	private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
	private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
	private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();
	private static Map<String, Integer> docValueMap = new HashMap<String, Integer>();

	public static void main(String[] args) {
		try {
			FileHandler f = new FileHandler("docs/queries/cacm_stem.txt", false);
			StringBuilder fileContent = new StringBuilder();
			String currentLine = null;
			while ((currentLine = f.readLine()) != null) {
				fileContent.append(currentLine + " ");
			}
			fileContent.substring(0, (fileContent.length() - 2));
			String[] files = fileContent.toString().split("#");
			for (int i = 1; i < files.length; i++) {
				String[] stemmedFileTerms = files[i].split(" ");
				index(Arrays.asList(stemmedFileTerms).subList(1, stemmedFileTerms.length));
			}
			
			List<Pair<String, List<DTF>>> indexSource = new ArrayList();
			List<Pair<Integer, String>> docMapSource = new ArrayList();
			List<Pair<Integer, Integer>> termCountSource = new ArrayList();
			for (Entry<String, List<DTF>> e : ii.entrySet()) {
				indexSource.add(new Pair(e.getKey(), e.getValue()));
			}
			for (Entry<Integer, String> e : docIdMap.entrySet()) {
				docMapSource.add(new Pair(e.getKey(), e.getValue()));
			}
			for (Entry<Integer, Integer> e : termCount.entrySet()) {
				termCountSource.add(new Pair(e.getKey(), e.getValue()));
			}
			try {
				PrintWriter writer = new PrintWriter("unigram_stemmed.txt", "UTF-8");
				for (Entry<String, List<DTF>> e : ii.entrySet()) {
					writer.print(e.getKey() + " ");
					writer.print(e.getValue().size() + " ");
					writer.println(e.getValue());
				}
				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			String indexLoc = ".unigram_stemmed";
			DB db = DBMaker.fileDB(indexLoc).make();
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void index(List<String> terms) {
		// calculate inverted index
		int docId = Integer.valueOf(terms.get(0));
		docIdMap.put(docId, "CACM-" + terms.get(0) + ".html");
		docValueMap.put("CACM-" + terms.get(0) + ".html", docId);
		termCount.put(docId, terms.size() - 1);
		regularInvertedIndex(terms, docId);
	}

	private static void regularInvertedIndex(List<String> terms, int docId) {

		for (int i = 0; i < terms.size(); i++) {
			String term = "";
			term += terms.get(i);
			if (ii.get(term) == null) {
				DTF dtf = new DTF();
				dtf.setdId(docId);
				dtf.setTf(1);
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
					;
				} else {
					DTF newdtf = new DTF();
					newdtf.setdId(docId);
					newdtf.setTf(1);
					dtf.add(newdtf);
				}
			}
		}
	}

}
