import org.junit.Test;
import webserver.HttpRequest;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Created by user on 2017-06-13.
 */
public class HttpRequestTest {
    private String testDirectory = "./src/test/resource/";

    @Test
    public void request_GET() throws Exception{
        InputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));
        HttpRequest request = new HttpRequest(in);

        assertEquals("GET", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParameter("userId"));
        assertEquals("password",request.getParameter("password"));
        //assertEquals("name",request.getParameter("JaeSung"));

    }

    @Test
    public void request_POST() throws Exception{
        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = new HttpRequest(in);

        assertEquals("POST", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive",request.getHeader("Connection"));
        assertEquals("javajigi",request.getParameter("userId"));
        assertEquals("password",request.getParameter("password"));
        //assertEquals("name",request.getParameter("JaeSung"));

    }

    @Test
    public void read() throws Exception{

        InputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String t = bufferedReader.readLine();
        while (!"".equals(t)){
            System.out.println(t);
            t = bufferedReader.readLine();
        }


    }
}
