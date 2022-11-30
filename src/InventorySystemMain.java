import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import org.jasypt.util.text.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class InventorySystemMain {
    static MainUI ui;
    static JFileChooser fileDialog;
    static ProgramState state;
    public static void main(String[] args) {
        state = new ProgramState(); // load previous state

        // password check, commented out for now because testing easier
        PasswordUI passwordDialog = new PasswordUI(state.password, false);   // thread will wait until passwordDialog is disposed before continuing because of modality built into PasswordUI
        if (!passwordDialog.verified)
            return;
        
        ui = new MainUI(state);
        ui.updateRevenue(state.revenue);
        ui.log("Loaded: " + state.db.filepath);
        
        // set up our JFileChooser for loading and saving CSVs
        fileDialog = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileDialog.setFileFilter(new FileFilter() {
            public boolean accept(File file) {      // show all directories and *.csv files in the file picker
                if (file.getName().endsWith(".csv") || file.isDirectory())
                    return true;
                return false;
            }
            public String getDescription() {        // show the description for csv files
                return "Comma separated value file (*.csv)";
            }
        });
    }

    // methods called by the listener in MainUI
    public static void load() {
        if (fileDialog.showOpenDialog(ui) == JFileChooser.APPROVE_OPTION) {
            String path = fileDialog.getSelectedFile().getAbsolutePath();
            state.db.filepath = path;
            state.db.products = Database.loadCSV(path);
            ui.updateRows(state.db);
            ui.log("Loaded: " + path);
        }
    }
    public static void save() {
        if (fileDialog.showSaveDialog(ui) == JFileChooser.APPROVE_OPTION) {
            String path = fileDialog.getSelectedFile().getAbsolutePath();
            path = path.endsWith(".csv") ? path : path + ".csv";
            Database.saveCSV(path, state.db.products);
            ui.log("Saved current table contents to: " + path);
        }
    }
    public static void add() {
        ProductUI dialog = new ProductUI("Add Product", null);
        Product product = dialog.product;
        if (product != null) {
            if (Database.getProductById(product.getId(), state.db.products).size() > 0 || product.getId() < 1)
                ui.log("Id already in use or Id negative, product add failed.");
            else {
                state.db.addProduct(product);
                ui.updateRows(state.db);
                ui.log("Added product '" + product.getName() + "' successfully.");
            }
        }
        else
            ui.log("Add canceled by user.");
    }
    public static void edit() {
        int id = ui.getSelectedRow();   // will return -1 if no row selected
        if (id > 0) {       // check if a row is selected
            Product original = Database.getProductById(id, state.db.products).get(0);
            ProductUI dialog = new ProductUI("Edit Product", original);
            if (dialog.product != null) {   // check if input was validated and a valid product was returned
                if (dialog.product.getId() == original.getId()) {}  // empty if to allow the same id to be used in an edit
                else if (Database.getProductById(dialog.product.getId(), state.db.products).size() > 0 || dialog.product.getId() < 1) {
                    ui.log("Id already in use or Id negative, product edit failed.");
                    return;
                }
                state.db.removeProduct(original);
                state.db.addProduct(dialog.product);
                ui.updateRows(state.db);
                ui.log("Edit succeeded.");
            }
            else
                ui.log("Edit canceled by user.");
        }
        else
            ui.log("No row selected, so product editing failed.");
    }
    public static void order() {
        TransactionUI dialog = new TransactionUI(ui.dataTable, "Order more stock");
        if (dialog.products != null) {
            for (int id : dialog.products.keySet()) {     // add an order for each item in dialog.products
                int quantity = dialog.products.get(id);
                if (quantity > 0) {     // decrease revenue and place the order
                    state.revenue -= Database.getProductById(id, state.db.products).get(0).getBuyPrice() * quantity;
                    state.orders.add(new Order(Database.getProductById(id, state.db.products).get(0), quantity, state.date));
                }
            }
            ui.updateRevenue(state.revenue);
            ui.log("Placed order(s).");
        }
        else
            ui.log("Order canceled by user.");
    }
    public static void transaction() {
        TransactionUI dialog = new TransactionUI(ui.dataTable, "Fake a Transaction");
        if (dialog.products != null) {
            for (int id : dialog.products.keySet()) {
                int quantity = dialog.products.get(id);
                if (quantity > 0) {     // increase revenue and decrease the inventory
                    Product p = Database.getProductById(id, state.db.products).get(0);
                    state.revenue += p.getSellPrice() * quantity;
                    p.setCurrentStock(p.getCurrentStock() - quantity);
                }
            }
            ui.updateRows(state.db);
            ui.updateRevenue(state.revenue);
            ui.log("Customer transaction occurred.");
            InventoryAnalysis.CheckStock(state.db.products, ui);
        }
        else
            ui.log("Transaction canceled by user.");
    }
    public static void setTime() {
        SetTimeUI dialog = new SetTimeUI();
        if (dialog.timeSpan != null) {
            state.date = state.date.plus(dialog.timeSpan);
            ui.log("Increased date to (Y/M/D): " + state.date.toString());

            // check for if any orders have arrived
            for (Order order : state.orders) {
                if (order.hasArrived(state.date)) {
                    order.receive(state.db, ui);
                }
            }
        }
        else
            ui.log("Set time operation canceled by user.");
    }
    public static void changePassword() {
        PasswordUI dialog = new PasswordUI("", true);
        if (!dialog.password.equals("")) {
            state.password = dialog.password;
            ui.log("Password was changed by user.");
        }
        else
            ui.log("Password change operation was canceled by user.");
    }
    public static void cloudLoad() {
        state.db.products = Database.loadDB("dummy_data");
        ui.updateRows(state.db);
        ui.log("Successfully loaded data from the cloud.");
    }
    public static void cloudSave() {
        Database.saveDB("dummy_data", state.db.products);
        ui.log("Successfully backed up to the cloud.");
    }

    // method to view google images for a product based on selected row in the UI
    // zenserp api: https://app.zenserp.com/ (50 free api requests / month)
    // json library: https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple
    public static void viewProduct() {
        int id = ui.getSelectedRow();
        if (id > 0) {       // check if a row is selected
            try {
                ArrayList<byte[]> bytes = new ArrayList<byte[]>();
                String productName = Database.getProductById(id, state.db.products).get(0).getName();

                // if cache dir doesn't exist, create it
                if (!new File(".cache").exists())
                    new File(".cache").mkdir();
                
                // if a product's images exist in cache, use those instead of sending a query and downloading images
                // this is more efficient speed-wise and saves my limited 50/month API requests
                int cacheCount = new File(".cache\\").listFiles((d, name) -> name.contains(productName + "_")).length;
                if (cacheCount > 0) {
                    for (int i = 9; i > 9 - cacheCount; i--) {
                        String path = ".cache\\" + productName + "_" + i + ".jpg";
                        FileInputStream stream = new FileInputStream(path);
                        bytes.add(stream.readAllBytes());
                        stream.close();
                    }
                }
                else {  // send an api request and download the images for caching
                    // set up and do the API call
                    String apiKey = "aa9c8880-6f9a-11ed-b49e-393dfed7d82c";
                    URL server = new URL("https://app.zenserp.com/api/v2/search?apikey=" + apiKey + "&q=" + productName + "&tbm=isch");
                    HttpURLConnection httpURLConnection = (HttpURLConnection)server.openConnection();
                    httpURLConnection.connect();
                    String readLine = null;
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {    // API call was good
                        // read in the response data from the API
                        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        StringBuffer response = new StringBuffer();
                        while ((readLine = in.readLine()) != null)
                            response.append(readLine);
                        in.close();

                        // parse out the urls from the JSON returned from the API
                        JSONObject json = (JSONObject)(new JSONParser().parse(response.toString()));
                        JSONArray arr = (JSONArray)json.get("image_results");
                        int total = arr.size() > 10 ? 10 : arr.size();
                        for (int i = 0; i < total; i++) {     // get a maximum of 10 images
                            JSONObject imgJson = (JSONObject)arr.get(i);
                            String url = imgJson.get("sourceUrl").toString();
                            if (url.endsWith(".jpg")) { // only download jpg images
                                byte[] imgBytes = new URL(url).openStream().readAllBytes(); // get the raw image bytes
                                // save the image
                                FileOutputStream fileStream = new FileOutputStream(".cache\\" + productName + "_" + (total - i - 1) + ".jpg");
                                fileStream.write(imgBytes);
                                fileStream.close();

                                // add image to bytes object
                                bytes.add(imgBytes);
                            }
                            else    // we still want 10 images if possible so increase the loop counter
                                total++;
                        }
                    }
                    else    // we had an issue with the API call
                        throw new Exception("Error in API Call");
                }
                new ImageUI(bytes); // show the dialogue with the images
            } catch (Exception ex) {
                // print the exception to the UI's console
                ex.printStackTrace();
                ui.log(ex.toString());
            }
        }
        else
            ui.log("No row selected, so product image viewing failed.");
    }

    // generalized encrypt and decrypt methods utilizing the open source library found here:
    // https://github.com/jasypt/jasypt
    public static void encrypt(String filepath) {
        try {
            File file = new File(filepath);
            AES256TextEncryptor encryptor = new AES256TextEncryptor();
            encryptor.setPassword(Integer.toString(file.getAbsolutePath().length()));   // init password to length of path so it's different for each file we encrypt

            String text = "";
            try (BufferedReader stream = new BufferedReader(new FileReader(file))) {   // read in text from file
                String current = "";
                while ((current = stream.readLine()) != null)
                    text += "\n" + current;
            }
            text = encryptor.encrypt(text.substring(1, text.length()));      // remove opening '\n' and encrypt
            try (BufferedWriter stream = new BufferedWriter(new FileWriter(file))) {       // write encrypted text to file
                stream.write(text);
            }
        } catch (Exception ex) { ex.printStackTrace(); }    // catch IO exceptions
    }
    public static void decrypt(String filepath) {
        try {
            File file = new File(filepath);
            AES256TextEncryptor encryptor = new AES256TextEncryptor();
            encryptor.setPassword(Integer.toString(file.getAbsolutePath().length()));   // password = length of filepath

            String text = "";
            try (BufferedReader stream = new BufferedReader(new FileReader(file))) {   // read in text from file
                text = stream.readLine();
            }
            text = encryptor.decrypt(text);      // decrypt text
            try (BufferedWriter stream = new BufferedWriter(new FileWriter(file))) {       // write decrypted text to file
                stream.write(text);
            }
        } catch (Exception ex) { ex.printStackTrace(); }    // catch IO exceptions
    }
}