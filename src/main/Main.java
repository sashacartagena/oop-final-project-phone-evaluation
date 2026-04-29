package main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Handler handler = new Handler(new RAGService());
        int choice = -1;
        while (choice != 0) {
            handler.printChoices();
            choice = in.nextInt();
            in.nextLine();
            in.nextLine();
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
