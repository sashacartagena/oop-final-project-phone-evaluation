package main;

public class PhoneEvaluationResponse {
    private final Phone phone;
    private final EvaluationResult evaluationResult;

    public PhoneEvaluationResponse(Phone phone, EvaluationResult evaluationResult) {
        this.phone = phone;
        this.evaluationResult = evaluationResult;
    }

    public Phone getPhone() {
        return phone;
    }

    public EvaluationResult getEvaluationResult() {
        return evaluationResult;
    }
}
