import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TfIdf {

	private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
	private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
	private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();

	public static void runTfIdf(List<Query> queries, Map<String, List<DTF>> ii2, Map<Integer, String> docIdMap2,
			Map<Integer, Integer> termCount2) {
		ii = ii2;
		docIdMap = docIdMap2;
		termCount = termCount2;
		queries.stream().forEach(query -> {
			List<Ranks> ranks = runTfIdf(query);
			System.out.println(query.getQuery());
			for (Ranks r : ranks) {
				System.out.println("Rank " + r.getRank() + " " + docIdMap.get(r.getDocId()));
			}
			System.out.println("\n");
			query.setOutput(ranks);
		});
	}

	private static List<Ranks> runTfIdf(Query query) {
		List<Ranks> ranks = new ArrayList<Ranks>();
		for (String term : query.getQuery().split(" ")) {
			List<DTF> dtf = ii.get(term);
			if (dtf != null) {
				for (DTF d : dtf) {
					double tf = calculateTf(d.getdId(), d.getTf());
					double idf = calculateIdf(dtf.size());
					double score = tf * idf;
					ranks.add(new Ranks(d.getdId(), score));
				}
			}
		}
		return returnTopRanks(ranks);
	}

	private static double calculateIdf(int size) {
		double totalDocuments = docIdMap.size();
		return 1 + Math.log(totalDocuments / (double) (size + 1));
	}

	private static double calculateTf(int did, int tf) {
		int wordsInDoc = termCount.get(did);
		return (double) tf / wordsInDoc;
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

}
