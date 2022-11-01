from burp import IBurpExtender
from burp import IHttpListener
from burp import IProxyListener
from burp import IHttpRequestResponse
from java.io import PrintWriter
from burp import IContextMenuFactory, IContextMenuInvocation
from java.awt import Toolkit
from java.awt.event import ActionListener
from java.awt.datatransfer import StringSelection
from javax.swing import JMenuItem
from java.util import ArrayList
from burp import IParameter, IRequestInfo
from functools import partial
import sys
import threading

class RequestMinimizer(object):
    def __init__(self, callbacks, helpers, requestResponses, stdout):
        self._request = requestResponses[0]
        self._Originalrequest = self._request.getRequest()
        self._OriginaResponse = self._request.getResponse()
        self._httpService = self._request.getHttpService()
        self._callbacks = callbacks
        self._helpers = helpers
        self._stdout = stdout

        return

    #Method used to start minimalization of the request
    def minimize(self):
        t = threading.Thread(target=self.kunda)
        #t.daemon = True
        t.start()

        return

    #Method to make requests in the new thread
    def kunda(self):

        #get request destination destination info
        host = self._httpService.host
        port = self._httpService.port
        protocol = True if self._httpService.protocol.lower() == 'https' else False

        #prepare reqest to minimize
        requestInfo = self._helpers.analyzeRequest(self._Originalrequest)
        OriginalrequestHeaders = requestInfo.getHeaders()
        bodyOffset = requestInfo.bodyOffset
        requestBody = self._Originalrequest.tostring()[bodyOffset:]

        #build request to minimize
        requestToMinimize = self._helpers.buildHttpMessage(OriginalrequestHeaders, requestBody)

        #response to original request
        OriginaResponse = self._callbacks.makeHttpRequest(self._httpService,requestToMinimize).getResponse()

        #initiaze array of necessary headers
        necessaryHeaders = []
        necessaryHeaders.append(OriginalrequestHeaders[0])
        necessaryHeaders.append(OriginalrequestHeaders[1])

        #iterate through headers and remove not neccessary one
        for i in range(2,len(OriginalrequestHeaders)):
            #copy headers and remove one
            tempHeaders = OriginalrequestHeaders[:]
            del(tempHeaders[i])
            #make request without the one header
            tempRequest = self._helpers.buildHttpMessage(tempHeaders, requestBody)
            tempResponse = self._callbacks.makeHttpRequest(self._httpService,tempRequest).getResponse()

            #compare responses
            difference = set(self._helpers.analyzeResponseVariations([OriginaResponse, tempResponse]).getVariantAttributes())
            #if there is a difference, header is neccessary
            if(len(difference) > 0):
                necessaryHeaders.append(tempHeaders[i])

        #make and print minimized request :)
        minimizedRequest = self._helpers.buildHttpMessage(necessaryHeaders, requestBody)
        minimizedResponse = self._callbacks.makeHttpRequest(self._httpService,minimizedRequest).getResponse()
        self._stdout.println(minimizedRequest.tostring())

        return

class BurpExtender(IBurpExtender, IHttpListener, IProxyListener, IContextMenuFactory):

    def	registerExtenderCallbacks(self, callbacks):
        # reference to callback object
        self._callbacks = callbacks
        #get helpers
        self._helpers = callbacks.getHelpers()
        # extension name
        callbacks.setExtensionName("requestMinimizer")
        # system output stream
        self._stdout = PrintWriter(callbacks.getStdout(), True)
        # registering HTTP listener
        callbacks.registerHttpListener(self)
        # registering Context Menu callbacks
        callbacks.registerContextMenuFactory(self)

    #method that the button calls
    def actionMenu(self, event):
        requestResponses = self.context.getSelectedMessages()
        #request = httpTraffic[0]
        RequestMinimizer(self._callbacks, self._helpers, requestResponses, self._stdout).minimize()

        return

    # implementing option in context menu(rign click on request)
    def createMenuItems(self, invocation):

         self.context = invocation
         label = "Send to Minimizer"
         menuItem = JMenuItem(label, actionPerformed=self.actionMenu)

         menuArray = ArrayList()
         menuArray.add(menuItem)

         return menuArray

    # implementing test IHttpListener, for debug process
    def processHttpMessage(self, toolFlag, messageIsRequest, messageInfo):
    #    self._stdout.println(
    #            ("HTTP request to " if messageIsRequest else "HTTP response from ") +
    #            messageInfo.getHttpService().toString() +
    #            " [" + self._callbacks.getToolName(toolFlag) + "]")
        return
