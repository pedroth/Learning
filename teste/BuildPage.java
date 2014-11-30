package teste;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import tokenizer.SufixTreeTokenizer;

public class BuildPage {

	public static void main(String[] args) {
		String[] special = { "<!--Special-->" };
		String name = "SimplePhysics";
		
		String text = "\n\n<h1>" + name + "</h1>\n" +   
				"<object type=\"application/x-java-applet\" height=\"100\" width=\"250\">\n" +
				"<param name=\"code\" value=\"apps.FrameApplet.class\" />\n" +
				"<param name=\"archive\" value=\"" + name + "Applet.jar\" />\nApplet failed to run.  No Java plug-in was found.\n" +
				" </object>\n" +
				" <p>input : </p><p>&lt; h &gt; : help button.</p>\n<p><a href='" + name + ".zip'> download applet and play it faster</a>\n" +
				"</p></div></td></tr></tbody></table>";
		try {
			SufixTreeTokenizer parser = new SufixTreeTokenizer(special);
			parser.init();
			BufferedReader reader = new BufferedReader(new FileReader("C:/Users/pedro/Desktop/canon.html"));
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/pedro/Desktop/"+name+".html"));
			String line;
			while ((line = reader.readLine()) != null ) {
				String[] aux = parser.tokenize(line);
				System.out.println(line);
				if (aux.length == 0) {
					writer.write(line + "\n");
				} else {
					writer.write(text + "\n");
				}
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
