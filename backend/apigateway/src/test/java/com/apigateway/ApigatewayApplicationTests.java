package com.apigateway;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApigatewayApplicationTests {
 static HttpServer users=stub("users"),tasks=stub("tasks");
 @LocalServerPort int port;
 static HttpServer stub(String service){try{var s=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);s.createContext("/",x->{byte[] body=(service+":"+x.getRequestURI().getPath()+":"+x.getRequestHeaders().getFirst("Authorization")).getBytes(StandardCharsets.UTF_8);x.sendResponseHeaders(200,body.length);try(var stream=x.getResponseBody()){stream.write(body);}});s.start();return s;}catch(Exception e){throw new IllegalStateException(e);}}
 @DynamicPropertySource static void properties(DynamicPropertyRegistry r){r.add("USERSERVICE_URL",()->"http://127.0.0.1:"+users.getAddress().getPort());r.add("TASKSERVICE_URL",()->"http://127.0.0.1:"+tasks.getAddress().getPort());}
 @AfterAll static void stop(){users.stop(0);tasks.stop(0);}
 HttpResponse<String> get(String path)throws Exception{return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Authorization","Bearer example").build(),HttpResponse.BodyHandlers.ofString());}
 @Test void routesUsersAndPreservesBearerToken()throws Exception{var r=get("/api/users/me");assertEquals(200,r.statusCode());assertEquals("users:/api/users/me:Bearer example",r.body());assertEquals("no-store",r.headers().firstValue("Cache-Control").orElse(""));}
 @Test void routesTaskAndProjectPaths()throws Exception{assertEquals("tasks:/api/tasks:Bearer example",get("/api/tasks").body());assertEquals("tasks:/api/projects:Bearer example",get("/api/projects").body());assertEquals("tasks:/api/activity:Bearer example",get("/api/activity").body());}
 @Test void readinessEndpointIsAvailable()throws Exception{assertEquals(200,get("/actuator/health/readiness").statusCode());}
 @Test void limitsAuthenticationAttempts()throws Exception{var client=HttpClient.newHttpClient();String ip=java.util.UUID.randomUUID().toString();int status=0;for(int i=0;i<21;i++){status=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/users/login")).header("X-Real-IP",ip).build(),HttpResponse.BodyHandlers.ofString()).statusCode();}assertEquals(429,status);}
}
