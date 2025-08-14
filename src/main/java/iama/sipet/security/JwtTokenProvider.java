package iama.sipet.security;

import java.util.Base64;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {
    private static final String SECRET_KEY = "GyjU2HCdb7wBG/1hUPcOJhpRLWiKtaEq/+QORPupraiMbHcbRVy4cMuXjTBXm875F6bvKJ6+Perinwmt2Gd/hw==";
    private static final long EXPIRATION_TIME = 86400000; // 1 days

    @SuppressWarnings("deprecation")
    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);
            Jwts.parserBuilder()
                    .setSigningKey(decodedKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);
        return Jwts.parserBuilder()
                .setSigningKey(decodedKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
