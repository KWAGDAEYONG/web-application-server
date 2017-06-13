package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2017-06-07.
 */
public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private Map<String, String> header = new HashMap<String, String>();
    private Map<String, String> param = new HashMap<String, String>();
    private String method;
    private String path;

    public HttpRequest(InputStream in)throws IOException{

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String line = bufferedReader.readLine();

        if(line==null){
            return;
        }

        String token[] = line.split(" ");
        this.method = token[0];

        int conLen = 0;
        while(!"".equals(line)){
            log.debug(line);
            line = bufferedReader.readLine();
            if(!"".equals(line)) {
                String headers[] = line.split(": ");
                header.put(headers[0], headers[1]);
                if (line.startsWith("Content-Length")) {
                    conLen = Integer.parseInt(headers[1]);
                }
            }

        }

        if(this.method.equals("GET")) {
            int idx = token[1].indexOf("?");
            this.path = token[1].substring(0, idx);
            this.param = HttpRequestUtils.myParseQueryString(token[1].substring(idx + 1), param);
            System.out.println(getParameter("userId")+"/"+getParameter("password")+"/"+getParameter("name"));
        }else if(this.method.equals("POST")){
            this.path = token[1];
            String data = IOUtils.readData(bufferedReader,conLen);
            this.param = HttpRequestUtils.myParseQueryString(data, param);
            System.out.println(getParameter("userId")+"/"+getParameter("password")+"/"+getParameter("name"));
        }


    }

    public String getHeader(String field){

        return header.get(field);
    }

    public String getParameter(String parameter){
        return param.get(parameter);
    }

    public String getMethod(){
        return this.method;
    }

    public String getPath(){

        return this.path;
    }

}
