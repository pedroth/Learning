package functionNode;


public class Atan2Node extends BinaryNode {

    public Atan2Node() {
    }

    public Atan2Node(FunctionNode[] args) {
        super(args);
    }

    @Override
    public FunctionNode createNode(FunctionNode[] args) {
        return new Atan2Node(args);
    }

    @Override
    public Double compute(Double[] variables) {
        return Math.atan2(args[0].compute(variables), args[1].compute(variables));
    }
}
