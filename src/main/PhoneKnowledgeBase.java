package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PhoneKnowledgeBase {
    private static final List<PhoneReference> REFERENCES = buildReferences();

    private PhoneKnowledgeBase() {}

    public static List<PhoneReference> getReferences() {
        return REFERENCES;
    }

    private static List<PhoneReference> buildReferences() {
        List<PhoneReference> references = new ArrayList<>();

        addAppleLineup(references);

        references.add(new PhoneReference("Samsung", "Samsung Galaxy S24", 128, 699.00));
        references.add(new PhoneReference("Samsung", "Samsung Galaxy S24 Ultra", 256, 1049.00));
        references.add(new PhoneReference("Samsung", "Samsung Galaxy S23", 128, 499.00));
        references.add(new PhoneReference("Samsung", "Samsung Galaxy A54", 128, 249.00));

        references.add(new PhoneReference("Google", "Google Pixel 8", 128, 499.00));
        references.add(new PhoneReference("Google", "Google Pixel 8 Pro", 128, 699.00));
        references.add(new PhoneReference("Google", "Google Pixel 7", 128, 349.00));

        references.add(new PhoneReference("OnePlus", "OnePlus 12", 256, 599.00));
        references.add(new PhoneReference("Motorola", "Motorola Edge 2024", 256, 399.00));

        return Collections.unmodifiableList(references);
    }

    private static void addAppleLineup(List<PhoneReference> references) {
        references.add(new PhoneReference("Apple", "iPhone 11", 64, 299.00));
        references.add(new PhoneReference("Apple", "iPhone 11 Pro", 64, 379.00));
        references.add(new PhoneReference("Apple", "iPhone 11 Pro Max", 64, 429.00));

        references.add(new PhoneReference("Apple", "iPhone 12 mini", 64, 319.00));
        references.add(new PhoneReference("Apple", "iPhone 12", 64, 359.00));
        references.add(new PhoneReference("Apple", "iPhone 12 Pro", 128, 439.00));
        references.add(new PhoneReference("Apple", "iPhone 12 Pro Max", 128, 499.00));

        references.add(new PhoneReference("Apple", "iPhone 13 mini", 128, 399.00));
        references.add(new PhoneReference("Apple", "iPhone 13", 128, 439.00));
        references.add(new PhoneReference("Apple", "iPhone 13 Pro", 128, 559.00));
        references.add(new PhoneReference("Apple", "iPhone 13 Pro Max", 128, 629.00));

        references.add(new PhoneReference("Apple", "iPhone 14", 128, 569.00));
        references.add(new PhoneReference("Apple", "iPhone 14 Plus", 128, 619.00));
        references.add(new PhoneReference("Apple", "iPhone 14 Pro", 128, 739.00));
        references.add(new PhoneReference("Apple", "iPhone 14 Pro Max", 128, 829.00));

        references.add(new PhoneReference("Apple", "iPhone 15", 128, 729.00));
        references.add(new PhoneReference("Apple", "iPhone 15 Plus", 128, 809.00));
        references.add(new PhoneReference("Apple", "iPhone 15 Pro", 128, 899.00));
        references.add(new PhoneReference("Apple", "iPhone 15 Pro", 256, 999.00));
        references.add(new PhoneReference("Apple", "iPhone 15 Pro Max", 256, 1079.00));

        references.add(new PhoneReference("Apple", "iPhone 16", 128, 829.00));
        references.add(new PhoneReference("Apple", "iPhone 16 Plus", 128, 909.00));
        references.add(new PhoneReference("Apple", "iPhone 16 Pro", 128, 1049.00));
        references.add(new PhoneReference("Apple", "iPhone 16 Pro Max", 256, 1199.00));
        references.add(new PhoneReference("Apple", "iPhone 16e", 128, 599.00));

        references.add(new PhoneReference("Apple", "iPhone 17", 256, 799.00));
        references.add(new PhoneReference("Apple", "iPhone 17 Air", 256, 999.00));
        references.add(new PhoneReference("Apple", "iPhone 17 Pro", 256, 1099.00));
        references.add(new PhoneReference("Apple", "iPhone 17 Pro Max", 256, 1199.00));
        references.add(new PhoneReference("Apple", "iPhone 17e", 256, 599.00));
    }

    public static final class PhoneReference {
        private final String brand;
        private final String model;
        private final int storage;
        private final double marketPrice;

        public PhoneReference(String brand, String model, int storage, double marketPrice) {
            this.brand = brand;
            this.model = model;
            this.storage = storage;
            this.marketPrice = marketPrice;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public int getStorage() {
            return storage;
        }

        public double getMarketPrice() {
            return marketPrice;
        }
    }
}
