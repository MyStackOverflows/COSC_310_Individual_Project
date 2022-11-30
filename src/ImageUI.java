import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.*;

public class ImageUI extends JDialog implements ActionListener {
    ArrayList<byte[]> data;
    int index;
    JLabel imgFrame;

    public ImageUI(ArrayList<byte[]> _data) {
        super(null, "View product images", ModalityType.DOCUMENT_MODAL);   // set modality so the main thread in InventorySystem that calls this constructor waits until this dialog gets disposed

        data = _data;
        index = 0;
        imgFrame = new JLabel();
        updateImage();

        JPanel root = new JPanel();
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setLayout(new BorderLayout());

        // add buttons
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new GridLayout(1, 2, 5, 5));
        for (JButton button : new JButton[] {
                new JButton("Previous"),
                new JButton("Next") }) {
            button.setActionCommand(button.getText().toLowerCase());
            button.addActionListener(this);
            subPanel.add(button);
        }

        // set up our main layout
        root.add(imgFrame, BorderLayout.CENTER);
        root.add(subPanel, BorderLayout.SOUTH);
        this.add(root);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.pack();
        this.setSize(500, 575);
        this.setLocation(300, 300);
        this.setVisible(true);
    }

    public void updateImage() {
        // update the image based on the current index
        ImageIcon icon = new ImageIcon(data.get(index));
        Image img = icon.getImage();
        
        // properly scale the image based on a max width and height of 480 pixels
        double w = icon.getIconWidth(), h = icon.getIconHeight();
        if (w > h)
            img = img.getScaledInstance(480, (int)(480 * (h / w)), Image.SCALE_SMOOTH);
        else
            img = img.getScaledInstance((int)(480 * (w / h)), 480, Image.SCALE_SMOOTH);

        // update the image as an ImageIcon
        imgFrame.setIcon(new ImageIcon(img));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("next")) {
            index++;
            if (index == data.size())
                index = 0;
        }
        else if (e.getActionCommand().equals("previous")) {
            index--;
            if (index == -1)
                index = data.size() - 1;
        }
        updateImage();
    }
}
