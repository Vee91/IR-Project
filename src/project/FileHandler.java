package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {

	private BufferedWriter _BW;
	private FileWriter _FW;
	private BufferedReader _BR;
	private FileReader _FR;

	public FileHandler(String fileName, boolean writer) throws IOException {
		if (writer) {
			_FW = new FileWriter(fileName);
			_BW = new BufferedWriter(_FW);
		} else {
			_FR = new FileReader(fileName);
			_BR = new BufferedReader(_FR);
		}
	}

	public void emptyFile() throws IOException {
		_BW.write("");
	}

	public void appendString(String str) throws IOException {
		_BW.append(str);
	}

	public String readLine() throws IOException {
		return _BR.readLine();
	}

	public void closeConnection() throws IOException {
		if (_BW != null) {
			_BW.close();
		}
		if (_FW != null) {
			_FW.close();
		}
		if (_FR != null) {
			_FR.close();
		}
		if (_BR != null) {
			_BR.close();
		}
	}

}
