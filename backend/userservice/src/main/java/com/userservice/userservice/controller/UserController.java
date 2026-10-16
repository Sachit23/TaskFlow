package com.userservice.userservice.controller;
import com.userservice.userservice.service.UserService;
import com.userservice.userservice.dto.UserResponse;
import com.userservice.userservice.dto.AuthRequests.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/users")
public class UserController {
 private final UserService users;public UserController(UserService users){this.users=users;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public AuthResponse register(@Valid @RequestBody Register r){return users.register(r);}
 @PostMapping("/login") public AuthResponse login(@Valid @RequestBody Login r){return users.login(r);}
 @GetMapping("/me") public UserResponse me(@RequestHeader(value="Authorization",required=false)String auth){return users.me(auth);}
 @GetMapping("/{id}") public UserResponse user(@PathVariable Long id,@RequestHeader(value="Authorization",required=false)String auth){UserResponse user=users.me(auth);if(!user.getId().equals(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found.");return user;}
 @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(@RequestHeader("Authorization")String auth){users.logout(auth);}
 @PatchMapping("/me") public UserResponse profile(@RequestHeader("Authorization")String auth,@Valid @RequestBody Profile r){return users.profile(auth,r);}
 @PutMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT) public void password(@RequestHeader("Authorization")String auth,@Valid @RequestBody Password r){users.changePassword(auth,r);}
}
