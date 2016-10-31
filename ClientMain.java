package homework3;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class ClientMain implements ClientIntFace {

    private static Boolean isRunning = true;
    private static Boolean userNotValidated = true;

    private static String inputusername;
    private static String activeuser;

    private ClientMain() {
    }

    // REMOTE METHODS ===================================
    public void CallbackWish(String wishmsg) {
        JOptionPane.showMessageDialog(null, wishmsg, "Wishlist Notification for " + activeuser,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void CallbackSell(String sellmsg) {
        JOptionPane.showMessageDialog(null, sellmsg, "Notification for " + activeuser, JOptionPane.INFORMATION_MESSAGE);
    }

    // MAIN ===================================
    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            // ======== RMI INITIALIZATION START =============================
            // Obtains the list from registry
            Registry registry = LocateRegistry.getRegistry(host);

            // STEP 4a
            // Lookup and obtain stub for MARKET and BANK
            MarketIntFace stubMarket = (MarketIntFace) registry.lookup("stubM");
            BankIntFace stubBank = (BankIntFace) registry.lookup("stubB");

            // STEP 5
            // Export client interface
            ClientMain regobj = new ClientMain();
            ClientIntFace stubClient = (ClientIntFace) UnicastRemoteObject.exportObject(regobj, 0);
            // ======== RMI INITIALIZATION END =============================

            Scanner reader = new Scanner(System.in);

            // Main program loop
            while (true) {

                // Login loop
                while (userNotValidated) {
                    System.out.println("\n\n====================================");
                    System.out.println("Networked Marketplace : Client Login");
                    System.out.println("====================================");
                    System.out.println("Option : \n" + "1. Login\n" + "2. Register");

                    // Asking for valid option
                    Boolean inputNotValid = true;
                    String option = null;
                    while (inputNotValid) {
                        System.out.print("Your option : ");
                        option = reader.next();
                        if (!(option.equalsIgnoreCase("1") || option.equalsIgnoreCase("2"))) {
                            System.out.print("Input not valid, try again. ");
                        } else {
                            inputNotValid = false;
                        }
                    }
                    int optionInt = Integer.parseInt(option);

                    // Switch case based on input
                    switch (optionInt) {

                        //LOGIN
                        case 1:
                            System.out.print("LOGIN \nEnter your username : ");
                            inputusername = reader.next();
                            if (stubMarket.userValidated(inputusername)) {                                                                                               
                                try{
                                    registry.bind(inputusername, stubClient);
                                } catch (RemoteException e) {
                                    System.err.println("Binding error");
                                }
                                
                                userNotValidated = false;
                                activeuser = inputusername;
                                String reply = stubMarket.SendObjectReference(activeuser);
                                System.out.println(reply);
                            } else {
                                System.out.println("\nUsername not found.");
                            }
                            break;

                        //REGISTER NEW
                        case 2:
                            System.out.print("\nREGISTER NEW CLIENT \nEnter your username : ");
                            inputusername = reader.next();
                            if (!(stubMarket.userValidated(inputusername))) {

                                // IMPORTANT +++++++++++++++++++++++++++
                                registry.bind(inputusername, stubClient);
                                // IMPORTANT +++++++++++++++++++++++++++							

                                stubMarket.Register(inputusername);
                                System.out.println("You are registered. Binded to : "+inputusername);
                                System.out.println("IMPORTANT! You must create bank account before buy/sell things.");
                                userNotValidated = false;
                                activeuser = inputusername;
                            } else {
                                System.out.println("Username already exist. Try again.");
                            }

                            break;
                        default:
                            System.out.print("\nEnter a valid option!\n\n ");
                            break;
                    }
                }
                while (isRunning) {
                    System.out.println("\n\n====================================");
                    System.out.println("Welcome, " + activeuser);
                    System.out.println("====================================");
                    System.out.println("What do you want to do? \n"
                            + "1. Show All Item \t6. Check Balance\n"
                            + "2. Buy \t\t        7. Deposit Money\n"
                            + "3. Sell \t         8. Withdraw Money\n"
                            + "4. Wish something \t9. Exit program\n"
                            + "5. Unregister\n");

                    // Asking for option and validate the input					
                    Boolean inputNotValid2 = true;
                    String option2 = null;
                    while (inputNotValid2) {
                        System.out.print("Your option : ");
                        option2 = reader.next();
                        if (!(option2.equalsIgnoreCase("1")
                                || option2.equalsIgnoreCase("2")
                                || option2.equalsIgnoreCase("3")
                                || option2.equalsIgnoreCase("4")
                                || option2.equalsIgnoreCase("5")
                                || option2.equalsIgnoreCase("6")
                                || option2.equalsIgnoreCase("7")
                                || option2.equalsIgnoreCase("8")
                                || option2.equalsIgnoreCase("9")
                                || option2.equalsIgnoreCase("10"))) {
                            System.out.print("Input not valid, try again. ");
                        } else {
                            inputNotValid2 = false;
                        }
                    }
                    int option2Int = Integer.parseInt(option2);

                    //BEGIN SWITCH CASE FOR OPTIONS
                    switch (option2Int) {
                        // SHOW ALL
                        case 1:
                            String showall = stubMarket.ShowAll();
                            System.out.print(showall);
                            break;

                        // BUY
                        case 2:
                            // Show all available item first
                            String showall2 = stubMarket.ShowAll();
                            System.out.print(showall2);
                            System.out.print("----------------------------------\nBUY ITEM\n");
                            System.out.print("Type item ID to buy : ");
                            String itemID = reader.next();
                            // Call remote method Buy
                            String reply = stubMarket.Buy(itemID, activeuser);
                            // Show result to buyer
                            System.out.println(reply);
                            break;

                        // SELL
                        case 3:
                            System.out.print("\nSELL NEW ITEM");
                            System.out.print("\nItem name  : ");
                            String sellname = reader.next();
                            System.out.print("Item price : ");
                            String sellprice = reader.next();
                            int sellpriceInt = Integer.parseInt(sellprice);
                            stubMarket.Sell(sellname, sellpriceInt, activeuser);
                            break;

                        // WISH
                        case 4:
                            System.out.print("\nWISH NEW ITEM");
                            System.out.print("\nItem name  : ");
                            String wishname = reader.next();
                            System.out.print("Item price (maximal) : ");
                            String wishprice = reader.next();
                            int wishpriceInt = Integer.parseInt(wishprice);
                            stubMarket.Wish(wishname, wishpriceInt, activeuser);
                            break;

                        // UNREGISTER
                        case 5:
                            System.out.print("!!!UNREGISTER!!! Enter your name : ");
                            String name2 = reader.next();
                            stubMarket.Unregister(name2);
                            break;

                        //CHECK BALANCE
                        case 6:
                            int balance = stubBank.getBalance(activeuser);
                            System.out.print("Your balance now : " + balance + " SEK");
                            System.out.print("\nPress enter to continue.");
                            System.in.read();
                            break;

                        //DEPOSIT
                        case 7:
                            System.out.print("Deposit amount : ");
                            String amount = reader.next();
                            int amountInt = Integer.parseInt(amount);
                            String replydep = stubBank.Deposit(activeuser, amountInt);
                            System.out.print("\n" + replydep);
                            System.out.print(" Press enter to continue.");
                            System.in.read();
                            break;

                        //WITHDRAW
                        case 8:
                            System.out.print("Withdraw amount : ");
                            String amountW = reader.next();
                            int amountWInt = Integer.parseInt(amountW);
                            String replywith = stubBank.Withdraw(activeuser, amountWInt);
                            System.out.print("\n" + replywith);
                            System.out.print(" Press enter to continue.");
                            System.in.read();
                            break;

                        //EXIT
                        case 9:
                            System.exit(0);
                            break;

                        default:
                            System.out.print("\nEnter a valid option!\n\n ");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("\nCLIENT : Error all\n");
            e.printStackTrace();
        }
    }

    private static void ClearScreen() {
        System.out.println(
                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

    }
}
