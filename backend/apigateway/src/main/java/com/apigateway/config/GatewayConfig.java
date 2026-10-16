package com.apigateway.config;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
public class GatewayConfig implements GlobalFilter,Ordered {
 private record Window(long minute,int count) {}
 private final ConcurrentHashMap<String,Window> attempts=new ConcurrentHashMap<>();
 public int getOrder(){return -100;}
 public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){
  var request=exchange.getRequest();String path=request.getURI().getPath();
  exchange.getResponse().getHeaders().set("Cache-Control","no-store");
  exchange.getResponse().getHeaders().set("X-Content-Type-Options","nosniff");
  if(path.equals("/api/users/login")||path.equals("/api/users/register")){
   long minute=Instant.now().getEpochSecond()/60;
   // Only the private reverse proxy can reach this service in Compose.
   String ip=request.getHeaders().getFirst("X-Real-IP");if(ip==null)ip=String.valueOf(request.getRemoteAddress());
   if(attempts.size()>10000)attempts.entrySet().removeIf(e->e.getValue().minute()<minute);
   if(attempts.size()>20000){exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);return exchange.getResponse().setComplete();}
   Window value=attempts.compute(ip,(k,v)->v==null||v.minute()!=minute?new Window(minute,1):new Window(minute,v.count()+1));
   if(value.count()>20){exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);exchange.getResponse().getHeaders().set("Retry-After","60");return exchange.getResponse().setComplete();}
  }
  return chain.filter(exchange);
 }
}
