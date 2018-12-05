import java.util.ArrayList;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;

public class Search {

	public static void main(String[] args) {
		search(args[0], args[1], Integer.valueOf(args[2]));
	}

	private static void search(String string1, String string2, int k) {
		DB db = DBMaker.fileDB(".unigram_positional").make();
		BTreeMap<String, List<DTF>> invertedIndex = db.treeMap("invertedIndex").valuesOutsideNodesEnable()
				.keySerializer(new SerializerCompressionWrapper(Serializer.STRING))
				.valueSerializer(new SerializerCompressionWrapper(Serializer.JAVA)).createOrOpen();

		BTreeMap<Integer, String> docMap = db.treeMap("docMap").valuesOutsideNodesEnable()
				.keySerializer(new SerializerCompressionWrapper(Serializer.INTEGER))
				.valueSerializer(new SerializerCompressionWrapper(Serializer.STRING)).createOrOpen();
		List<DTF> result1 = invertedIndex.get(string1);
		List<DTF> result2 = invertedIndex.get(string2);
		List<String> output = new ArrayList<String>();

		if (result1 != null && result2 != null && result1.size() > 0 && result2.size() > 0) {
			int i = 0;
			int j = 0;
			while (i != result1.size() && j != result2.size()) {
				DTF dtf1 = result1.get(i);
				DTF dtf2 = result2.get(j);
				if (dtf1.getdId() > dtf2.getdId()) {
					j++;
				} else if (dtf2.getdId() > dtf1.getdId()) {
					i++;
				} else {
					List<DTF> first = new ArrayList<DTF>();
					List<DTF> second = new ArrayList<DTF>();
					first.add(dtf1);
					second.add(dtf2);
					while (i < result1.size() - 1 && (result1.get(i + 1).getdId() == dtf1.getdId())) {
						first.add(result1.get(i + 1));
						i++;
					}
					while (j < result2.size() - 1 && (result2.get(j + 1).getdId() == dtf2.getdId())) {
						second.add(result2.get(j + 1));
						j++;
					}
					if (i == result1.size() - 1 || j == result2.size() - 1) {
						if (result1.get(i).getdId() == dtf1.getdId()) {
							first.add(result1.get(i));
						}
						if (result2.get(j).getdId() == dtf2.getdId()) {
							second.add(result2.get(j));
						}
					}
					composeOutput(first, second, output, docMap, k);
					i++;
					j++;
				}
			}
		}
		System.out.println(output);
	}

	private static void composeOutput(List<DTF> first, List<DTF> second, List<String> output,
			BTreeMap<Integer, String> docMap, int k) {
		int pos1 = 0;
		for (int i = 0; i < first.size(); i++) {
			DTF d = first.get(i);
			pos1 += d.getPos();
			int pos2 = 0;
			for (int j = 0; j < second.size(); j++) {
				DTF t = second.get(j);
				pos2 += t.getPos();
				if (Math.abs(pos2 - pos1) <= k) {
					output.add(docMap.get(t.getdId()));
					return;
				}
			}
		}
	}

}
