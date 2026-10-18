package com.userservice.userservice.service;
import com.userservice.userservice.dto.AuthRequests.*;
import com.userservice.userservice.dto.UserResponse;
import com.userservice.userservice.entity.*;
import com.userservice.userservice.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
@Service @Transactional
public class UserService {
 private final UserRepository users; private final SessionRepository sessions; private final PasswordEncoder encoder;
 private final SecureRandom random=new SecureRandom(); private final String dummyHash;
 public UserService(UserRepository users,SessionRepository sessions,PasswordEncoder encoder){this.users=users;this.sessions=sessions;this.encoder=encoder;this.dummyHash=encoder.encode("invalid-login-placeholder");}
 public AuthResponse register(Register request){
  String email=request.email().trim().toLowerCase(Locale.ROOT);
  if(users.findByEmail(email).isPresent())throw new ResponseStatusException(CONFLICT,"An account with this email already exists.");
  checkPassword(request.password());
  User user=new User();user.setName(request.name().trim());user.setEmail(email);user.setPassword(encoder.encode(request.password()));
  return issue(users.saveAndFlush(user));
 }
 public AuthResponse login(Login request){
  User user=users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT)).orElse(null);
  boolean matches=encoder.matches(request.password(),user==null?dummyHash:user.getPassword());
  if(user==null||!matches)throw new ResponseStatusException(UNAUTHORIZED,"Email or password is incorrect.");
  return issue(user);
 }
 private AuthResponse issue(User user){
  sessions.deleteByExpiresAtBefore(Instant.now());
  byte[] bytes=new byte[32];random.nextBytes(bytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  UserSession session=new UserSession();session.setTokenHash(hash(token));session.setUserId(user.getId());session.setExpiresAt(Instant.now().plus(7,ChronoUnit.DAYS));sessions.save(session);
  return new AuthResponse(token,mapToResponse(user));
 }
 public User current(String authorization){
  String token=token(authorization);UserSession s=sessions.findById(hash(token)).filter(v->v.getExpiresAt().isAfter(Instant.now())).orElseThrow(()->new ResponseStatusException(UNAUTHORIZED,"Your session has expired. Please sign in again."));
  return users.findById(s.getUserId()).orElseThrow(()->new ResponseStatusException(UNAUTHORIZED,"Please sign in again."));
 }
 public UserResponse me(String authorization){return mapToResponse(current(authorization));}
 public void logout(String authorization){sessions.deleteById(hash(token(authorization)));}
 public UserResponse profile(String authorization,Profile request){User user=current(authorization);user.setName(request.name().trim());return mapToResponse(users.save(user));}
 public void changePassword(String authorization,Password request){User user=current(authorization);if(!encoder.matches(request.currentPassword(),user.getPassword()))throw new ResponseStatusException(BAD_REQUEST,"Current password is incorrect.");checkPassword(request.newPassword());user.setPassword(encoder.encode(request.newPassword()));users.save(user);sessions.deleteByUserIdAndTokenHashNot(user.getId(),hash(token(authorization)));}
 private void checkPassword(String password){if(password.getBytes(StandardCharsets.UTF_8).length>72)throw new ResponseStatusException(BAD_REQUEST,"Password must be at most 72 UTF-8 bytes.");}
 private String token(String auth){if(auth==null||!auth.startsWith("Bearer ")||auth.length()>100)throw new ResponseStatusException(UNAUTHORIZED,"Please sign in to continue.");return auth.substring(7);}
 private String hash(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 public UserResponse mapToResponse(User user){UserResponse r=new UserResponse();r.setId(user.getId());r.setName(user.getName());r.setEmail(user.getEmail());return r;}
}
