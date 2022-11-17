import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class TransactionUI extends JDialog implements ActionListener {
    JTable dataTable;
    ArrayList<JSpinner> spinners;
    ArrayList<Integer> ids;
    HashMap<Integer, Integer> products;      // product_id : quantity
    public TransactionUI(JTable _dataTable, String title) {
        super(null, title, ModalityType.DOCUMENT_MODAL);   // set modality so the main thread in InventorySystem that calls this constructor waits until this dialog gets disposed
        
        spinners = new ArrayList<JSpinner>();
        ids = new ArrayList<Integer>();
        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setLayout(new BoxLayout(root, BoxLayout.PAGE_AXIS));
        JButton submitButton = new JButton("Submit");
        submitButton.setActionCommand("submit");
        submitButton.addActionListener(this);
        dataTable = _dataTable;

        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridLayout(dataTable.getRowCount(), 2, 5, 5));
        for (int i = 0; i < dataTable.getRowCount(); i++) {
            String name = dataTable.getModel().getValueAt(i, 1).toString();
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
            subPanel.add(spinner);
            subPanel.add(new JLabel(name));
            spinners.add(spinner);
            ids.add(Integer.parseInt(dataTable.getModel().getValueAt(i, 0).toString()));
        }

        root.add(new JScrollPane(subPanel));
        root.add(submitButton);

        this.add(root);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.pack();
        this.setSize(300, 200);
        this.setLocation(500, 300);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("submit")) {
            products = new HashMap<Integer, Integer>();
            for (int i = 0; i < dataTable.getRowCount(); i++)
                products.put(ids.get(i), (int)spinners.get(i).getValue());
            this.dispose();
        }
    }
}