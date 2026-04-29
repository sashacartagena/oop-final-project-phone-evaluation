package main;

public class EvaluationResult {
    private final String explanation;
    private final String sourceLabel;
    private final boolean usedExternalAI;
    private final String retrievalContext;

    public EvaluationResult(String explanation, String sourceLabel, boolean usedExternalAI) {
        this(explanation, sourceLabel, usedExternalAI, "");
    }

    public EvaluationResult(String explanation, String sourceLabel, boolean usedExternalAI, String retrievalContext) {
        this.explanation = explanation;
        this.sourceLabel = sourceLabel;
        this.usedExternalAI = usedExternalAI;
        this.retrievalContext = retrievalContext;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public boolean usedExternalAI() {
        return usedExternalAI;
    }

    public String getRetrievalContext() {
        return retrievalContext;
    }
}
