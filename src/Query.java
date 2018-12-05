import java.util.List;

public class Query {

	private int queryId;
	private String query;
	private List<Ranks> output;
	private List<Integer> relevantDocs;

	public int getQueryId() {
		return queryId;
	}

	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<Ranks> getOutput() {
		return output;
	}

	public void setOutput(List<Ranks> output) {
		this.output = output;
	}

	public List<Integer> getRelevantDocs() {
		return relevantDocs;
	}

	public void setRelevantDocs(List<Integer> relevantDocs) {
		this.relevantDocs = relevantDocs;
	}

	
}
