package bff_cajeros.banco.controller;

import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@RequestMapping("/api/auth")
public class AuthController {
    
    
    private final JwtEncoder JwtEncoder;

    public AuthController(JwtEncoder jwtEncoder){
        this.JwtEncoder = jwtEncoder;
    }

    @PostMapping("/token")
    public ResponseEntity<?> genararToken(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        //validacion de credenciales de cajero autorizado
        if("cajero_admin".equals(username) && "duoc123".equals(password)){
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("banco-auth-server")
            .issuedAt(now)
            .expiresAt(now.plus(1,ChronoUnit.HOURS))
            .subject(username)
            .claim("scope", "ROLE_CAJERO")
            .build();

            String token = JwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(() -> "HS256").build(), claims)).getTokenValue();
            return ResponseEntity.ok(Map.of("access_token", token, "token_type", "Bearer", "expires_in", 3600));

              }
        return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
    }
    


}
