package com.taskservice.taskservice.config;
import com.taskservice.taskservice.service.UserClient;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;
@Configuration public class AuthenticationConfig implements WebMvcConfigurer {
 private final UserClient users;public AuthenticationConfig(UserClient users){this.users=users;}
 public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(new HandlerInterceptor(){
  public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){request.setAttribute("identity",users.authenticate(request.getHeader("Authorization")));return true;}
 }).addPathPatterns("/api/**");}
}
