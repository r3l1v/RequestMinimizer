package burp;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;  

public class cookieMinimizer extends Thread {

    private IHttpRequestResponse requestResponse;
    private PrintWriter stdout;
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;

    private byte[] originalRequest;
    private byte[] originalResponse;
    private IHttpService httpService;

    public cookieMinimizer(IBurpExtenderCallbacks callbacks,IExtensionHelpers helpers, IHttpRequestResponse[] requestResponse, PrintWriter stdout){
        this.requestResponse = requestResponse[0];
        this.stdout = stdout;
        this.callbacks = callbacks;
        this.helpers = helpers;

        this.originalRequest = this.requestResponse.getRequest();
        this.originalResponse = this.requestResponse.getResponse();
        this.httpService = this.requestResponse.getHttpService();
    }

    @Override
    public void run(){

        String host = httpService.getHost();
        int port = httpService.getPort();
        boolean protocol;
        if (httpService.getProtocol() == "https"){protocol=true;}else{protocol=false;};

        //prepare request to minimize
        IRequestInfo requestInfo = helpers.analyzeRequest(originalRequest);
        List<String> originalRequestHeaders = requestInfo.getHeaders();

        int bodyOffset = requestInfo.getBodyOffset();
        int lenght = originalRequest.length - bodyOffset;
        //String stringRequestBody = this.originalRequest.toString().substring(bodyOffset);
        byte[] requestBody = new byte[lenght];
        System.arraycopy(originalRequest,requestInfo.getBodyOffset(), requestBody, 0, lenght);

        // build request to minimize
        byte[] requestToMinimize = helpers.buildHttpMessage(originalRequestHeaders, requestBody);

        // response to original request
        byte[] originalResponse = callbacks.makeHttpRequest(httpService, requestToMinimize).getResponse();

        //minimalizing cookies

        List<String> original_cookies = new ArrayList<String>();
        //extract cookies
        for(String header : originalRequestHeaders){
            if(header.contains("Cookie")){
                String[] temp = header.replace("Cookie: ", "").split(";",0);
                original_cookies = Arrays.asList(temp);
            }
        }

        // initialize array of necessary cookies
        List<String> necessaryCookies = new ArrayList<String>();

        for(int i = 0; i < original_cookies.size();i++){
            //copy cookies and remove one
            List<String> temp_cookies = new ArrayList<String>(original_cookies);
            temp_cookies.remove(i);

            //return back to string format

            String result = temp_cookies.stream()  
                            .map(String::valueOf)  
                            .collect(Collectors.joining(";"));
            
            result = "Cookie: " + result;

            // add header with removed cookie
            List<String> tempHeaders = new ArrayList<String>(originalRequestHeaders);

            for(int j = 0; j < tempHeaders.size();j++){
                if(tempHeaders.get(j).contains("Cookie")){
                    tempHeaders.set(j, result);
                }
            }

            // make request without the removed cookie
            byte[] tempRequest = helpers.buildHttpMessage(tempHeaders, requestBody);
            byte[] tempResponse = callbacks.makeHttpRequest(httpService, tempRequest).getResponse();

            // compare responses and if they differ, add cookie to list of neccessary cookies
            List<String> difference = new ArrayList<String>();
            difference = helpers.analyzeResponseVariations(originalResponse,tempResponse).getVariantAttributes();
            if(difference.size() > 0){
               necessaryCookies.add(original_cookies.get(i));
            }
        }

        //necessary cookies to string and if 0 cookies are neccessary then remove the header

        List<String> necessaryHeaders = new ArrayList<String>(originalRequestHeaders);

        if(necessaryCookies.size() > 0){
            String Cookie_header = necessaryCookies.stream()  
                                .map(String::valueOf)  
                                .collect(Collectors.joining(";"));
                
            Cookie_header = "Cookie: " + Cookie_header;

            for(int j = 0; j < necessaryHeaders.size();j++){
                if(necessaryHeaders.get(j).contains("Cookie")){
                    necessaryHeaders.set(j, Cookie_header);
                }   
            }
        }else{
            for(int j = 0; j < necessaryHeaders.size();j++){
                if(necessaryHeaders.get(j).contains("Cookie")){
                    necessaryHeaders.remove(j);
                }   
            }
        }

        // make and print minimized request

        byte[] minimizedRequest = helpers.buildHttpMessage(necessaryHeaders, requestBody);
        byte[] minimizedResponse = callbacks.makeHttpRequest(httpService, minimizedRequest).getResponse();

        burp.uiWindow window = new burp.uiWindow("Request Minimizer");
        window.setVisible();
        window.setRequest(helpers.bytesToString(minimizedRequest));
        window.setRequestLabel(requestInfo.getUrl().toString());

    }
}
