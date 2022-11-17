import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Random;

public class InventorySystemTest {
    String statePath = "InventorySystem.state";

    @Test
    public void DatabaseSaveLoadTest() {    // tests the database object's ability to edit products as well as save/load itself to a new csv file
        String testpath = "db_save_test.csv";
        Database db = new Database();       // start with no data
        
        // add and remove some products
        db.addProduct(new Product(1, "test product", 3, 4.99, 2.79, 7));
        db.addProduct(new Product(2, "test product 2", 3, 4.99, 2.79, 3));
        db.addProduct(new Product(3, "test product 3", 3, 4.99, 2.79, 2));
        db.removeProduct(Database.getProductById(1, db.products).get(0));

        Database.saveCSV(testpath, db.products);    // save the new database to a new file

        Database testdb = new Database(testpath);   // init a new database from that file
        assertEquals(db.toString(), testdb.toString()); // check if all products are identical (Database has an overrided toString method)
        assertEquals(db.products.size(), 2);    // checks if the removeProduct method worked
    }

    @Test
    public void SaveStateTest() throws IOException {   // tests the savestate functionality
        if (new File(statePath).exists())
            Files.delete(Path.of(statePath)); // delete any currently existing state

        ProgramState state = new ProgramState();    // create default programstate

        // change some values and add some orders
        state.db.addProduct(new Product(1, "test product", 3, 4.99, 2.79, 7));
        state.db.addProduct(new Product(2, "test product 2", 3, 4.99, 2.79, 3));
        state.db.addProduct(new Product(3, "test product 3", 3, 4.99, 2.79, 2));
        Database.saveCSV("dummy_data.csv", state.db.products);
        state.password = "new super strong password";
        state.date = state.date.plus(Period.ofDays(3));
        state.revenue += new Random().nextDouble() * 1000;
        state.orders.add(new Order(Database.getProductById(1, state.db.products).get(0), 5, state.date));
        state.orders.add(new Order(Database.getProductById(2, state.db.products).get(0), 3, state.date));
        state.orders.add(new Order(Database.getProductById(3, state.db.products).get(0), 7, state.date));

        state.save();       // save and encrypt the current state of the program

        ProgramState test = new ProgramState();     // if state saved properly, the test should now have loaded the state and they should be identical
        assertEquals(state.date, test.date);
        assertEquals(state.db.toString(), test.db.toString());
        assertEquals(state.revenue, test.revenue, 0.0);
        assertEquals(state.password, test.password);

        ArrayList<String> orderTest = getStringOrders(state);   // get a string array of the orders contained in state
        int totalOrders = 0;
        for (Order i : test.orders) {   // for each order in test, check it's contained in state
            assertTrue(orderTest.contains(i.toString()));
            totalOrders++;
        }
        assertEquals(orderTest.size(), totalOrders);    // check the number of orders is the same
    }

    @Test
    public void EncryptionTest() throws IOException {  // tests that the encryption/decryption works properly
        if (new File(statePath).exists())
            Files.delete(Path.of(statePath)); // delete any currently existing state

        ProgramState state = new ProgramState();
        state.save();   // saves and encrypts the file

        InventorySystemMain.decrypt(statePath);     // decrypt the file
        // manually read in the file and check each value against 'state'
        BufferedReader stream = new BufferedReader(new FileReader(statePath));
        assertEquals(state.date, LocalDate.parse(stream.readLine()));
        assertEquals(state.db.toString(), new Database(stream.readLine()).toString());
        assertEquals(state.revenue, Double.parseDouble(stream.readLine()), 0.0);
        assertEquals(state.password, stream.readLine());

        ArrayList<String> orderTest = getStringOrders(state);
        int totalOrders = 0;
        String current;
        while ((current = stream.readLine()) != null) {     // read in a line for each order
            String[] args = current.split(",");     // split the line on commas
            Order order = new Order(Database.getProductById(Integer.parseInt(args[0]), state.db.products).get(0), Integer.parseInt(args[1]), LocalDate.parse(args[2]));
            assertTrue(orderTest.contains(order.toString()));   // check that the array of order strings contains the order we've read in
            totalOrders++;
        }
        assertEquals(orderTest.size(), totalOrders);    // check the number of orders is the same
        stream.close();
    }

    public ArrayList<String> getStringOrders(ProgramState state) {
        ArrayList<String> orderTest = new ArrayList<String>();
        for (Order i : state.orders)  // state.save() only saves active orders, so add all active orders to orderTest
            if (i.isActive)
                orderTest.add(i.toString());
        return orderTest;
    }
}