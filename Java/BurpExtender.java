package burp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import burp.IMenuItemHandler;
import burp.MenuItemListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BurpExtender implements IBurpExtender, IHttpListener, IContextMenuFactory
{
    private IExtensionHelpers helpers;
    private IBurpExtenderCallbacks callbacks;
    private PrintWriter stdout;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        // reference to callback object
        this.callbacks = callbacks;
        // get helpers
        this.helpers = callbacks.getHelpers();
        // extension name
        callbacks.setExtensionName("Request Minimizer");
        // system output stream
        stdout = new PrintWriter(callbacks.getStdout(), true);
        // registering HTTP listener
        callbacks.registerHttpListener(this);
        // registering Context Menu Callback
        callbacks.registerContextMenuFactory(this);
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo){}

    // implementing option in context menu(rign click on request)
    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation){
        IHttpRequestResponse[] requestResponse = invocation.getSelectedMessages();
        List<JMenuItem> menu = new ArrayList<JMenuItem>();

        // Header minimalization button
        JMenuItem ContextMenuButton_headers = new JMenuItem("Minimize Headers");
        ContextMenuButton_headers.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    burp.requestMinimizer minimizer = new burp.requestMinimizer(callbacks, helpers, requestResponse, stdout);
                    minimizer.start();
                }

            });
        

        // Cookie minimalization button
        JMenuItem ContextMenuButton_cookies = new JMenuItem("Minimize Cookies");
        ContextMenuButton_cookies.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    burp.cookieMinimizer cookie_minimizer = new burp.cookieMinimizer(callbacks, helpers, requestResponse, stdout);
                    cookie_minimizer.start();
                }

            });

        menu.add(ContextMenuButton_headers);
        menu.add(ContextMenuButton_cookies);

        return menu;
    }

}