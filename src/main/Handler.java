package main;

import java.util.List;
import java.util.Scanner;

public class Handler {
    private final Scanner in;
    private final PhoneAppService phoneAppService;

    public Handler(Scanner in, PhoneAppService phoneAppService) {
        this.in = in;
        this.phoneAppService = phoneAppService;
    }

    public void printChoices(){
        System.out.println("======== Phone Valuation Tool ========");
        System.out.println("\n1. Evaluate my phone");
        System.out.println("2. Search phones");
        System.out.println("3. View all phones");
        System.out.println("0. Exit");
        System.out.print("Choice: ");
    }

    public void searchMenu() {
        System.out.println("======== Search for Phone ========");
        System.out.println("\n1. Search by model");
        System.out.println("2. Search by price range");
        System.out.print("Choice: ");
        Integer choice = readIntLine();
        if (choice == null) {
            System.out.println("Invalid option.");
            return;
        }
        if (choice == 1) {
            System.out.println("======== Search for Phone By Model ========");
            searchByModel();
        } else if (choice == 2) {
            System.out.println("======== Search for Phone By Price ========");
            searchByPrice();
        } else {
            System.out.println("Invalid option.");
        }
    }

    public void evaluatePhone() {
        System.out.println("======== Evaluate Phone ========");
        System.out.print("Please enter the Model Name (Ex: iPhone 15 Pro) \nModel name: ");
        String model = in.nextLine().trim();
        System.out.print("Please enter phone storage (EX: 15)\nStorage (GB): ");
        Integer storage = readIntLine();
        if (storage == null) {
            System.out.println("Storage must be a whole number.");
            return;
        }
        System.out.print("Describe any damage (or \"none\"): ");
        String damage = in.nextLine().trim();
        PhoneEvaluationResponse response = phoneAppService.evaluatePhone(model, storage, damage);
        Phone phone = response.getPhone();
        EvaluationResult result = response.getEvaluationResult();
        System.out.println("\nAI source: " + result.getSourceLabel());
        System.out.println("Used external AI: " + (result.usedExternalAI() ? "Yes" : "No"));
        System.out.println("Evaluation: " + result.getExplanation());
        System.out.printf("Estimated price: $%.2f%n", phone.getEstimatedPrice());
        System.out.println("Phone added to the database.");
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    private void searchByModel() {
        System.out.print("Please enter the Model Name (EX: 'iPhone' or 'iPhone 15 Pro')\nModel to search: ");
        String model = in.nextLine().trim();
        printResults(phoneAppService.searchByModel(model));
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    private void searchByPrice() {
        System.out.print("Please enter the minimum price (EX: 100.00 or 100)\nMin price ($): ");
        Double min = readDoubleLine();
        if (min == null) {
            System.out.println("Minimum price must be a number.");
            return;
        }
        System.out.print("Please enter the maximum price (EX: 100.00 or 100)\nMax price ($): ");
        Double max = readDoubleLine();
        if (max == null) {
            System.out.println("Maximum price must be a number.");
            return;
        }
        printResults(phoneAppService.searchByPrice(min, max));
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    public void viewAll() {
        printResults(phoneAppService.getAllPhones());
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    private void printResults(List<Phone> results) {
        if (results.isEmpty()) {
            System.out.println("No phones found.");
            return;
        }
        System.out.println();
        System.out.printf("%n%-25s %-13s %-35s %s%n", "Phone Model", "Storage (GB)", "Condition", "Estimated Price");
        for (int i = 0; i < results.size(); i++) {
            System.out.println(results.get(i));
        }
    }

    private Integer readIntLine() {
        String input = in.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double readDoubleLine() {
        String input = in.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
