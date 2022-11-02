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
        callbacks.setExtensionName("requestMinimizer");
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
        JMenuItem ContextMenuButton = new JMenuItem("Send to Minimizer");

        MenuItemListener listener = new MenuItemListener(callbacks, helpers, requestResponse, stdout);
        ContextMenuButton.addActionListener(listener);

        menu.add(ContextMenuButton);
        return menu;
    }

}