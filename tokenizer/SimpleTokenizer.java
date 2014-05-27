package tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * 
 * @author pedro
 *
 *it does not work very well with some words, for instance if you have x,sin,gauss,exp
 *
 *and the string "expsinh" it will be recognized "exps" instead of "exp", "sin".
 *
 *
 *this probabilistic method is not very good it was an experiment
 *
 */
public class SimpleTokenizer extends TokenRecognizer {
	int[][] freqTable;
	int maxStrSize;
	Map<String, Boolean> tokenMap;
	double avgStrSize;
	double correctProb;
	double stringProb;
	int[] wordCount;
	double likewood;
	double strGivenWrong;

	SimpleTokenizer(String[] patterns) {
		super(patterns);
		maxStrSize = Integer.MIN_VALUE;
		tokenMap = new Hashtable<String, Boolean>();
		likewood = 1;
		strGivenWrong = 1;
	}

	public void init() {
		int acm = 0;
		String[] p = this.getPatterns();
		for (int i = 0; i < p.length; i++) {
			int max = p[i].length();
			acm += p[i].length();
			maxStrSize = Math.max(max, maxStrSize);
			tokenMap.put(p[i], true);
		}
		avgStrSize = acm / p.length;
		freqTable = new int['z' - 'a' + 1][maxStrSize];
		wordCount = new int[maxStrSize];

		for (int i = 0; i < p.length; i++) {
			for (int j = 0; j < p[i].length(); j++) {
				int count = p[i].charAt(j) - 'a';
				freqTable[count][j]++;
				wordCount[j]++;
			}
		}
		correctProb = (double) 0.25; // prior
		// probability,which is
		// probability
		// of a word
		// being
		// correct
		stringProb = (double) 1 / ('z' - 'a' + 1); // probability of a certain
													// letter
	}

	public double computeProb(Vector<Character> stack) {
		double dx = 1E-26;
		int size = stack.size();
		char aux;
		aux = stack.get(size - 1);
		if ((size - 1) > maxStrSize - 1 || aux == '$') {
			likewood *= dx;
		} else {
			likewood *= ((double) freqTable[aux - 'a'][size - 1] / wordCount[size - 1]);
			strGivenWrong *= ((double) 1 * stringProb);
		}
		return (likewood * correctProb)
				/ ((likewood * correctProb) + (1 - correctProb) * strGivenWrong);
		// Math.pow(Math.ceil(avgStrSize * 0.75) * stringProb,
		// size));
	}

	public String addString(Vector<Character> stack) {
		String aux = new String();
		Iterator<Character> i = stack.iterator();
		while (i.hasNext()) {
			aux += i.next();
		}
		return aux;
	}

	@Override
	public String[] tokenize(String s) {
		s += "$";
		int size = s.length();
		double oldProb = 0;
		double newProb = 0;
		double dp = 0;
		Vector<String> answer = new Vector<String>();
		Vector<Character> stack = new Vector<Character>();
		for (int i = 0; i < size; i++) {
			Character aux = s.charAt(i);
			stack.add(aux);
			newProb = computeProb(stack);
			dp = newProb - oldProb;
			if (dp == 0.0) {
				stack.removeAllElements();
				likewood = 1;
				strGivenWrong = 1;
				continue;
			}
			/**
			 * debug prints
			 */
			String help = "";
			for (int k = 0; k < newProb * 20; k++) {
				help += "*";
			}
			System.out.println(addString(stack) + " : "
					+ String.format("%.5f", newProb) + " : " + help);

			if (dp < 0) {
				stack.remove(stack.size() - 1);
				String key = addString(stack);
				if (oldProb - 0.70 > 0) {
					/**
					 * if not commented  complexity raise.
					 */
					try {
					// if (tokenMap.get(key)) {
						 answer.add(key);
					 //} else {

					 //}
					 } catch (NullPointerException e) {
					// do nothing
					 }
				}
				likewood = 1;
				strGivenWrong = 1;
				stack.removeAllElements();
				i--;
			}
			oldProb = newProb;
		}
		return answer.toArray(new String[0]);
	}

	public static void main(String args[]) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String[] s = { "x", "exp", "gauss", "euler", "pedro", "sin", "cos",
				"ln", "sinh", "cosh", "tanh", "acos", "asin", "acosh", "asinh", "she", "hers" };
		SimpleTokenizer st = new SimpleTokenizer(s);
		st.init();
		String line = null;
		try {
			line = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] tokens = st.tokenize(line);
		for (int i = 0; i < tokens.length; i++) {
			System.out.println(tokens[i]);
		}
	}
}
