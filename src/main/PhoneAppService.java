package main;

import java.util.List;

public class PhoneAppService {
    private final PhoneList phoneList;
    private final AIService aiService;

    public PhoneAppService(PhoneList phoneList, AIService aiService) {
        this.phoneList = phoneList;
        this.aiService = aiService;
    }

    public PhoneEvaluationResponse evaluatePhone(String model, int storage, String damageDescription) {
        Phone phone = new Phone(model, storage, damageDescription, 0.0);
        EvaluationResult evaluationResult = aiService.evaluate(phone);
        phoneList.addPhone(phone);
        return new PhoneEvaluationResponse(phone, evaluationResult);
    }

    public List<Phone> searchByModel(String model) {
        return phoneList.searchByModel(model);
    }

    public List<Phone> searchByPrice(double min, double max) {
        return phoneList.searchByPrice(min, max);
    }

    public List<Phone> getAllPhones() {
        return phoneList.getAllPhones();
    }
}
