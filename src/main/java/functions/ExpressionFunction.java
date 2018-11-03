package functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import functionNode.ACosNode;
import functionNode.ASinNode;
import functionNode.ATanNode;
import functionNode.AddNode;
import functionNode.Atan2Node;
import functionNode.CombinationNode;
import functionNode.ConstantNode;
import functionNode.CosNode;
import functionNode.DivNode;
import functionNode.DummyVariableNode;
import functionNode.ExpNode;
import functionNode.FunctionNode;
import functionNode.LnNode;
import functionNode.MinNode;
import functionNode.MultNode;
import functionNode.NegNode;
import functionNode.PedroNode;
import functionNode.PowNode;
import functionNode.Sigma;
import functionNode.SinNode;
import functionNode.SubNode;
import functionNode.TanNode;
import functionNode.VarNode;
import tokenizer.NumbersTokenizer;
import tokenizer.TokenRecognizer;

public class ExpressionFunction extends DoubleFunction {
	private TokenRecognizer tokenRecog;
	/**
	 * maps name of variables to its position in the arguments
	 */
	private Map<String, Integer> varNametoInt;
	/**
	 * maps name of functions to the function object
	 */
	private Map<String, FunctionNode> functionNametoFunc;
	/**
	 * maps name of the operators to its priority
	 * <p>
	 * priority belongs to the integer number set. higher priority means an
	 * higher number.
	 */
	private Map<String, Integer> operatorNametoPriotity;
	/**
	 * maps constants names to constant node
	 */
	private Map<String, ConstantNode> constantNametoNode;
	private FunctionNode posfixExpr;
	private String[] lexOut;
	/**
	 * input
	 */
	private String expr;
	private String[] vars;
	/**
	 * all tokens that program will use
	 */
	private Vector<String> tokens;
	/**
	 * maps dummy variable name to its stack
	 */
	private Map<String, Stack<Double>> dummyVarNametoStack;

	/**
	 * @param expr
	 *            = mathematical expression. ex: sin(x + 3 * y / exp(pi * z)) ^
	 *            2
	 * @param vars
	 *            = array with the name of variables, order of the name matters.
	 *            Names that are equal to function will not be recognized. Ex
	 *            that works: "x","xyz","x1","mariacachucha". badEx: "exp",
	 *            "sin(x)".
	 */
	public ExpressionFunction(String expr, String[] vars) {
		this.expr = expr;

		if (vars != null)
			this.vars = vars;
		else
			this.vars = new String[0];

		varNametoInt = new HashMap<>();
		operatorNametoPriotity = new HashMap<>();
		functionNametoFunc = new HashMap<>();
		constantNametoNode = new HashMap<>();
		tokens = new Vector<>();
		dummyVarNametoStack = new HashMap<>();
	}

	public static void main(String[] args) {
		String[] varTokens = { "u", "x", "y" };
		// ExpressionFunction foo = new
		// ExpressionFunction("pedro(cos(t)*cos(2*t),sin(t)*cos(2*t),0,2*pi,x,y)",
		// varTokens);
		ExpressionFunction foo = new ExpressionFunction("C(1,3)", varTokens);
		String[] dummyVar = { "i" };
		foo.addFunction("sigma", new Sigma(dummyVar, foo));
		/**
		 * you must create to variables of String[]
		 */
		String[] dummyVar2 = { "t" };
		foo.addFunction("pedro", new PedroNode(dummyVar2, foo));
		foo.addFunction("C", new CombinationNode());
		foo.init();
		Double[] vars = { 3.141592, 0.0, 3.0 };
		double oldTime = System.nanoTime() * 1E-9;
		System.out.println(foo.compute(vars));
		System.out.println(System.nanoTime() * 1E-9 - oldTime);
	}

	public void init() {
		// add functions here or in main
		addOperator("+", 1, new AddNode());

		addOperator("-", 1, new SubNode());

		addOperator("*", 2, new MultNode());

		addOperator("/", 2, new DivNode());

		addOperator("^", 5, new PowNode());

		addOperator("u-", 4, new NegNode());

		addFunction("sin", new SinNode());

		addFunction("cos", new CosNode());

		addFunction("exp", new ExpNode());

		addFunction("tan", new TanNode());

		addFunction("ln", new LnNode());

		addFunction("atan", new ATanNode());

		addFunction("acos", new ACosNode());

		addFunction("asin", new ASinNode());

		addFunction("min", new MinNode());

		addFunction("atan2", new Atan2Node());

		addConstant("pi", new ConstantNode(Math.PI));

		tokens.add("(");

		tokens.add(")");

		tokens.add(",");

		for (int i = 0; i < vars.length; i++) {
			tokens.add(vars[i]);
			varNametoInt.put(vars[i], i);
		}
		lexAnalysis(expr);

		SyntaxAnalysis();
	}

	private void SyntaxAnalysis() throws SyntaxErrorException {
		Vector<String> stack = new Vector<String>();
		Vector<FunctionNode> output = new Vector<FunctionNode>();
		int size = lexOut.length;
		String aux;
		try {
			for (int i = 0; i < size; i++) {
				aux = lexOut[i];
				if (aux == "(") {

					stack.add(aux);

				} else if (aux == ")") {

					String s = stack.lastElement();
					while (s != "(") {
						popFunction(stack, output);
						s = stack.lastElement();
					}
					stack.remove(stack.size() - 1);
					if (!stack.isEmpty() && functionNametoFunc.get(stack.lastElement()) != null && operatorNametoPriotity.get(stack.lastElement()) == null) {
						popFunction(stack, output);
					}

				} else if (aux == ",") {

					String s = stack.lastElement();
					while (s != "(") {
						popFunction(stack, output);
						s = stack.lastElement();
					}

				} else if (varNametoInt.get(aux) != null) {

					output.add(new VarNode(varNametoInt.get(aux)));

				} else if (dummyVarNametoStack.get(aux) != null) {

					output.add(new DummyVariableNode(aux, this));

				} else if (operatorNametoPriotity.get(aux) != null && aux != "^") {

					int opPriority = operatorNametoPriotity.get(aux);
					while (!stack.isEmpty() && operatorNametoPriotity.get(stack.lastElement()) != null && opPriority <= operatorNametoPriotity.get(stack.lastElement())) {
						popFunction(stack, output);
					}
					stack.add(aux);
				} else if (operatorNametoPriotity.get(aux) != null && aux == "^") {

					int opPriority = operatorNametoPriotity.get(aux);
					while (!stack.isEmpty() && operatorNametoPriotity.get(stack.lastElement()) != null && opPriority < operatorNametoPriotity.get(stack.lastElement())) {
						popFunction(stack, output);
					}
					stack.add(aux);

				} else if (functionNametoFunc.get(aux) != null) {

					stack.add(aux);

				} else if (constantNametoNode.get(aux) != null) {

					output.add(constantNametoNode.get(aux));

				} else {

					output.add(new ConstantNode(Double.parseDouble(aux)));

				}
			}
			int stackSize = stack.size();
			for (int j = 0; j < stackSize; j++) {
				popFunction(stack, output);
			}

			posfixExpr = output.lastElement();

		} catch (Exception e) {
			e.printStackTrace();
			throw new SyntaxErrorException();
		}
	}

	private void lexAnalysis(String expr) {
		String[] patterns = tokens.toArray(new String[0]);
		tokenRecog = new NumbersTokenizer(patterns);
		tokenRecog.init();
		lexOut = tokenRecog.tokenize(expr);
		checkForUnitaryOp(lexOut);
	}

	private void checkForUnitaryOp(String[] lex) {
		boolean changeToUna = false;
		for (int i = 0; i < lex.length; i++) {
			if (lex[i] == "-") {
				if (i == 0)
					changeToUna = true;
				else if (operatorNametoPriotity.get(lex[i - 1]) != null)
					changeToUna = true;
				else if (lex[i - 1] == "(")
					changeToUna = true;
				else if (lex[i - 1] == ",")
					changeToUna = true;

				if (changeToUna)
					lex[i] = "u-";

				changeToUna = false;
			}
		}
	}

	@Override
	public Double compute(Double[] variables) {
		if (variables.length != vars.length)
			throw new Error("number of variables don�t match");
		return posfixExpr.compute(variables);
	}

	private void popFunction(Vector<String> stack, Vector<FunctionNode> output) {
		String s = stack.lastElement();
		stack.remove(stack.size() - 1);
		FunctionNode f = null;
		f = functionNametoFunc.get(s);
		FunctionNode[] args = new FunctionNode[f.getnVars()];
		int outsize = output.size();
		for (int j = 0; j < f.getnVars(); j++) {
			args[f.getnVars() - j - 1] = output.get(outsize - j - 1);
			output.remove(outsize - j - 1);
		}
		output.add(f.createNode(args));
	}

	public void addOperator(String token, int priority, FunctionNode node) {
		tokens.add(token);
		functionNametoFunc.put(token, node);
		operatorNametoPriotity.put(token, priority);
	}

	public void addFunction(String token, FunctionNode node) {
		tokens.add(token);
		functionNametoFunc.put(token, node);
	}

	public void addConstant(String token, ConstantNode node) {
		tokens.add(token);
		constantNametoNode.put(token, node);
	}

	public void pushDummyVar(String dummyVarName, double x) {
		Stack<Double> aux = dummyVarNametoStack.get(dummyVarName);
		aux.push(x);
	}

	public double popDummyVar(String dummyVarName) {
		Stack<Double> aux = dummyVarNametoStack.get(dummyVarName);
		return aux.pop();
	}

	public double peekDummyVar(String dummyVarName) {
		Stack<Double> aux = dummyVarNametoStack.get(dummyVarName);
		return aux.peek();
	}

	public void addToken(String s) {
		tokens.add(s);
	}

	public void putDummyVarintoMap(String dummyVarName, Stack<Double> stack) {
		if (dummyVarNametoStack.get(dummyVarName) == null) {
			dummyVarNametoStack.put(dummyVarName, stack);
		}
	}
}
