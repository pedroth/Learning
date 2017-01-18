package tokenizer;

public abstract class TokenRecognizer {
    private String[] patterns;

    TokenRecognizer(String[] patterns) {
        this.patterns = patterns;
    }

    public String[] getPatterns() {
        return patterns;
    }

    public void setPatterns(String[] patterns) {
        this.patterns = patterns;
    }

    public abstract String[] tokenize(String s);

    public abstract void init();
}
