package project;
import java.io.Serializable;
import java.util.List;

public class DTF implements Serializable {

	private static final long serialVersionUID = 1L;
	private int dId;
	private int tf;
	private int pos;


	// change
	private List<Integer> positions;

	public List<Integer> getPositions()
	{
		return positions;
	}

	public void setPositions(List<Integer> positions)
	{
		this.positions = positions;
	}
	//

	public DTF() {

	}

	public DTF(int dId, int tf, int pos) {
		super();
		this.dId = dId;
		this.tf = tf;
		this.pos = pos;
	}

	public DTF(int dId, int tf) {
		super();
		this.dId = dId;
		this.tf = tf;
	}

	public int getdId() {
		return dId;
	}

	public void setdId(int dId) {
		this.dId = dId;
	}

	public int getTf() {
		return tf;
	}

	public void setTf(int tf) {
		this.tf = tf;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	@Override
	public String toString() {
		return "(" + dId + ", " + tf + ", " + pos + ")";
	}

}
