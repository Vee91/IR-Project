package project;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static List<String> getStopWords() throws IOException {
        File file = new File("StopList\\common_words");

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        List<String> stopwwords=new ArrayList<String>();
        while ((st = br.readLine()) != null)
            stopwwords.add(st.toLowerCase());

        return stopwwords;
    }

    public static String processedWord(String word) {
        // remove all punctuations
        return word.replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("(?<![0-9])[^\\P{P}-](?![0-9])", "") // retain hyphens in text
                .replaceAll("[\\p{Punct}](?![0-9a-zA-Z])", "")
                .replace("(", "")
                .replace(")", "")
                .replaceAll("/"," ")
                .replace("'s", "s");
    }

    public static List<String> getQueryTerms(String query) {
        List<String> queryTerms = new ArrayList<>();
        String terms[];
        query = query.trim();

        query = processedText(query);

        terms = query.split("\\s");
        for(String t : terms) {
            t = processedWord(t);
            if(!(t.trim().equals("")) && !(t.trim().equals("//s"))){
                queryTerms.add(t.trim());
            }
        }
        return queryTerms;
    }

    public static String processedText(String line) {

        return (line.trim().toLowerCase()
                .replaceAll("\\("," ")
                .replaceAll("\\)"," ")
                .replaceAll("(\\r)", " "));
    }
}
