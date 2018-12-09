package project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
		if (args[0].equals("base")) {
			db = DBMaker.fileDB(".unigram_positional").make();
		}
		else if(args[0].equals("stem")) {
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
			List<Query> queries = loadQueries();
			BM25.runBM25(queries, ii, docIdMap, termCount);
			//BM25.runBM25(StopListRun.generateStopListQueries(loadQueries()), ii,
			// docIdMap, termCount);
			//TfIdf.runTfIdf(queries, ii, docIdMap, termCount);
			//QLM.runJMQLM(queries, ii, docIdMap, termCount, null);
			//Lucene.runLucene(queries);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<Query> loadQueries() throws IOException {
		List<Query> queries = new ArrayList();
		String pattern = "([a-zA-Z]+|\\s+)([\\p{Punct}]+)([a-zA-Z]*)";
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
			while (!queryText.equals(queryText.replaceAll(pattern, "$1$3"))) {
				queryText = queryText.replaceAll(pattern, "$1$3");
			}

			Query q = new Query();
			q.setQueryId(Integer.valueOf(queryNo.toString().trim()));
			q.setQuery(queryTextNode.toString().toLowerCase().trim());
			q.setRelevantDocs(null);
			queries.add(q);
		}
		return queries;
	}

}
