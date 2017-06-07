package webserver;

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
    private Map<String, String> header = new HashMap<String, String>();

    public HttpRequest(InputStream request)throws IOException{

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request));
        String line = bufferedReader.readLine();

        if(line==null){
            return;
        }
        while(!"".equals(line)){
            String token[] = line.split(" ");
            if(!token[0].contains(":")) {
                header.put(token[0],token[1]);
                System.out.println("push =>"+token[0]+" : "+token[1]);
            }else {
                header.put(token[0].substring(0, token[0].length() - 1), token[1]);
                System.out.println("push =>"+token[0].substring(0, token[0].length()-1)+" : "+token[1]);
            }
            line = bufferedReader.readLine();
        }
    }

    public String getHeader(String field){

        return header.get(field);
    }

}
