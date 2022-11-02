package burp;

import java.io.PrintWriter;
import java.util.List;
import java.util.Arrays;

public class requestMinimizer {

    private IHttpRequestResponse requestResponse;
    private PrintWriter stdout;
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;

    private byte[] originalRequest;
    private byte[] originalResponse;
    private IHttpService httpService;

    public requestMinimizer(IBurpExtenderCallbacks callbacks,IExtensionHelpers helpers, IHttpRequestResponse[] requestResponse, PrintWriter stdout){
        this.requestResponse = requestResponse[0];
        this.stdout = stdout;
        this.callbacks = callbacks;
        this.helpers = helpers;

        this.originalRequest = this.requestResponse.getRequest();
        this.originalResponse = this.requestResponse.getResponse();
        this.httpService = this.requestResponse.getHttpService();
    }

    public void minimize(){

        String host = httpService.getHost();
        int port = httpService.getPort();
        boolean protocol;
        if (httpService.getProtocol() == "https"){protocol=true;}else{protocol=false;};

        //prepare request to minimize
        IRequestInfo requestInfo = helpers.analyzeRequest(originalRequest);
        List<String> originalRequestHeaders = requestInfo.getHeaders();
        int bodyOffset = requestInfo.getBodyOffset();
        String stringRequestBody = this.originalRequest.toString().substring(bodyOffset);
        
        byte[] requestBody = stringRequestBody.getBytes();

        // build request to minimize
        byte[] requestToMinimize = helpers.buildHttpMessage(originalRequestHeaders, requestBody);

        // response to original request
        byte[] originalResponse = callbacks.makeHttpRequest(httpService, requestToMinimize).getResponse();

        // initialize array of necessary headers
        List<String> necessaryHeaders = null;
        necessaryHeaders.add(originalRequestHeaders.get(0));
        necessaryHeaders.add(originalRequestHeaders.get(1));

        // iterate through headers and remove not necessary ones
        for(int i = 2; i < originalRequestHeaders.size(); i++){
            //copy headers and remove one
            List<String> tempHeaders = originalRequestHeaders;
            tempHeaders.remove(i);

            // make request without the removed header
            byte[] tempRequest = helpers.buildHttpMessage(tempHeaders, requestBody);
            byte[] tempResponse = callbacks.makeHttpRequest(httpService, tempRequest).getResponse();

            // compare responses
            List<String> difference = helpers.analyzeResponseVariations(originalResponse,tempResponse).getInvariantAttributes();
            if(difference.size() > 0){
                necessaryHeaders.add(tempHeaders.get(i));
            }
        }

        // make and print minimized request

        byte[] minimizedRequest = helpers.buildHttpMessage(necessaryHeaders, requestBody);
        byte[] minimizedResponse = callbacks.makeHttpRequest(httpService, minimizedRequest).getResponse();
        stdout.println(minimizedRequest.toString());
    }
}
