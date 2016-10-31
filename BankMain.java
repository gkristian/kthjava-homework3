package homework3;

import static homework3.MarketMain.connection;
import static homework3.MarketMain.stmt;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class BankMain implements BankIntFace {

    //DATABASE THINGS
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String DB_URL = "jdbc:derby://localhost:1527/homework3_db;create=true";
    static final String USER = "qwerty";
    static final String PASS = "qwerty";
    static Connection connection;
    static Statement stmt = null;

    public static final String USER_TABLE_NAME = "TABLE_USER";
    public static final String ITEM_TABLE_NAME = "TABLE_ITEM";
    public static final String WISH_TABLE_NAME = "TABLE_WISH";

    static List<String> accountlist = new ArrayList<String>();
    // ---accountID#name#balance---
    Random randomGenerator = new Random();

    private BankMain() {
    }

    // REMOTE METHODS ===================================
    /*
	public String CreateAccount(String name) {
		int accountId = randomGenerator.nextInt(100);
		// Start with zero balance
		String newaccount = Integer.toString(accountId) + "#" + name + "#0";
		System.out.println("New bank account : " + newaccount);
		accountlist.add(newaccount); // 76#greg#0
		return "New account created. Name : " + name + ". User ID : " + accountId;

	}
     */
    public int getBalance(String name) {
        int output = 0;
        // Loop, search through item list
        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_USER";
            ResultSet rsb1;
            rsb1 = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while (rsb1.next()) {
                //Retrieve by column name                
                String username = rsb1.getString("user_name");
                int userbalance = rsb1.getInt("user_balance");
                if (name.equalsIgnoreCase(username)) {
                    output = userbalance;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    public String Deposit(String name, int depositamount) {
        System.out.println("Deposit in progress.");
        String output = "Deposit failed";

        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_USER";
            ResultSet rsb2;
            rsb2 = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while (rsb2.next()) {
                //Retrieve by column name                
                String username = rsb2.getString("user_name");
                int userbalance = rsb2.getInt("user_balance");
                if (name.equalsIgnoreCase(username)) {
                    System.out.println("Deposit : find name matching");
                    //Add new amount and update into list
                    int newbalance = userbalance + depositamount;
                    //DB UPDATE                   
                    String sql2 = "UPDATE table_user SET user_balance=" + newbalance + " WHERE user_name='" + name + "'";
                    stmt.executeUpdate(sql2);

                    output = "Deposit success. New balance : " + newbalance + " SEK";
                    System.out.println(output);
                    Sleep(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return output;
    }

    public String Withdraw(String name, int withdrawamount) {
        System.out.println("Withdraw in progress.");
        String output = "Withdraw failed. Unknown error.";
        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_USER WHERE user_name='"+name+"'";
            ResultSet rsb3;
            rsb3 = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while (rsb3.next()) {
                //Retrieve by column name                
                String username = rsb3.getString("user_name");
                int userbalance = rsb3.getInt("user_balance");
                if (name.equalsIgnoreCase(username)) {
                    System.out.println("Withdraw : find name matching");
                    //Add new amount and update into list
                    int newbalance = userbalance - withdrawamount;
                    //DB UPDATE                   
                    String sql2 = "UPDATE table_user SET user_balance=" + newbalance + " WHERE user_name='" + name + "'";
                    stmt.executeUpdate(sql2);

                    output = "Withdraw success. New balance : " + newbalance + " SEK";
                    System.out.println(output);

                } else if (userbalance <= withdrawamount) {
                    output = "Not enough account balance.";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    
    return output ;
}

// MAIN ===================================
public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            // ======== RMI INITIALIZATION START =============================
            // Obtains the list from registry
            Registry registry = LocateRegistry.getRegistry(host);

            // STEP 2A
            // Lookup and obtain stub for MARKET 
            MarketIntFace stubMarket = (MarketIntFace) registry.lookup("stubM");

            // STEP 2B
            // Export BANK remote object and bind to registry
            BankMain regobj = new BankMain();
            BankIntFace stubBank = (BankIntFace) UnicastRemoteObject.exportObject(regobj, 0);
            registry.bind("stubB", stubBank);

            // Ask market to do step 3
            // Call BankConnect to notify server that Bank Server has started
            stubMarket.BankConnect();

            // ======== RMI INITIALIZATION END =============================
            System.out.println("Bank server started");
        } catch (Exception e) {
            System.err.println("\nCLIENT : Error all\n");
            e.printStackTrace();
        }
        //CONNECTION DB
        try {
            //STEP 2: Register JDBC driver
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = connection.createStatement();
            System.out.println("Database connected.");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    private static void Sleep(int time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
