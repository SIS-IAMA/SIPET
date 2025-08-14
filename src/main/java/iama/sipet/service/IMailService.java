package iama.sipet.service;

import iama.sipet.response.UserResponseRest;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;

public interface IMailService {
    public ResponseEntity<UserResponseRest> createToken(String User);
    public ResponseEntity<UserResponseRest> checkToken(String token);
}