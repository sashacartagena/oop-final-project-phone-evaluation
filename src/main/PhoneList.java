package main;

import java.util.ArrayList;
import java.util.List;

public class PhoneList {
    private final List<Phone> phones = new ArrayList<>();

    public PhoneList() {}

    public void addPhone(Phone phone) {
        phones.add(phone);
    }

    public List<Phone> searchByModel(String search) {
        String searchModel = search.trim().toLowerCase();
        List<Phone> exact_match = new ArrayList<>();
        List<Phone> partial_match = new ArrayList<>();

        for (int i = 0; i < phones.size(); i++) {
            Phone p = phones.get(i);
            String model = p.getModel().toLowerCase();
            if (model.equals(searchModel)) {
                exact_match.add(p);
            } else if (model.contains(searchModel)) {
                partial_match.add(p);
            }
        }
        List<Phone> results = new ArrayList<>();
        results.addAll(exact_match);
        results.addAll(partial_match);
        return results;
    }

    public List<Phone> searchByPrice(double min, double max) {
        List<Phone> results = new ArrayList<>();
        for (int i = 0; i < phones.size(); i++) {
            Phone p = phones.get(i);
            if (p.getEstimatedPrice() >= min && p.getEstimatedPrice() <= max) {
                results.add(p);
            }
        }
        return results;
    }

    public List<Phone> getAllPhones() {
        return phones;
    }
}
