package com.wor.dash.jwt.model.service;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.wor.dash.user.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {
	
	@Value("${jwt.secret}")
	private String secretKey;
	
	@Value("${jwt.access-token-expiration}")
    private long accessTokenExpire;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpire;
    
    private final TokenService tokenRepository;

    public JwtServiceImpl(TokenService tokenRepository) {
    	this.tokenRepository = tokenRepository;
    }

	@Override
	public String extractUserEmail(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	@Override
	public boolean isValid(String token, UserDetails user) {
		String userEmail = extractUserEmail(token);
		
		int validToken = tokenRepository
				.findByAccessToken(token)
				.map(t -> t.getLogout()^1)
				.orElse(1);
		
		return (userEmail.equals(user.getUsername())) && !isTokenExpired(token) && validToken == 1;
	}

	public boolean isValidRefreshToken(String token, User user) {
        String userEmail = extractUserEmail(token);

        int validRefreshToken = tokenRepository
                .findByRefreshToken(token)
                .map(t -> t.getLogout()^1)
                .orElse(1);

        return (userEmail.equals(user.getUserEmail())) && !isTokenExpired(token) && validRefreshToken == 1;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpire);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpire );
    }
    
    //86400000 밀리세컨드-> 86400세컨드 -> 1440 분 -> 24시간 
    private String generateToken(User user, long expireTime) {
        String token = Jwts
                .builder()
                .subject(user.getUserEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime ))
                .signWith(getSigninKey())
                .compact();

        return token;
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
