package iama.sipet.security;

import java.util.Base64;

import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;

public class JwtSecretKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);    
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Key Generada: "+encodedKey);
    }
}
