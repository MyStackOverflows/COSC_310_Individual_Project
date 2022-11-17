import java.io.*;
import java.time.*;
import java.util.*;

public class ProgramState {
    public String path;
    public LocalDate date;
    public Database db;
    public List<Order> orders;
    public double revenue;
    public String password;

    public ProgramState() {
        /* InventorySystem.state layout/format:
         * yyyy-mm-dd           // date
         * c:\path\to\the\csv   // filepath to db instance's csv
         * 1234.56              // revenue double
         * supersecretpassword  // password
         * 1,12,2022-11-06      // ordered 12 products of id=1 on nov 6th 2022 */
        
        path = "InventorySystem.state";
        if (new File(path).exists())
            load();
        else {  // no state found, must be first run. so, load basic defaults
            date = LocalDate.now();
            db = new Database("dummy_data.csv");    // load in default dummy data
            orders = new ArrayList<Order>();
            revenue = 0;
            password = "password";
        }
    }
    
    public void save() {
        // save current state to the file
        try {
            BufferedWriter stream = new BufferedWriter(new FileWriter(path));
            stream.write(date.toString());    // save date
            stream.write("\n" + db.filepath);    // save filepath of the db's csv
            stream.write("\n" + revenue);       // save revenue
            stream.write("\n" + password);           // save password in plaintext, it will be encrypted later
            for (Order i : orders)                      // save a line for each active order
                if (i.isActive)     // if an order isn't active (ie has been received) we don't need to save it
                    stream.write(String.format("\n%d,%d,%s", i.product.getId(), i.quantity, i.date.toString()));
        
            stream.close();     // close the stream
        } catch (IOException ex) { ex.printStackTrace(); }

        // ADD CALLS TO ENCRYPT THE FILE HERE
        InventorySystemMain.encrypt(path);
    }

    public void load() {
        // ADD CALLS TO DECRYPT THE FILE HERE
        InventorySystemMain.decrypt(path);

        // read in the state to this instance
        try {
            BufferedReader stream = new BufferedReader(new FileReader(path));
            date = LocalDate.parse(stream.readLine());
            db = new Database(stream.readLine());
            revenue = Double.parseDouble(stream.readLine());
            password = stream.readLine();
    
            orders = new ArrayList<Order>();
            String current;
            while ((current = stream.readLine()) != null) {     // read in a line for each order
                String[] args = current.split(",");     // split the line on commas
                orders.add(new Order(Database.getProductById(Integer.parseInt(args[0]), db.products).get(0), Integer.parseInt(args[1]), LocalDate.parse(args[2])));
            }
            stream.close();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}