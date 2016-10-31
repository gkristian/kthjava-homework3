package homework3;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

public class MarketMain implements MarketIntFace {

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

    public MarketMain() throws RemoteException {
        // super();
    }

    // private Vector<Customer> customerList=new Vector<Customer>();
    // private Vector<Item> ItemList=new Vector<Item>();
    // private Vector<Item> WishList=new Vector<Item>();
    private String b = null;
    String[] AfterSplit;
    static List<String> userlist = new ArrayList<String>(); // ---userid#gregorius---
    static List<String> itemlist = new ArrayList<String>(); // ---indexitem#kamera#3000#userid---
    static List<String> wishlist = new ArrayList<String>(); // ---indexwish#laptop#2000#userid---

    private static ClientIntFace stubClient0;
    private static ClientIntFace stubClient1;
    private static ClientIntFace stubClient2;
    private static ClientIntFace stubClient3;
    private static BankIntFace stubBank;
    private static Registry registry;

    private PreparedStatement getRowCountStatement;
    private PreparedStatement RegisterStatement;

    // DATABASE ===============================================
    private void prepareStatements(Connection connection) throws SQLException {

        getRowCountStatement = connection.prepareStatement("SELECT COUNT(user_id) FROM " + USER_TABLE_NAME);
        RegisterStatement = connection.prepareStatement("INSERT INTO " + USER_TABLE_NAME + " VALUES (?,?,?,?)");
    }

    // USER REMOTE METHODS ===================================
    public boolean userValidated(String inputusername) { // Input username
        //Return true if user already registered    
        boolean output = false;
        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_USER";
            ResultSet rsm1;
            rsm1 = stmt.executeQuery(sql);
            while (rsm1.next()) {
                //Loop row by row
                String userName = rsm1.getString("user_name");
                if (userName.equalsIgnoreCase(inputusername)) {
                    output = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return output;
    }

    public String SendObjectReference(String name) {
        String output = "Error in creating object reference";
        int userid = getUserId(name);
        System.out.println("Send obj ref " + userid + " " + name);
        try {
            switch (userid) {
                case 0:
                    stubClient0 = (ClientIntFace) registry.lookup(name);
                    output = "Object reference import success. ID : 0";
                    break;
                case 1:
                    stubClient1 = (ClientIntFace) registry.lookup(name);
                    output = "Object reference import success. ID : 1";
                    break;
                case 2:
                    stubClient2 = (ClientIntFace) registry.lookup(name);
                    output = "Object reference import success. ID : 2";
                    break;
                case 3:
                    stubClient3 = (ClientIntFace) registry.lookup(name);
                    output = "Object reference import success. ID : 3";
                    break;
                default:
                    output = "Object reference not imported";
                    System.err.println(output);
                    break;
            }
        } catch (RemoteException | NotBoundException e) {
            output = "Object reference not imported";
            System.err.println(output);
        }
        return output;
    }

    public void Register(String name) {
        System.out.println("Registering user : " + name);
        //int userid = userlist.size(); // add into end of index
        //String newuser = Integer.toString(userid) + "#" + name;
        //userlist.add(newuser); // 0#greg
        int userid = getTableRowCount(USER_TABLE_NAME);
        int initialBalance = 1000;

        try {
            //RegisterStatement.setInt(1, userid);
            //RegisterStatement.setString(2, name);
            //RegisterStatement.setString(3, "password");
            //RegisterStatement.setInt(4, initialBalance);
            //RegisterStatement.executeUpdate();
            connection.setAutoCommit(false);
            String sql = "INSERT INTO table_user VALUES (" + userid + ",'" + name + "','password'," + initialBalance + ")";
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // REGISTER STATEMENT
        Sleep(1);
        System.out.println("User registered to list :" + name);
        // Create stub name dynamically. MAX 4 USERS :(((((
        try {
            switch (userid) {
                case 0:
                    stubClient0 = (ClientIntFace) registry.lookup(name);
                    break;
                case 1:
                    stubClient1 = (ClientIntFace) registry.lookup(name);
                    break;
                case 2:
                    stubClient2 = (ClientIntFace) registry.lookup(name);
                    break;
                case 3:
                    stubClient3 = (ClientIntFace) registry.lookup(name);
                    break;
                default:
                    System.err.println("Failed creating stub");
                    break;
            }
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Failed creating stub");
        }

        System.out.println("User stub :" + name + ": imported");
    }

    public void Unregister(String name) {
        try {
            connection.setAutoCommit(false);
            String sql = "DELETE FROM table_user WHERE user_name = " + name;
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(name + " deleted");
    }

    public String ShowAll() {
        String output = "-------------------------------------\n" + "ID   ITEM\tPRICE(SEK)   SELLER \n"
                + "-------------------------------------\n";
        //String temp = null;
        //String[] splitted;
        //for (int i = 0; i < itemlist.size(); i++) {
        //    temp = itemlist.get(i);
        //    splitted = temp.split("#");
        //    output = output + splitted[0] + "    " + splitted[1] + "\t" + splitted[2] + "\t     " + splitted[3] + "\n";
        //}

        //STEP 4: Execute a query
        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_ITEM";
            ResultSet rsm2;
            rsm2 = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while (rsm2.next()) {
                //Retrieve by column name
                int itemId = rsm2.getInt("item_id");
                String itemName = rsm2.getString("item_name");
                int itemPrice = rsm2.getInt("item_price");
                String itemSeller = rsm2.getString("item_seller");

                output = output + itemId + "    " + itemName + "\t" + itemPrice + "\t     " + itemSeller + "\n";
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return output;
    }

    public String Buy(String buyitemId, String buyer) {
        String output = "Error, purchase failed.";
        String tempitem = null;
        String[] itemsplitted;
        int itemlocation = 1000;

        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_ITEM";
            ResultSet rsm3;
            rsm3 = stmt.executeQuery(sql);
            System.out.println("Buy 1");

            while (rsm3.next()) {
                System.out.println("Buy 2");
                //Retrieve by column name
                int itemId = rsm3.getInt("item_id");
                String itemname = rsm3.getString("item_name");
                int itemprice = rsm3.getInt("item_price");
                String itemseller = rsm3.getString("item_seller");

                if (Integer.toString(itemId).equalsIgnoreCase(buyitemId)) {
                    System.out.println("Buy 3");
                    int currentbalance = 0;
                    try {
                        currentbalance = stubBank.getBalance(buyer);
                    } catch (RemoteException e) {
                        System.err.println("Market : failed getting account balance.");
                    }

                    if (currentbalance >= itemprice) { // Money is enough :D

                        // WITHDRAW MONEY
                        try {
                            String replywith = stubBank.Withdraw(buyer, itemprice);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        // Notification for buyer
                        output = "Succesfully bought " + itemname + " for " + itemprice + " SEK";
                        System.out.println(output);

                        // Notification for seller
                        int sellerId = getUserId(itemseller);
                        System.out.println("Buy 3 " + sellerId + " " + itemseller);
                        SendCallbackSell(itemname, sellerId);
                        // Remove item from item list
                        String sql2 = "DELETE FROM table_item WHERE item_id = " + buyitemId;
                        stmt.executeUpdate(sql2);

                        // Deposit money to seller
                        //String sellername = getUserName(sellerIdInt);
                        try {
                            String replydep = stubBank.Deposit(itemseller, itemprice);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        // Do re-sorting of item ID
                    } else if (currentbalance < itemprice) {
                        output = "Money not enough";
                    }
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        } /*finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/

        //SortTableItem();
        // Check if it finds matching
        return output; // Send notification for buyer
    }

    public void Sell(String item, int price, String seller) {
        // Print status
        System.out.println("New selling item : " + item + " " + price + "SEK");

        // Get item index and seller's ID
        int index = itemlist.size();
        int sellerId = getUserId(seller);

        // Combine all information. Format : itemID#itemname#price#sellerID
        String input = Integer.toString(index) + "#" + item + "#" + Integer.toString(price) + "#"
                + Integer.toString(sellerId);
        System.out.println("Input:---" + input + "---");

        // Add to item list
        itemlist.add(input);

        // Check if new selling item is on the wish list
        SendCallbackWish();
    }

    public void Wish(String item, int price, String wisher) {
        // Print status
        System.out.println("New wished item : " + item + " with max price " + price + "SEK");

        // Get wish index and wisher's ID        
        int indexwish = getTableRowCount(WISH_TABLE_NAME);
        int wisherId = getUserId(wisher);

        try {
            connection.setAutoCommit(false);
            String sql = "INSERT INTO table_wish VALUES (" + indexwish + ",'" + item + "'," + price + ",'" + wisher + "')";
            stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // BANK REMOTE METHODS ===================================
    // STEP 3
    public void BankConnect() {
        try {
            stubBank = (BankIntFace) registry.lookup("stubB");
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Failed lookup bank's stub.");
        }
    }

    // ======================= MAIN ============================================
    public static void main(String args[]) {
        try {
            // Create object
            MarketMain regobj = new MarketMain();
            System.out.println("1");

            // STEP 1
            // Export remote object
            MarketIntFace stubMarket = (MarketIntFace) UnicastRemoteObject.exportObject(regobj, 0);
            System.out.println("2");

            // Bind the remote object's stub in the registry
            //registry = LocateRegistry.getRegistry();
            registry = LocateRegistry.createRegistry(1099);
            System.out.println("3");
            registry.bind("stubM", stubMarket);
            System.out.println("4");
            System.out.println("Server started.");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
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
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        System.out.println("Database connected");
    }

    // ======================= LOCAL METHODS ================================
    // Check wishlist and send callback if it find a matching wish listh
    private static void SendCallbackWish() {
        String output = "Item not found.";
        String tempitem = null;
        String tempwish = null;
        String[] itemsplitted;
        String[] wishsplitted;

        for (int i = 0; i < wishlist.size(); i++) {
            // wishlist : kamera#5000
            tempwish = wishlist.get(i);
            wishsplitted = tempwish.split("#");
            String wishname = wishsplitted[1];
            int wishpriceInt = Integer.parseInt(wishsplitted[2]);
            String wisherID = wishsplitted[3];

            for (int j = 0; j < itemlist.size(); j++) {
                // For each wish, find through the item list
                tempitem = itemlist.get(j);
                itemsplitted = tempitem.split("#");
                String itemname = itemsplitted[1];
                int itempriceInt = Integer.parseInt(itemsplitted[2]);
                String sellerID = itemsplitted[3];
                if (itemname.equalsIgnoreCase(wishname) && itempriceInt <= wishpriceInt) {
                    // DO CALLBACK FOR WISH
                    String wishmsg = wishname + " is now available for " + itemsplitted[2] + " SEK";
                    try {
                        switch (Integer.parseInt(wisherID)) {
                            // Call callback, depends on the wisher's userID
                            case 0:
                                stubClient0.CallbackWish(wishmsg);
                                break;
                            case 1:
                                stubClient1.CallbackWish(wishmsg);
                                break;
                            case 2:
                                stubClient2.CallbackWish(wishmsg);
                                break;
                            case 3:
                                stubClient3.CallbackWish(wishmsg);
                                break;
                            default:
                                System.err.println("MARKET : Failed creating callback for wish");
                                break;
                        }
                    } catch (RemoteException e) {
                        System.err.println("MARKET : Failed creating callback for wish");
                    }
                }
            }
        }

    }

    private static void SendCallbackSell(String itemname, int sellerId) {
        System.out.println("Inside callback sell. Seller id : " + sellerId);
        String sellmsg = itemname + " is sold!";
        try {
            switch (sellerId) {
                // Call callback, depends on the wisher's userID
                case 0:
                    stubClient0.CallbackSell(sellmsg);
                    break;
                case 1:
                    stubClient1.CallbackSell(sellmsg);
                    break;
                case 2:
                    stubClient2.CallbackSell(sellmsg);
                    break;
                case 3:
                    stubClient3.CallbackSell(sellmsg);
                    break;
                default:
                    System.err.println("MARKET : Failed creating callback for sell");
                    break;
            }
        } catch (RemoteException e) {
            System.err.println("MARKET : Failed creating callback for sell");
        }
    }

    private static int getUserId(String usernameinput) {
        // Return UserId of a user name
        int output = 999; // 999 means username not found

        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_USER";
            ResultSet rsm4;
            rsm4 = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while (rsm4.next()) {
                //Retrieve by column name
                int userId = rsm4.getInt("user_id");
                String username = rsm4.getString("user_name");

                if (username.equalsIgnoreCase(usernameinput)) {
                    output = userId;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        return output;
    }

    private static String getUserName(int userIdinput) {
        // Return username of a user ID
        String output = "NOTFOUND";
        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM TABLE_USER";
            ResultSet rsm5;
            rsm5 = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while (rsm5.next()) {
                //Retrieve by column name
                int userId = rsm5.getInt("user_id");
                String username = rsm5.getString("user_name");

                if (userIdinput == userId) {
                    output = username;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    private static void Sleep(int time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int getTableRowCount(String table) {
        int output = 0;
        try {
            connection.setAutoCommit(false);
            Statement s = connection.createStatement();
            ResultSet r;
            r = s.executeQuery("SELECT COUNT(*) AS rowcount FROM " + table);
            r.next();
            output = r.getInt("rowcount");
            r.close();

        } catch (SQLException ex) {
            Logger.getLogger(MarketMain.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    private static void SortTableItem() {
        System.out.println("Inside table sort");
        int i = 0;
        try {
            connection.setAutoCommit(false);
            String sql;
            sql = "SELECT * FROM table_item";
            ResultSet rsm6;
            rsm6 = stmt.executeQuery(sql);

            while (rsm6.next()) {
                System.out.println(i);
                int itemId = rsm6.getInt("item_id");
                String itemname = rsm6.getString("item_name");
                String sql2 = "UPDATE table_item SET item_id=" + i + " WHERE item_name='" + itemname + "'";
                stmt.executeUpdate(sql2);
                i++;
            }
        } catch (SQLException ex) {
            System.err.println("SQL Error");
        }
    }
}
