package visualization;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import windowThreeDim.Composite;
import windowThreeDim.Line;
import windowThreeDim.Triangle;
import algebra.TriVector;

public class ObjParser {
	private ArrayList<TriVector> vertex;
	private Composite obj;
	private String adress;

	public ObjParser(String s) {
		adress = s;
		obj = new Composite();
		vertex = new ArrayList<TriVector>();
	}

	public Composite parse() {
		BufferedReader in;
		try {
			if (isUrl(adress)) {
				URL url;
				url = new URL(adress);
				in = new BufferedReader(new InputStreamReader(url.openStream()));

			} else {
				in = new BufferedReader(new FileReader(adress));
			}

			String line;

			while ((line = in.readLine()) != null) {
				String[] letter = line.split(" ");
				if (letter[0].equals("v")) {
					addVertex(line);
				} else if (letter[0].equals("f")) {
					addFace(line);
				} else if (letter[0].equals("l")) {
					addLine(line);
				} else {
					// do nothing
				}
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	private int parseFaceNumber(String s) {
		return Math.abs(Integer.parseInt(s.split("/")[0]));
	}

	private void addFace(String line) {
		String[] numbers = line.split("\\s+");

		Triangle t = new Triangle(vertex.get(parseFaceNumber(numbers[1]) - 1), vertex.get(parseFaceNumber(numbers[2]) - 1), vertex.get(parseFaceNumber(numbers[3]) - 1));
		t.setColor(Color.gray);
		obj.add(t);
	}

	private void addVertex(String line) {
		String[] numbers = line.split("\\s+");
		TriVector v = new TriVector(Double.parseDouble(numbers[1]), Double.parseDouble(numbers[2]), Double.parseDouble(numbers[3]));
		vertex.add(v);
	}
	
	private void addLine(String line) {
		String[] numbers = line.split("\\s+");
		Line l = new Line(vertex.get(parseFaceNumber(numbers[1]) - 1), vertex.get(parseFaceNumber(numbers[2]) - 1));
		l.setColor(Color.gray);
		obj.add(l);
	}

	private boolean isUrl(String adress) {
		String[] aux = adress.split("http");
		if (aux.length > 1)
			return true;
		else
			return false;
	}

	public static void main(String[] args) {
		ObjParser obj = new ObjParser("http://graphics.stanford.edu/~mdfisher/Data/Meshes/bunny.obj");
		Composite c = obj.parse();
		System.out.println("hello");
	}

}
