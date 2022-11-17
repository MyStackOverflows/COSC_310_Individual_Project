import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.time.Period;

public class SetTimeUI extends JDialog implements ActionListener {
    JSpinner monthSpinner;
    JSpinner daySpinner;
    JSpinner hourSpinner;

    Period timeSpan;

    public SetTimeUI() {
        super(null, "Fast-forward Time", ModalityType.DOCUMENT_MODAL);   // set modality so the main thread in InventorySystem that calls this constructor waits until this dialog gets disposed
        
        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setLayout(new GridBagLayout());
        monthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 12, 1));
        daySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 31, 1));
        JButton submitButton = new JButton("Submit");
        submitButton.setActionCommand("submit");
        submitButton.addActionListener(this);

        Insets dummy = new Insets(0, 0, 0, 0);
        root.add(monthSpinner, new GridBagConstraints(0, 0, 1, 1, .2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));
        root.add(new JLabel("months"), new GridBagConstraints(1, 0, 1, 1, .2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));
        root.add(daySpinner, new GridBagConstraints(2, 0, 1, 1, .2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));
        root.add(new JLabel("days"), new GridBagConstraints(3, 0, 1, 1, .2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));
        root.add(submitButton, new GridBagConstraints(4, 0, 2, 1, .2, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));

        this.add(root);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.pack();
        this.setSize(300, 100);
        this.setLocation(500, 300);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("submit")) {
            int months = (int)monthSpinner.getValue(), days = (int)daySpinner.getValue();
            timeSpan = Period.of(0, months, days);
            this.dispose();
        }
    }
}