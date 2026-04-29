package main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        PhoneList phoneList = new PhoneList();
        ReferencePhoneLoader.loadInto(phoneList);
        PhoneAppService phoneAppService = new PhoneAppService(
            phoneList,
            new OpenRouterAIService(new RAGService())
        );
        Handler handler = new Handler(in, phoneAppService);
        int choice = -1;
        while (choice != 0) {
            handler.printChoices();
            String input = in.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Invalid option, please try again.");
                continue;
            }
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid option, please try again.");
                continue;
            }
            if (choice == 1) {
                handler.evaluatePhone();
            } else if (choice == 2) {
                handler.searchMenu();
            } else if (choice == 3) {
                handler.viewAll();
            } else if (choice == 0) {
                System.out.println("Goodbye.");
            } else {
                System.out.println("Invalid option, please try again.");
            }
        }
    }
}
