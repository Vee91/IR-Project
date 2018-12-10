package project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;

public class BaselineRuns {

	public static void main(String[] args) {
		DB db = null;
		if (args[0].equals("base") || args[0].equals("stop") || args[0].equals("prf") ) {
			db = DBMaker.fileDB(".unigram_positional").make();
		} else if (args[0].equals("stem")) {
			db = DBMaker.fileDB(".unigram_stemmed").make();
		}
		BTreeMap<String, List<DTF>> ii = db.treeMap("invertedIndex").valuesOutsideNodesEnable()
				.keySerializer(new SerializerCompressionWrapper(Serializer.STRING))
				.valueSerializer(new SerializerCompressionWrapper(Serializer.JAVA)).createOrOpen();

		BTreeMap<Integer, String> docIdMap = db.treeMap("docMap").valuesOutsideNodesEnable()
				.keySerializer(new SerializerCompressionWrapper(Serializer.INTEGER))
				.valueSerializer(new SerializerCompressionWrapper(Serializer.STRING)).createOrOpen();

		BTreeMap<Integer, Integer> termCount = db.treeMap("termCountMap").valuesOutsideNodesEnable()
				.keySerializer(new SerializerCompressionWrapper(Serializer.INTEGER))
				.valueSerializer(new SerializerCompressionWrapper(Serializer.INTEGER)).createOrOpen();
		try {
			if (args[0].equals("stop")) {
				List<Query> stopQueries = StopListRun.generateStopListQueries(loadQueries());
				BM25.runBM25(stopQueries, ii, docIdMap, termCount, false);
				PrintWriter writer = new PrintWriter("BM25_stop.txt", "UTF-8");
				for (Query query : stopQueries) {
					List<Ranks> ranks = query.getOutput();
					for (Ranks r : ranks) {
						writer.println(query.getQueryId() + " Q0 "
								+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5) + " "
								+ r.getRank() + " " + r.getScore() + " BM25_Model");
					}
					writer.println("\n");
				}
				writer.close();
				TfIdf.runTfIdf(stopQueries, ii, docIdMap, termCount);
				writer = new PrintWriter("tfidf_stop.txt", "UTF-8");
				for (Query query : stopQueries) {
					List<Ranks> ranks = query.getOutput();
					for (Ranks r : ranks) {
						writer.println(query.getQueryId() + " Q0 "
								+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5) + " "
								+ r.getRank() + " " + r.getScore() + " BM25_Model");
					}
					writer.println("\n");
				}
				writer.close();
				QLM.runJMQLM(stopQueries, ii, docIdMap, termCount, null);
				writer = new PrintWriter("qlm_stop.txt", "UTF-8");
				for (Query query : stopQueries) {
					List<Ranks> ranks = query.getOutput();
					for (Ranks r : ranks) {
						writer.println(query.getQueryId() + " Q0 "
								+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5) + " "
								+ r.getRank() + " " + r.getScore() + " BM25_Model");
					}
					writer.println("\n");
				}
				writer.close();
			} else if (args[0].equals("base") || args[0].equals("stem")) {
				List<Query> queries = loadQueries();
				PrintWriter writer = null;
				BM25.runBM25(queries, ii, docIdMap, termCount, false);
				if (args[0].equals("base")) {
					writer = new PrintWriter("BM25.txt", "UTF-8");
					for (Query query : queries) {
						List<Ranks> ranks = query.getOutput();
						for (Ranks r : ranks) {
							writer.println(query.getQueryId() + " Q0 "
									+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5)
									+ " " + r.getRank() + " " + r.getScore() + " BM25_Model");
						}
						writer.println("\n");
					}
					writer.close();
				} else {
					writer = new PrintWriter("BM25_stem.txt", "UTF-8");
					for (Query query : queries) {
						List<Ranks> ranks = query.getOutput();
						for (Ranks r : ranks) {
							writer.println(query.getQueryId() + " Q0 "
									+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5)
									+ " " + r.getRank() + " " + r.getScore() + " BM25_Model");
						}
						writer.println("\n");
					}
					writer.close();
				}

				TfIdf.runTfIdf(queries, ii, docIdMap, termCount);
				if (args[0].equals("base")) {
					writer = new PrintWriter("tfidf.txt", "UTF-8");
					for (Query query : queries) {
						List<Ranks> ranks = query.getOutput();
						for (Ranks r : ranks) {
							writer.println(query.getQueryId() + " Q0 "
									+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5)
									+ " " + r.getRank() + " " + r.getScore() + " BM25_Model");
						}
						writer.println("\n");
					}
					writer.close();
				} else {
					writer = new PrintWriter("tfidf_stem.txt", "UTF-8");
					for (Query query : queries) {
						List<Ranks> ranks = query.getOutput();
						for (Ranks r : ranks) {
							writer.println(query.getQueryId() + " Q0 "
									+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5)
									+ " " + r.getRank() + " " + r.getScore() + " BM25_Model");
						}
						writer.println("\n");
					}
					writer.close();
				}

				QLM.runJMQLM(queries, ii, docIdMap, termCount, null);
				if (args[0].equals("base")) {
					writer = new PrintWriter("qlm.txt", "UTF-8");
					for (Query query : queries) {
						List<Ranks> ranks = query.getOutput();
						for (Ranks r : ranks) {
							writer.println(query.getQueryId() + " Q0 "
									+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5)
									+ " " + r.getRank() + " " + r.getScore() + " BM25_Model");
						}
						writer.println("\n");
					}
					writer.close();
				} else {
					writer = new PrintWriter("qlm_stem.txt", "UTF-8");
					for (Query query : queries) {
						List<Ranks> ranks = query.getOutput();
						for (Ranks r : ranks) {
							writer.println(query.getQueryId() + " Q0 "
									+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5)
									+ " " + r.getRank() + " " + r.getScore() + " BM25_Model");
						}
						writer.println("\n");
					}
					writer.close();
				}

				if (args[0].equals("base")) {
					Lucene.runLucene(queries);
				}
			} else if (args[0].equals("prf")) {
				List<Query> queries = loadQueries();
				BM25.runBM25(queries, ii, docIdMap, termCount, true);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Query> loadQueries() throws IOException {
		List<Query> queries = new ArrayList();
		String endingPattern = "([a-zA-Z]+|\\s+)([\\p{Punct}&&[^-]]+)([a-zA-Z]*)";
		String startingPattern = "([a-zA-Z]*|\\s*)([\\p{Punct}&&[^-]]+)([a-zA-Z]+)";
		FileHandler f = new FileHandler("docs/queries/cacm.query.txt", false);
		StringBuilder fileContent = new StringBuilder();
		String currentLine = null;
		while ((currentLine = f.readLine()) != null) {
			fileContent.append(currentLine + " ");
		}
		fileContent.substring(0, (fileContent.length() - 2));
		Document doc = Jsoup.parse(fileContent.toString());
		Elements e = doc.getElementsByTag("DOC");
		Iterator<Element> itr = e.iterator();
		while (itr.hasNext()) {
			Element query = itr.next();
			Node queryNo = query.childNode(1).childNode(0);
			Node queryTextNode = query.childNode(2);

			// punctuation removal
			String queryText = queryTextNode.toString().toLowerCase().trim();
			while (!queryText.equals(queryText.replaceAll(endingPattern, "$1$3"))) {
				queryText = queryText.replaceAll(endingPattern, "$1$3");
			}
			while (!queryText.equals(queryText.replaceAll(startingPattern, "$1$3"))) {
				queryText = queryText.replaceAll(startingPattern, "$1$3");
			}

			Query q = new Query();
			q.setQueryId(Integer.valueOf(queryNo.toString().trim()));
			q.setQuery(queryText);
			q.setRelevantDocs(null);
			queries.add(q);
		}
		return queries;
	}

}
