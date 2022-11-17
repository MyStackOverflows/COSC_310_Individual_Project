import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class ProductUI extends JDialog implements ActionListener {
    JSpinner idInput, currentStockInput, sellPriceInput, buyPriceInput, shipTimeDayInput;
    JTextField nameInput;
    Product product;

    public ProductUI(String title, Product _product) {   // change to allow a Product argument to pre-fill the boxes etc
        super(null, title, ModalityType.DOCUMENT_MODAL);   // set modality so the main thread in InventorySystem that calls this constructor waits until this dialog gets disposed
        
        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setLayout(new GridBagLayout());
        JButton submitButton = new JButton("Submit");
        submitButton.setActionCommand("submit");
        submitButton.addActionListener(this);
        idInput = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        nameInput = new JTextField();
        currentStockInput = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
        sellPriceInput = new JSpinner(new SpinnerNumberModel((double)0, (double)0, null, (double)0.01));
        buyPriceInput = new JSpinner(new SpinnerNumberModel((double)0, (double)0, null, (double)0.01));
        shipTimeDayInput = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));

        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridLayout(2, 6, 5, 5));
        Insets dummy = new Insets(0, 0, 0, 0);
        for (String i : new String[] {"id", "name", "currentStock", "sellPrice", "buyPrice", "shipTimeDays"})
            subPanel.add(new JLabel(i));
        for (Component i : new Component[] {idInput, nameInput, currentStockInput, sellPriceInput, buyPriceInput, shipTimeDayInput})
            subPanel.add(i);

        root.add(subPanel, new GridBagConstraints(0, 0, 1, 1, 1, .5, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, dummy, 0, 0));
        root.add(submitButton, new GridBagConstraints(0, 1, 1, 1, 1, .5, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, dummy, 0, 0));

        if (_product != null) {     // if we get a product passed in, pre-load the input box values
            idInput.setValue(_product.getId());
            nameInput.setText(_product.getName());
            currentStockInput.setValue(_product.getCurrentStock());
            sellPriceInput.setValue(_product.getSellPrice());
            buyPriceInput.setValue(_product.getBuyPrice());
            shipTimeDayInput.setValue(_product.getShipTimeDays());
        }

        this.add(root);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.pack();
        this.setSize(750, 200);
        this.setLocation(500, 300);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("submit")) {
            int id = (int)idInput.getValue();
            String name = nameInput.getText();
            int currentStock = (int)currentStockInput.getValue();
            double sellPrice = (double)sellPriceInput.getValue();
            double buyPrice = (double)buyPriceInput.getValue();
            int shipTimeDays = (int)shipTimeDayInput.getValue();

            if (id == 0)
                JOptionPane.showMessageDialog(this, "Product id cannot be 0.");
            else if (name.equals(""))
                JOptionPane.showMessageDialog(this, "Product name cannot be empty.");
            else if (sellPrice == 0 || buyPrice == 0)
                JOptionPane.showMessageDialog(this, "Product prices cannot be 0.");
            else if (shipTimeDays == 0)
                JOptionPane.showMessageDialog(this, "Product ship time cannot be 0.");
            else {  // no errors, so create product and dispose
                this.product = new Product(id, name, currentStock, sellPrice, buyPrice, shipTimeDays);
                this.dispose();
            }
        }
    }
}