import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StopListRun {

    public static List<Query> generateStopListQueries(List<Query> unstoppedQueries) throws IOException {
        File file = new File("StopList\\common_words");

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        List<String> stopwwords=new ArrayList<String>();
        while ((st = br.readLine()) != null)
            stopwwords.add(st.toLowerCase());
        for (Query q:
             unstoppedQueries) {
            List<String> finalQueryTerms =  new ArrayList<String>();
            Collections.addAll(finalQueryTerms, q.getQuery().toLowerCase().split(" "));
            q.setQuery(finalQueryTerms.stream().filter(x->!stopwwords.contains(x)).collect(Collectors.joining(" ")));
        }
        return unstoppedQueries;
    }
}
