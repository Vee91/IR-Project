import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SnippetGeneration {
public static void main(String[] args) throws IOException {

    String query="\"Languages and compilers for parallel processors, especially highly horizontal microcoded machines; code compaction";
    List<String> QueryTerms=Utilities.getQueryTerms(query);
    String[] queryTermArray = new String[QueryTerms.size()];
    List<String> trisnippetSentences = SnippetGeneration
            .generateSnips(QueryTerms.toArray(queryTermArray),"CACM-2112.html",3 );
    if (trisnippetSentences.size()!=0)
        return;
    List<String> bisnippetSentences = SnippetGeneration
            .generateSnips(QueryTerms.toArray(queryTermArray),"CACM-2112.html",2 );
    if (bisnippetSentences.size()!=0)
        return;
    List<String> unnsnippetSentences = SnippetGeneration
            .generateSnips(QueryTerms.toArray(queryTermArray),"CACM-2112.html",1 );

    }

    public static List<String> generateSnips(String[] queryTerms,String fileName,int size) throws IOException {
        String docContents = new String(Files.readAllBytes(Paths.get("corpus\\" + fileName)));
        List<String> snipetSentences=new ArrayList<String>();
        for (int i=0;i<queryTerms.length-size-1;i++) {
            String gram="";
            if(size==3)
            gram=queryTerms[i]+" "+queryTerms[i+1]+" "+queryTerms[i+2];
            else if(size==2)
                gram=queryTerms[i]+" "+queryTerms[i+1];
            else
                gram=queryTerms[i];
            if(docContents.contains(gram)){
                int trigramIndex=docContents.indexOf(gram);
                int startIndex=Math.max(trigramIndex-50,0);
                String startingterm="";
                String endingTerm="";
                if (startIndex!=0)
                {
                    while (docContents.charAt(startIndex)!=' ' && docContents.charAt(startIndex)!='\n')
                        startIndex-=1;
                    startingterm=docContents.substring(startIndex,trigramIndex);
                }
                int endIndex=Math.min(trigramIndex+50,docContents.length());

                if (endIndex!=docContents.length())
                {
                    while (docContents.charAt(endIndex)!=' ' && docContents.charAt(endIndex)!='\n')
                        endIndex+=1;
                    endingTerm=docContents.substring(trigramIndex+gram.length(),endIndex);
                }
                snipetSentences.add(startingterm+" "+gram+" "+endingTerm);
            }
        }
        return snipetSentences;
    }
}