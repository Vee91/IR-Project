package project;

public class test {
	
	public static void main(String[] args) {
		String bib = "([0-9]+\\s+)([0-9]+\\s+)([0-9]+)";
		System.out.println("7	5	7 \r 6 7 8".replaceAll(bib, "V"));
	}

}
