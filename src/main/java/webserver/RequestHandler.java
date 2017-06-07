package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import static util.HttpRequestUtils.parseCookies;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = "ㅇ ㅇ"; //bufferedReader.readLine();
            String token[] = line.split(" ");
            String url = token[1];

            HttpRequest httpRequest = new HttpRequest(in);

            if(line==null){
                return;
            }
            int conLen = 0;
            boolean logined = false;
            while(!"".equals(line)){
                log.debug("header:{}",line);
                line = bufferedReader.readLine();
                //post일때 읽기
                if(line.startsWith("Content-Length")){
                    String temp[] = line.split(" ");
                    conLen = Integer.parseInt(temp[1]);
                }
                if(line.contains("Cookie")){
                    String cookieToken[] = line.split(" ");
                    Map<String, String> cookieMap = HttpRequestUtils.parseCookies(cookieToken[1]);
                    if(Boolean.parseBoolean(cookieMap.get("logined"))){
                        logined = true;
                    }
                }
            }

            if(url.contains("/create")){
                String data = IOUtils.readData(bufferedReader,conLen);
                Map<String, String> param = HttpRequestUtils.parseQueryString(data);
                User user = new User(param.get("userId"),param.get("password"),param.get("name"),param.get("email"));
                log.debug("user:{}",user.toString());
                DataBase.addUser(user);
                DataOutputStream dos = new DataOutputStream(out);
                response302Header(dos);
            }else if(url.endsWith("login")){
                String data = IOUtils.readData(bufferedReader,conLen);
                Map<String, String> param = HttpRequestUtils.parseQueryString(data);
                String id = param.get("userId");
                String password = param.get("password");
                User dbUser = DataBase.findUserById(id);
                boolean login = true;
                url = "/index.html";
                if(dbUser==null) {
                    log.debug("아이디가 없습니다");
                    login = false;
                    url = "/user/login_failed.html";
                }else if(!dbUser.getPassword().equals(password)){
                    log.debug("비밀번호가 틀립니다");
                    login = false;
                    url = "/user/login_failed.html";
                }
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
                loginHeader(dos,body.length, login);
                responseBody(dos, body);
            } else if (url.endsWith("list")) {
                if(!logined){
                    log.debug("로그인이 안되어 있습니다");
                    url = "/user/login.html";
                    DataOutputStream dos = new DataOutputStream(out);
                    byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<table>");
                for(User user : users){
                    stringBuilder.append("<tr>");
                    stringBuilder.append("<td>"+user.getUserId()+"</td>");
                    stringBuilder.append("<td>"+user.getName()+"</td>");
                    stringBuilder.append("<td>"+user.getEmail()+"</td>");
                    stringBuilder.append("</tr>");
                }
                stringBuilder.append("</table>");
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = stringBuilder.toString().getBytes();
                response200Header(dos, body.length);
                responseBody(dos,body);
            } else if(url.endsWith(".css")){
                if(url.contains("/user")){
                    String modiUrl = url.substring(5);
                    url = modiUrl;
                }
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                css200Header(dos, body.length);
                responseBody(dos, body);
            } else {
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200Header(dos, body.length);
                responseBody(dos, body);
            }


            /*DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
            response302Header(dos, body.length);
            responseBody(dos, body);*/
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void css200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loginHeader(DataOutputStream dos, int lengthOfBodyContent, boolean login) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if(login){
                log.debug("로그인 성공");
                dos.writeBytes("Set-Cookie: logined=true\r\n");
            }else{
                log.debug("로그인 실패");
                dos.writeBytes("Set-Cookie: logined=false\r\n");
            }
            dos.writeBytes("");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
