package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.List;

public class MenuItemListener implements ActionListener {
    // class used to handle the button click in context menu
    private final IHttpRequestResponse[] requestResponse;
    private PrintWriter stdout;

    MenuItemListener(IHttpRequestResponse[] requestResponse,PrintWriter stdout) {
        this.requestResponse = requestResponse;
        this.stdout = stdout;
    }

    // action to be performed on th click
    @Override
    public void actionPerformed(ActionEvent e) {
        stdout.println("button click");
    }
}