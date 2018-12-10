package project;

import project.Utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class SnippetGeneration {
public static void main(String[] args) throws IOException {
    SnippetGeneration sg=new SnippetGeneration();
    SnippetGeneration.WriteToHml(BaselineRuns.loadQueries(),"Baseline/BM25.txt");
    }

    public static List<String> generateSnippets(String query,String docid) throws IOException {
        List<String> QueryTerms=Utilities.getQueryTerms(query);
        String[] queryTermArray = new String[QueryTerms.size()];
        List<String> snippets=new ArrayList<>();
        snippets = SnippetGeneration
                .generateSnips(QueryTerms.toArray(queryTermArray),docid,3 );
        if (snippets.size()!=0)
            return snippets;

        snippets = SnippetGeneration
                .generateSnips(QueryTerms.toArray(queryTermArray),docid,2 );
        if (snippets.size()!=0)
            return snippets;
        snippets = SnippetGeneration
                .generateSnips(QueryTerms.toArray(queryTermArray),docid,1 );
        if (snippets.size()!=0)
            return snippets;
        return snippets;
    }

    public static void WriteToHml(List<Query> queries,String resultPath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("Results/snipeet.html"));
        String docContents = new String(Files.readAllBytes(Paths.get(resultPath)));
        String[] resultArray=docContents.split("\n");
        writer.write("<!DOCTYPE html>");
        for (Query query:
             queries) {
            writer.write("{Query id = " + query.getQueryId() + " }<br />");
            for(String docDetails:resultArray) {
                String[] lineContents=docDetails.split(" ");
                if(lineContents.length>=5)
                {   if(Integer.parseInt(lineContents[0])==query.getQueryId()) {
                        writer.write("{Doc Name = " + lineContents[2] + " }<br />");
                        writer.write(" {Snippet} <br />");
                        System.out.println(lineContents[2]);
                        List<String> snippets = generateSnippets(query.getQuery(), lineContents[2]+".html");
                        writer.write(String.join("...", snippets) + "<br />");
                        writer.write(" {\\Snippet} <br />");
                        writer.write("{\\Doc Name = " + lineContents[2] + " }<br />");
                        writer.write("<br \\>");
                    }
                }
            }
            writer.write("{/Query}<br />");
        }

        writer.close();
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
                    System.out.println(startIndex);
                    while (docContents.charAt(startIndex)!=' ' && docContents.charAt(startIndex)!='\n' && startIndex!=0)
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
                snipetSentences.add(startingterm+" <mark>["+gram+"]</mark> "+endingTerm);
            }
        }
        return snipetSentences;
    }
}