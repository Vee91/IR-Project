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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class QLM {

	private static Map<String, List<DTF>> ii = new TreeMap<String, List<DTF>>();
	private static Map<Integer, String> docIdMap = new HashMap<Integer, String>();
	private static Map<Integer, Integer> termCount = new HashMap<Integer, Integer>();

	public static void runJMQLM(List<Query> queries, Map<String, List<DTF>> ii1, Map<Integer, String> docIdMap1,
			Map<Integer, Integer> termCount1, Map<Integer, Set<Integer>> relevantinfo) {
		ii = ii1;
		docIdMap = docIdMap1;
		termCount = termCount1;
		try {
			PrintWriter writer = new PrintWriter("QLM.txt", "UTF-8");
			queries.stream().forEach(query -> {
				List<Ranks> ranks = runJMQLM(query, relevantinfo);
				writer.println(query.getQuery());
				for (Ranks r : ranks) {
					writer.println("Rank " + r.getRank() + " " + docIdMap.get(r.getDocId()) + " " + r.getScore() + " " + r.getDocId());
				}
				writer.println("\n");
				query.setOutput(ranks);
			});
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<Ranks> runJMQLM(Query query, Map<Integer, Set<Integer>> relevantinfo) {
		double LAMBDA = 0.35;
		String queryTerms[] = formatQuery(query.getQuery());
		double score = 0;
		List<Ranks> ranks = new ArrayList<>();
		for (String qt : queryTerms) {
			try {
				List<DTF> dtf = ii.get(qt);
				int cqi = dtf.size(); // change
				int cLength = getCollectionLength(query, dtf);
				Iterator<DTF> dtfitr = dtf.iterator();
				while (dtfitr.hasNext()) {
					DTF d = dtfitr.next();
					if (query.getRelevantDocs().contains(d.getdId())) {
						score = (1 - LAMBDA) * (d.getTf()) / termCount.get(d.getdId());
						Iterator<Ranks> rankItr = ranks.iterator();
						boolean flag = false;
						while (rankItr.hasNext()) {
							Ranks r = rankItr.next();
							if (r.getDocId() == (d.getdId())) {
								r.setScore(r.getScore() + score);
								flag = true;
							}
						}
						if (!flag) {
							ranks.add(new Ranks(d.getdId(), score));
						}
					} else {
						score = LAMBDA * cqi / cLength;
						Iterator<Ranks> rankItr = ranks.iterator();
						boolean flag = false;
						while (rankItr.hasNext()) {
							Ranks r = rankItr.next();
							if (r.getDocId() == (d.getdId())) {
								r.setScore(r.getScore() + score);
								flag = true;
							}
						}
						if (!flag) {
							ranks.add(new Ranks(d.getdId(), score));
						}
					}
				}
			} catch (NullPointerException ne) {
			}
		}
		return returnTopRanks(ranks);
	}

	private static int getCollectionLength(Query query, List<DTF> dtf) {
		Set<Integer> relDocs = query.getRelevantDocs();
		Iterator<DTF> dtfItr = dtf.iterator();
		int cLength = 0;
		while (dtfItr.hasNext()) {
			DTF d = dtfItr.next();
			if (!relDocs.contains(d.getdId()))
				cLength += termCount.get(d.getdId());
		}
		return cLength;
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
		List<Ranks> resultList = ranks.stream().sorted((a, b) -> Double.compare(b.getScore(), a.getScore())).limit(100)
				.collect(Collectors.toCollection(ArrayList<Ranks>::new));
		resultList.stream().forEach(x -> {
			x.setRank(counter.getAndIncrement());
		});
		return resultList;
	}
}
