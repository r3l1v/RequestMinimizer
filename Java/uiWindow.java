package burp;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class uiWindow implements ActionListener{

    private JFrame frame;
    private JScrollPane mainScrollPane;
    private JPanel mainPanel;
    private JButton copyHTMLButton;
    private JLabel requestLabel;
    private JScrollPane requestScrollPane;
    private JTextPane requestTextPane;


    public uiWindow(String title) {
        initialize(title);
    }

    private void initialize(String title) {
        frame = new JFrame();
        frame.setTitle(title);
        frame.setBounds(100, 100, 775, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainScrollPane = new JScrollPane(getMainPanel());
        frame.getContentPane().add(mainScrollPane);
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setPreferredSize(new Dimension(750, 600));
            mainPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(11, 4, 11, 8));

            requestLabel = new JLabel("Request to: ");
            requestLabel.setAlignmentX(0.0f);

            requestTextPane = new JTextPane();
            requestTextPane.getDocument().putProperty("name", "Request");
            requestScrollPane = new JScrollPane(requestTextPane);
            requestScrollPane.setBorder(new LineBorder(Color.GRAY, 1, true));
            requestScrollPane.setPreferredSize(new Dimension(730, 400));
            requestScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 400));
            requestScrollPane.setAlignmentX(0.0f);

            copyHTMLButton = new JButton("Copy Request");
            copyHTMLButton.setAlignmentX(0.0f);
            copyHTMLButton.addActionListener(this);
            copyHTMLButton.setActionCommand("Copy Request");

            mainPanel.add(requestLabel);
            mainPanel.add(Box.createRigidArea(new Dimension(10, 8)));
            mainPanel.add(requestScrollPane);
            mainPanel.add(Box.createRigidArea(new Dimension(10, 8)));
            //mainPanel.add(Box.createRigidArea(new Dimension(10, 8)));
            //mainPanel.add(Box.createRigidArea(new Dimension(10, 8)));
            mainPanel.add(copyHTMLButton);
            mainPanel.revalidate();
        }
        return mainPanel;
    }
    public void setRequestLabel(String url) {
        requestLabel.setText(new StringBuilder()
                .append("Request to: ")
                .append(url)
                .toString()
        );
    }

    public void setVisible() {
        frame.setVisible(true);
    }

    public void setRequest(String request) {
        requestTextPane.setText(request);
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("Copy Request")) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(requestTextPane.getText()), null);
        }
    }

}
