package project;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StopListRun {

    public static List<Query> generateStopListQueries(List<Query> unstoppedQueries) throws IOException {
        List<String> stopwords=Utilities.getStopWords();
        for (Query q:
             unstoppedQueries) {
            List<String> finalQueryTerms =  new ArrayList<String>();
            Collections.addAll(finalQueryTerms, q.getQuery().toLowerCase().split(" "));
            q.setQuery(finalQueryTerms.stream().filter(x->!stopwords.contains(x)).collect(Collectors.joining(" ")));
        }
        return unstoppedQueries;
    }
}
