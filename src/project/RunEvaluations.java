package project;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunEvaluations {
    static Map<Integer,List<String>> reldocs;
    static Map<Integer,List<String>> fetchedDocs;

    public RunEvaluations(){
        reldocs=new HashMap<Integer, List<String>>();
        fetchedDocs=new HashMap<Integer, List<String>>();
    }

    private static void createRelevantDic() throws IOException {
        String docContents = new String(Files.readAllBytes(Paths.get("RelevantDoc\\" + "cacm.rel.txt")));
        String[] lines=docContents.split("\n");
        for (String line:
             lines) {
            String[] lineContens=line.split(" ");
            reldocs.computeIfPresent(Integer.parseInt(lineContens[0]),(k,v)->{
                v.add(lineContens[2]);
                return v;
            });
            if (!reldocs.containsKey(Integer.parseInt(lineContens[0])))
            {
                List<String> temp=new ArrayList<String>();
                temp.add(lineContens[2]);
                reldocs.put(Integer.parseInt(lineContens[0]),temp);
            }
        }
    }

    private static void createBaseLineDic(String path) throws IOException {
        String docContents = new String(Files.readAllBytes(Paths.get(path)));
        String[] lines=docContents.split("\n");
        for (String line:
                lines) {
            String[] lineContens=line.split(" ");
            fetchedDocs.computeIfPresent(Integer.parseInt(lineContens[0]),(k,v)->{
                v.add(lineContens[2]);
                return v;
            });
            if(!fetchedDocs.containsKey(Integer.parseInt(lineContens[0])))
            {
                List<String> temp=new ArrayList<String>();
                temp.add(lineContens[2]);
                fetchedDocs.put(Integer.parseInt(lineContens[0]),temp);
            }
        }
        for (Map.Entry<Integer,List<String>> me:
             fetchedDocs.entrySet()) {
            if(me.getValue()==null)
            {
                fetchedDocs.remove(me.getKey());
            }
        }
    }

    public static void Evaluate(String outputPath) throws IOException {
        String tableValue="";
        double sumAvgPrecision = 0.0;
        double sumRelevanceRank = 0.0;
        for (Map.Entry<Integer,List<String>> entry:
                fetchedDocs.entrySet()) {
            double avgPrecision=0.0;
            double relevanceRank=0.0;
            double precisionSum=0;
            double rank=0.0;
            List<String> tempBaseDocs=new ArrayList<String>();
            List<String> tempRelDocs=new ArrayList<String>();
            double precisionAt5=0.0;
            double precisionAt20=0.0;
            for (String doc:
                 entry.getValue()) {
                 rank=rank+1;
                 tempBaseDocs.add(doc);
                List<String> relevantDocs=reldocs.get(entry.getKey());
                if(relevantDocs==null)
                    relevantDocs=new ArrayList<>();
                if (relevantDocs.contains(doc))
                {
                    if (tempRelDocs.size()==0) {
                        relevanceRank = 1 / rank;
                        sumRelevanceRank = sumRelevanceRank + relevanceRank;
                    }
                    tempRelDocs.add(doc);
                    precisionSum=precisionSum+(tempRelDocs.size()/(double)tempBaseDocs.size());
                }
                if (rank==5)
                    precisionAt5=(tempRelDocs.size()/(double)tempBaseDocs.size());
                if(rank==20)
                    precisionAt20=(tempRelDocs.size()/(double)tempBaseDocs.size());
                tableValue+= entry.getKey() + " "+ rank + " " + doc + " " + tempRelDocs.size()
                        + "/" + tempBaseDocs.size() + " " + tempRelDocs.size() +
                        "/" + relevantDocs.size()+"\n";
                if (precisionSum!=0)
                    avgPrecision=precisionSum/(double)tempRelDocs.size();
            }
            sumAvgPrecision+=avgPrecision;
            tableValue+= "avg precision is" + " " + avgPrecision+"\n";
            tableValue+= "RR is" + " " + relevanceRank+"\n";
            tableValue+= "P@5=" + " " + precisionAt5 + " " + "P@20=" + " " + precisionAt20 + "\n";
        }
        double MAP = sumAvgPrecision/reldocs.size();
        double MRR = sumRelevanceRank/reldocs.size();
        tableValue+= "\n" + "MAP is" + " " + MAP + " " + "MRR is" + " " + MRR+"\n";

        Path path = Paths.get(outputPath);
        byte[] strToBytes = tableValue.getBytes();

        Files.write(path, strToBytes);
    }

    public static void main(String args[]) throws IOException {
        RunEvaluations re=new RunEvaluations();
        re.createRelevantDic();
        re.createBaseLineDic("Results/BM25Run.txt");
        re.Evaluate("Results/BM25Evaluation.txt");
    }
}
