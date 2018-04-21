package teste;

import java.awt.Button;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFrame;

import tokenizer.NumbersTokenizer;
import algebra.Matrix;

public class MarkovExperiment extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TextArea in1, in2, out;
	private TextField numberOfIt;
	private Matrix markovGraph;
	private Matrix measures;

	public MarkovExperiment() {
		in1 = new TextArea("Markov Matrix, must be a square matrix");
		in2 = new TextArea(
				"Measures, must be a n * l where l is numbers of rows of the markov matrix");
		out = new TextArea();
		numberOfIt = new TextField("100");

		Button button = new Button("Generate outPut");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				readMatrices();
				simulateMarkov(Integer.parseInt(numberOfIt.getText()));

			}
		});
		Panel panel = new Panel();
		panel.add(in1);
		panel.add(in2);
		panel.add(out);
		panel.add(numberOfIt);
		panel.add(button);
		this.add(panel);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Set JFrame size
		setSize(500, 720);
		setResizable(false);
		setVisible(true);
	}

	public Matrix parseMatrix(String s) {
		String[] lines = s.split("\\r?\\n");
		NumbersTokenizer tok = new NumbersTokenizer(null);
		String[] x = tok.tokenize(lines[0]);

		Matrix mat = new Matrix(lines.length, x.length);
		for (int i = 1; i <= x.length; i++)
			mat.setMatrix(1, i, Double.parseDouble(x[i - 1]));
		for (int i = 1; i <= lines.length; i++) {
			x = tok.tokenize(lines[i - 1]);
			for (int j = 1; j <= x.length; j++)
				mat.setMatrix(i, j, Double.parseDouble(x[j - 1]));
		}
		return mat;
	}

	public void readMatrices() {
		String in1Txt = in1.getText();
		markovGraph = parseMatrix(in1Txt);
		String in2Txt = in2.getText();
		measures = parseMatrix(in2Txt);
	}

	public void simulateMarkov(int iterations) {
		Matrix outMatrix = new Matrix(measures.getLines(), iterations);
		Matrix states = new Matrix(1, iterations);
		Matrix stateVector = new Matrix(1, markovGraph.getLines());
		stateVector.setMatrix(1, 1, 1.0);
		int state = 1;
		for (int i = 1; i <= iterations; i++) {
			addMeasureGivenState(outMatrix, state, i);
			states.setMatrix(1, i, state);
			stateVector = Matrix.multiMatrix(stateVector, markovGraph);
			state = generateNextState(Matrix.transpose(stateVector));
		}
		String s = states.toString();
		s += "\n\n";
		s += outMatrix.toString();
		out.setText(s);
	}

	private int generateNextState(Matrix stateVector) {
		Random r = new Random();
		int i = 1;
		boolean logic = true;
		while (logic) {
			for (i = 1; i <= stateVector.getLines(); i++) {
				double p = stateVector.selMatrix(i, 1);
				if (r.nextDouble() <= p) {
					logic = false;
					break;
				}
			}
		}
		return i;
	}

	private void addMeasureGivenState(Matrix m, int inState, int numIt) {
		for (int i = 1; i <= m.getLines(); i++) {
			m.setMatrix(i, numIt, measures.selMatrix(i, inState));
		}
	}

	public static void main(String[] args) {
		new MarkovExperiment();
	}
}
