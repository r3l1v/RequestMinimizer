package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.List;

public class MenuItemListener implements ActionListener {
    // class used to handle the button click in context menu
    private IHttpRequestResponse[] requestResponse;
    private PrintWriter stdout;
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;

    MenuItemListener(IBurpExtenderCallbacks callbacks,IExtensionHelpers helpers, IHttpRequestResponse[] requestResponse,PrintWriter stdout) {
        this.requestResponse = requestResponse;
        this.stdout = stdout;
        this.callbacks = callbacks;
        this.helpers = helpers;
    }

    // action to be performed on th click
    @Override
    public void actionPerformed(ActionEvent e) {
        burp.requestMinimizer minimizer = new burp.requestMinimizer(callbacks, helpers, requestResponse, stdout);
        minimizer.minimize();
    }
}