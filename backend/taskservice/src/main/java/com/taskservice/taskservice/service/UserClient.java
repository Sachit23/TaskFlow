package com.taskservice.taskservice.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
@Service
public class UserClient {
 public record Identity(Long id,String name,String email) {}
 private final RestTemplate http;private final String url;
 public UserClient(RestTemplate http,@Value("${userservice.url}")String url){this.http=http;this.url=url;}
 public Identity authenticate(String authorization){
  if(authorization==null||!authorization.startsWith("Bearer "))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in to continue.");
  HttpHeaders headers=new HttpHeaders();headers.set("Authorization",authorization);
  try {Identity user=http.exchange(url+"/api/users/me",HttpMethod.GET,new HttpEntity<>(headers),Identity.class).getBody();if(user==null||user.id()==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Account service is temporarily unavailable.");return user;}
  catch(HttpClientErrorException e){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Your session has expired. Please sign in again.");}
  catch(RestClientException e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Account service is temporarily unavailable. Try again shortly.");}
 }
}
