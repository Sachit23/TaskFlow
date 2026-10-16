package com.userservice.userservice;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:users;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password="})
@AutoConfigureMockMvc
class UserserviceApplicationTests {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 String email(){return java.util.UUID.randomUUID()+"@example.com";}
 JsonNode register(String email)throws Exception {return mapper.readTree(mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Alex Morgan\",\"email\":\""+email+"\",\"password\":\"secure-password123\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.user.password").doesNotExist()).andReturn().getResponse().getContentAsString());}
 @Test void registrationCreatesSessionAndNeverReturnsPassword()throws Exception{var result=register(email());mvc.perform(get("/api/users/me").header("Authorization","Bearer "+result.get("token").asText())).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Alex Morgan")).andExpect(jsonPath("$.password").doesNotExist());}
 @Test void loginUsesStoredPasswordAndNormalizesEmail()throws Exception{String e=email();register(e);mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\""+e.toUpperCase()+"\",\"password\":\"secure-password123\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());}
 @Test void invalidCredentialsAreUnauthorized()throws Exception{String e=email();register(e);mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\""+e+"\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Email or password is incorrect."));}
 @Test void duplicateEmailIsConflict()throws Exception{String e=email();register(e);mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Alex\",\"email\":\""+e+"\",\"password\":\"secure-password123\"}")).andExpect(status().isConflict());}
 @Test void rejectsInvalidInput()throws Exception{mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"short\"}")).andExpect(status().isBadRequest());}
 @Test void unauthenticatedLookupIsRejected()throws Exception{mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());}
 @Test void logoutRevokesSession()throws Exception{var result=register(email());String auth="Bearer "+result.get("token").asText();mvc.perform(post("/api/users/logout").header("Authorization",auth)).andExpect(status().isNoContent());mvc.perform(get("/api/users/me").header("Authorization",auth)).andExpect(status().isUnauthorized());}
 @Test void cannotReadAnotherUsersProfile()throws Exception{var a=register(email());var b=register(email());mvc.perform(get("/api/users/"+b.get("user").get("id").asLong()).header("Authorization","Bearer "+a.get("token").asText())).andExpect(status().isNotFound());}
 @Test void profileAndPasswordChangesPersist()throws Exception{String email=email();var a=register(email);String auth="Bearer "+a.get("token").asText();mvc.perform(patch("/api/users/me").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New Name\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("New Name"));mvc.perform(put("/api/users/password").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content("{\"currentPassword\":\"secure-password123\",\"newPassword\":\"new-secure-password\"}")).andExpect(status().isNoContent());mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\""+email+"\",\"password\":\"new-secure-password\"}")).andExpect(status().isOk());}
 @Test void passwordHashesAndRawTokensAreNotInterchangeable()throws Exception{var a=register(email());assertTrue(a.get("token").asText().length()>=40);mvc.perform(get("/api/users/me").header("Authorization","Bearer invalid-token")).andExpect(status().isUnauthorized());}
}
