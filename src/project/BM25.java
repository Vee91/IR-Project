package project;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BM25 {

	private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
	private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
	private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();

	public static void runBM25(List<Query> queries, Map<String, List<DTF>> ii1, Map<Integer, String> docIdMap1,
			Map<Integer, Integer> termCount1, boolean runPseudo) throws IOException {
		for (Entry<String, List<DTF>> e : ii1.entrySet()) {
			ii.put(e.getKey(), e.getValue());
		}
		for (Entry<Integer, String> e : docIdMap1.entrySet()) {
			docIdMap.put(e.getKey(), e.getValue());
		}
		for (Entry<Integer, Integer> e : termCount1.entrySet()) {
			termCount.put(e.getKey(), e.getValue());
		}
		System.out.println("Running BM25");
		queries.stream().forEach(query -> {
			List<Ranks> ranks = evaluateQuery(query);
			query.setOutput(ranks);
		});
		if (runPseudo) {
			PseudoRelevanceFeedback prf = new PseudoRelevanceFeedback();
			prf.runPsuedoRelevance(queries, docIdMap);
			PrintWriter writer2 = new PrintWriter("BM25PRF.txt", "UTF-8");
			queries.stream().forEach(query -> {
				List<Ranks> ranks = evaluateQuery(query);
				for (Ranks r : ranks) {
					writer2.println(query.getQueryId() + " Q0 "
							+ docIdMap.get(r.getDocId()).substring(0, docIdMap.get(r.getDocId()).length() - 5) + " "
							+ r.getRank() + " " + r.getScore() + " BM25_ModelPRF");
				}
				writer2.println("\n");
				query.setOutput(ranks);
			});
		}
	}

	private static List<Ranks> evaluateQuery(Query query) {
		Set<Integer> relevantDocs = query.getRelevantDocs();
		boolean relevanceFlag = relevantDocs != null;
		List<Ranks> ranks = new ArrayList<Ranks>();
		int ri = 0, qfi = 0;
		double avdl = averageLengthofDocs();
		for (String term : query.getQuery().toLowerCase().split(" ")) {

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
		// TODO try different k2
		double R = 0;
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

	// not used
	private static int ri(String term, List<DTF> dtf, Set<Integer> relevantDocs) {
		try {
			return (int) dtf.stream().filter(x -> relevantDocs.stream().anyMatch(y -> y == x.getdId())).count();
		} catch (NullPointerException nfe) {
			return 0;
		}
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
