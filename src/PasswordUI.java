import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class PasswordUI extends JDialog implements ActionListener {
    boolean verified, editing;
    JTextField input;
    String password;
    public PasswordUI(String _password, boolean _editing) {
        super(null, _editing ? "Change password" : "Credential Check", ModalityType.DOCUMENT_MODAL);   // set modality so the main thread in InventorySystem that calls this constructor waits until this dialog gets disposed

        verified = false;
        editing = _editing;
        password = _password;
        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setLayout(new GridBagLayout());
        input = new JPasswordField();
        input.setPreferredSize(new Dimension(270, 50));
        input.setFont(input.getFont().deriveFont(18f));
        JButton submitButton = new JButton("Submit");
        submitButton.setActionCommand("submit");
        submitButton.addActionListener(this);

        Insets dummy = new Insets(0, 0, 0, 0);
        root.add(input, new GridBagConstraints(0, 0, 1, 1, 0.9, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));
        root.add(submitButton, new GridBagConstraints(1, 0, 1, 1, 0.1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, dummy, 0, 0));

        // frame parameter boilerplate code
        this.add(root);    // add the main root panel to the frame
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //this.setTitle("Credential Check");    // this line commented out as the code for modality already sets the title
        this.pack();
        this.setSize(300, 90);
        this.setLocation(500, 300);
        this.setVisible(true);     // actually show the window
    }

    @Override
    public void actionPerformed(ActionEvent e) {    // this is a listener method that waits for any GUI event to occur and runs the corresponding method for each event
        if (e.getActionCommand().equals("submit")) {
            if (editing) {
                password = input.getText();
                if (password.equals(""))
                    return;
                JOptionPane.showMessageDialog(this, "Password set to '" + password + "'");
            }
            else {
                if (input.getText().equals(password))
                    verified = true;
                else {
                    JOptionPane.showMessageDialog(this, "Incorrect password.", "Error!", 0);
                    return;
                }
            }
            this.dispose();
        }
    }
}