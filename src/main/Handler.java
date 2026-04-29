package main;

import java.util.List;
import java.util.Scanner;

public class Handler {
    private final Scanner in = new Scanner(System.in);
    private final PhoneList phoneList = new PhoneList();
    private final AIService aiService;

    public Handler(AIService aiService) {
        this.aiService = aiService;
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
        int choice = in.nextInt();
        in.nextLine();
        in.nextLine();
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
        int storage = in.nextInt();
        in.nextLine();
        System.out.print("Describe any damage (or \"none\"): ");
        String damage = in.nextLine().trim();
        Phone phone = new Phone(model, storage, damage, 0.0);
        String result = aiService.evaluate(phone); 
        System.out.println("\nEvaluation: " + result);
        System.out.printf("Estimated price: $%.2f%n", phone.getEstimatedPrice());

        phoneList.addPhone(phone);
        System.out.println("Phone added to the database.");
        in.nextLine();
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    private void searchByModel() {
        System.out.print("Please enter the Model Name (EX: 'iPhone' or 'iPhone 15 Pro')\nModel to search: ");
        String model = in.nextLine().trim();
        printResults(phoneList.searchByModel(model));
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    private void searchByPrice() {
        System.out.print("Please enter the minimum price (EX: 100.00 or 100)\nMin price ($): ");
        double min = in.nextDouble();
        in.nextLine();
        System.out.print("Please enter the maximum price (EX: 100.00 or 100)\nMax price ($): ");
        double max = in.nextDouble();
        in.nextLine();
        printResults(phoneList.searchByPrice(min, max));
        System.out.println("Press Enter to Return to Main Menu");
        in.nextLine();
    }

    public void viewAll() {
        printResults(phoneList.getAllPhones());
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
}
