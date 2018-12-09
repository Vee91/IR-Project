package project;
import java.util.List;
import java.util.Set;

public class Query {

	private int queryId;
	private String query;
	private List<Ranks> output;
	private Set<Integer> relevantDocs;

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

	public Set<Integer> getRelevantDocs() {
		return relevantDocs;
	}

	public void setRelevantDocs(Set<Integer> relevantDocs) {
		this.relevantDocs = relevantDocs;
	}

	
}
