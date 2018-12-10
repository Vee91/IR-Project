package project;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import kotlin.Pair;

public class QLM {

	private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
	private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
	private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();

	public static void runJMQLM(List<Query> queries, Map<String, List<DTF>> ii1, Map<Integer, String> docIdMap1,
			Map<Integer, Integer> termCount1, Map<Integer, Set<Integer>> relevantinfo) {
		System.out.println("Running QLM");
		for (Entry<String, List<DTF>> e : ii1.entrySet()) {
			ii.put(e.getKey(), e.getValue());
		}
		for (Entry<Integer, String> e : docIdMap1.entrySet()) {
			docIdMap.put(e.getKey(), e.getValue());
		}
		for (Entry<Integer, Integer> e : termCount1.entrySet()) {
			termCount.put(e.getKey(), e.getValue());
		}
		queries.stream().forEach(query -> {
			List<Ranks> ranks = runJMQLM(query, relevantinfo);
			query.setOutput(ranks);
		});
	}

	private static List<Ranks> runJMQLM(Query query, Map<Integer, Set<Integer>> relevantinfo) {
		double LAMBDA = 0.35;
		String queryTerms[] = formatQuery(query.getQuery());
		Map<Integer, Double> accumulator = new HashMap<Integer, Double>();
		int cLength = getCollectionLength();
		List<Ranks> ranks = new ArrayList<>();
		for (String qt : queryTerms) {
			try {
				List<DTF> dtf = ii.get(qt);
				int cqi = calculateCqi(dtf);
				Iterator<DTF> dtfitr = dtf.iterator();
				double collectionScore = LAMBDA * cqi / cLength;
				while (dtfitr.hasNext()) {
					DTF d = dtfitr.next();
					int fqi = d.getTf();
					double docScore = (1 - LAMBDA) * fqi / termCount.get(d.getdId());
					double score = Math.log(collectionScore + docScore);
					if (accumulator.containsKey(d.getdId())) {
						double oldScore = accumulator.get(d.getdId());
						accumulator.put(d.getdId(), oldScore + score);
					} else {
						accumulator.put(d.getdId(), score);
					}
				}
			} catch (NullPointerException ne) {
			}
		}
		for (Entry<Integer, Double> e : accumulator.entrySet()) {
			Ranks r = new Ranks(e.getKey(), e.getValue());
			ranks.add(r);
		}
		return returnTopRanks(ranks);
	}

	private static int calculateCqi(List<DTF> dtf) {
		int count = 0;
		Iterator<DTF> dtfItr = dtf.iterator();
		while (dtfItr.hasNext()) {
			DTF d = dtfItr.next();
			count += d.getTf();
		}
		return count;
	}

	private static int getCollectionLength() {
		int count = 0;
		for (Entry<Integer, Integer> e : termCount.entrySet()) {
			count += e.getValue();
		}
		return count;
	}

	private static String[] formatQuery(String s) {
		s = s.toString().replaceAll("\\s{2,}", " ").replaceAll("[^\\p{ASCII}]", "")
				.replaceAll("(?<![0-9a-zA-Z])[\\p{Punct}]", "").replaceAll("[\\p{Punct}](?![0-9a-zA-Z])", "")
				.replaceAll("http.*?\\s", "");
		s = s.toLowerCase();
		return s.split(" ");
	}

	private static List<Ranks> returnTopRanks(List<Ranks> ranks) {
		AtomicInteger counter = new AtomicInteger(1);
		List<Ranks> resultList = ranks.stream().sorted((a, b) -> Double.compare(a.getScore(), b.getScore())).limit(100)
				.collect(Collectors.toCollection(ArrayList<Ranks>::new));
		resultList.stream().forEach(x -> {
			x.setRank(counter.getAndIncrement());
		});
		return resultList;
	}
}
