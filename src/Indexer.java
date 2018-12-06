import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;

import kotlin.Pair;

public class Indexer {

	// inverted index storage
	private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
	private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
	private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();
	private static Map<String, Integer> docValueMap = new HashMap<String, Integer>();

	public static void main(String[] args) {
		Tokenizer.tokenize("case_folding");
		positionalIndex(1);
		try {
			//runBM25(loadQueries());
			runBM25(StopListRun.generateStopListQueries(loadQueries()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void runBM25(List<Query> queries) {
		queries.stream().forEach(query -> {
			List<Ranks> ranks = evaluateQuery(query);
			System.out.println(query.getQuery());
			for(Ranks r : ranks) {
				System.out.println("Rank "+r.getRank() + " " + docIdMap.get(r.getDocId()));
			}
			System.out.println("\n");
			query.setOutput(ranks);
		});
		//TODO write output to file
	}

	private static List<Ranks> evaluateQuery(Query query) {
		Set<Integer> relevantDocs = query.getRelevantDocs();
		boolean relevanceFlag = relevantDocs != null;
		List<Ranks> ranks = new ArrayList<Ranks>();
		int ri = 0, qfi = 0;
		double avdl = averageLengthofDocs();
		for (String term : query.getQuery().toLowerCase().split(" ")) {
			if (!relevanceFlag)
				ri = 0;
			else {
				if (ii.containsKey(term))
					ri = ri(term, ii.get(term), relevantDocs);
			}
			qfi = qfi(query, term);
			if (ii.containsKey(term)) {
				List<DTF> dtf = ii.get(term);
				for (DTF d : dtf) {
					double currentScore = bm25Score(d.getdId(), d.getTf(), ii.get(term).size(), qfi, ri, relevantDocs,
							avdl);
					updateScore(ranks, d.getdId(), currentScore);
				}
			}

		}

		return returnTopRanks(ranks);
	}

	private static List<Ranks> returnTopRanks(List<Ranks> ranks) {
		AtomicInteger counter = new AtomicInteger(1);
		List<Ranks> resultList = ranks.stream().sorted((a, b) -> Double.compare(b.getScore(), a.getScore())).limit(100)
				.collect(Collectors.toCollection(ArrayList<Ranks>::new));
		resultList.stream().forEach(x -> {
			x.setRank(counter.getAndIncrement());
		});
		return resultList;
	}

	private static void updateScore(List<Ranks> ranks, int dId, double currentScore) {
		double oldScore = 0.00;
		if (ranks.stream().anyMatch(x -> x.getDocId() == dId)) {
			Ranks r = ranks.stream().filter(x -> x.getDocId() == dId).findFirst().get();
			oldScore = r.getScore();
			r.setScore(currentScore + oldScore);
		} else {
			ranks.add(new Ranks(dId, currentScore));
		}
	}

	private static double bm25Score(int did, int tf, int size, int qfi, int ri, Set<Integer> relevantDocs,
			double avdl) {
		double k1 = 1.2;
		double b = 0.75;
		double k2 = 100;
		//TODO try different k2
		double R;
		try {
			R = (double) relevantDocs.size();
		} catch (NullPointerException ne) {
			R = 0;
		}
		double K = calculateK(k1, b, did, avdl);

		double num1 = ((double) ri + 0.5) / (R - (double) ri + 0.5);
		double den1 = ((double) size - (double) ri + 0.5) / (termCount.size() - (double) size - R + (double) ri + 0.5);
		double sec = ((k1 + 1) * (double) tf) / (K + (double) tf);
		double thr = ((k2 + 1) * (double) qfi) / (k2 + (double) qfi);
		return (Math.log((num1 / den1) * sec * thr));
	}

	private static double calculateK(double k1, double b, int did, double avdl) {
		return k1 * ((1 - b) + b * (double) termCount.get(did) / avdl);
	}

	private static double averageLengthofDocs() {
		return termCount.values().stream().mapToDouble(x -> x).average().getAsDouble();
	}

	private static int qfi(Query query, String term) {
		int count = 0;
		for (String word : query.getQuery().toLowerCase().split(" ")) {
			if (word.equals(term))
				count++;
		}
		return count;
	}

	private static int ri(String term, List<DTF> dtf, Set<Integer> relevantDocs) {
		try {
			return (int) dtf.stream().filter(x -> relevantDocs.stream().anyMatch(y -> y == x.getdId())).count();
		} catch (NullPointerException nfe) {
			return 0;
		}
	}

	private static List<Query> loadQueries() throws IOException {
		List<Query> queries = new ArrayList();
		FileHandler f = new FileHandler("docs/queries/cacm.query.txt", false);
		StringBuilder fileContent = new StringBuilder();
		String currentLine = null;
		while ((currentLine = f.readLine()) != null) {
			fileContent.append(currentLine + " ");
		}
		fileContent.substring(0, (fileContent.length() - 2));
		Map<Integer, Set<Integer>> relevantInfo = new HashMap<Integer, Set<Integer>>();
		loadRelevantInfo(relevantInfo);
		Document doc = Jsoup.parse(fileContent.toString());
		Elements e = doc.getElementsByTag("DOC");
		Iterator<Element> itr = e.iterator();
		while (itr.hasNext()) {
			Element query = itr.next();
			Node queryNo = query.childNode(1).childNode(0);
			Node queryText = query.childNode(2);
			Query q = new Query();
			q.setQueryId(Integer.valueOf(queryNo.toString().trim()));
			q.setQuery(queryText.toString().trim());
			q.setRelevantDocs(relevantInfo.get(Integer.valueOf(queryNo.toString().trim())));
			queries.add(q);
		}
		return queries;

	}

	private static void loadRelevantInfo(Map<Integer, Set<Integer>> relevantInfo) throws IOException {
		FileHandler f = new FileHandler("docs/queries/cacm.rel.txt", false);
		String currentLine = null;
		while ((currentLine = f.readLine()) != null) {
			String[] temp = currentLine.split(" ");
			int key = Integer.valueOf(temp[0]);
			if (relevantInfo.containsKey(key)) {
				Set<Integer> docs = relevantInfo.get(key);
				docs.add(docValueMap.get(temp[2]));
			} else {
				Set<Integer> docs = new HashSet<Integer>();
				docs.add(docValueMap.get(temp[2] + ".html"));
				relevantInfo.put(key, docs);
			}
		}
	}

	private static void positionalIndex(int nGram) {

		File[] files = new File("corpus/").listFiles();
		String indexLoc = ".unigram_positional";

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
				positionalInvertedIndex(ii, terms, i, nGram);
			}
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
