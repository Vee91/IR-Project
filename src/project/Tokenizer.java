package project;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Tokenizer {

	public static void tokenize(String i) {
		File[] files = new File("docs/raw/").listFiles();
		String endingPattern = "([a-zA-Z]+|\\s+)([\\p{Punct}&&[^-]]+)([a-zA-Z]*)";
		String startingPattern = "([a-zA-Z]*|\\s*)([\\p{Punct}&&[^-]]+)([a-zA-Z]+)";
		String bib = "([0-9]+\\s+)([0-9]+\\s+)([0-9]+)"; // remove numbers at the bottom
		int count = 0;
		for (File f : files) {
			if (f.isFile()) {
				try {
					String str = "";
					Document doc = Jsoup.parse(f, "UTF-8");
					formatDoc(doc);
					// default. Do both casefolding and punctuation handling
					if (i.equals("both")) {
						str = doc.text().toLowerCase();
						while (!str.equals(str.replaceAll(endingPattern, "$1$3"))) {
							str = str.replaceAll(endingPattern, "$1$3");
						}
						while (!str.equals(str.replaceAll(startingPattern, "$1$3"))) {
							str = str.replaceAll(startingPattern, "$1$3");
						}
						str = str.replaceAll(bib, "");
					}
					// no case folding
					else if (i.equals("case_folding")) {
						str = doc.text().toLowerCase();
						str = str.replaceAll(bib, "");
					} else if (i.equals("punct")) {
						while (!str.equals(str.replaceAll(endingPattern, "$1$3"))) {
							str = str.replaceAll(endingPattern, "$1$3");
						}
						str = str.replaceAll(bib, "");
					}

					// no case folding or punctuation handling
					else if (i.equals("false")) {
						str = doc.text();
					}

					saveDocument(f.getName(), str);
					count++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Parsed " + count + " files");
	}

	private static void formatDoc(Document document) {
		document.getElementsByAttributeValue("role", "navigation").remove(); // for external links
		document.getElementsByAttributeValue("class", "mwe-math-element").remove(); // for formulae
		document.getElementsByAttributeValueContaining("class", "reflist").remove(); // for references
		document.getElementsByAttributeValueContaining("class", "mw-redirect").remove(); // for redirect
		document.getElementsByAttributeValue("class", "printfooter").remove();
		document.getElementsByAttributeValue("class", "catlinks").remove();
		document.getElementsByAttributeValue("style", "display:none").remove();
		document.getElementsByAttributeValue("id", "mw-navigation").remove();
		document.getElementsByAttributeValue("role", "contentinfo").remove();
		document.select("table").remove(); // remove tables
		document.select("img").remove(); // remove images
		document.select("form").remove(); // remove forms
		document.select("input").remove(); // remove input
		document.select("sup").remove(); // remove citations
	}

	private static void saveDocument(String docname, String document)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("corpus/" + docname, "UTF-8");
		writer.println(document);
		writer.close();
	}

}
