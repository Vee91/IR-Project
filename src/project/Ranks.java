package project;

public class Ranks {

	private int rank;
	private int docId;
	private double score;
	private double precision;
	private double recall;
	private String snippet;

	public Ranks(int dId, double currentScore) {
		this.docId = dId;
		this.score = currentScore;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	@Override
	public String toString() {
		return "Ranks [rank=" + rank + ", docId=" + docId + ", score=" + score + "]";
	}

	
}
