package com.wor.dash.jwt.model.service;

import java.util.List;

import com.wor.dash.password.model.PasswordAnswer;
import com.wor.dash.password.model.service.PasswordService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wor.dash.jwt.model.Token;
import com.wor.dash.response.AuthenticationResponse;
import com.wor.dash.user.model.User;
import com.wor.dash.user.model.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordService passwordRepository;

    public AuthenticationServiceImpl(UserService repository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     TokenService tokenRepository,
                                     AuthenticationManager authenticationManager,
                                     PasswordService passwordRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordRepository = passwordRepository;
    }

    @Override
//	@Transactional
    public AuthenticationResponse register(User request) {
        log.info("AuthenticationService/register");
        if(repository.getUserEmail(request.getUserId()) != null) {
            return new AuthenticationResponse(null, null,"User already exist");
        }

        User user = new User();
        user.setUserEmail(request.getUserEmail());
        user.setUserPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setUserName(request.getUserName());
        user.setUserNickname(request.getUserNickname());
        user.setUserPhoneNumber(request.getUserPhoneNumber());
        user = repository.addUser(user);

        PasswordAnswer answer = new PasswordAnswer();
        answer.setUserId(user.getUserId());
        answer.setQuestionId(request.getUserPasswordQuestionId());
        answer.setPasswordQuestionAnswer(request.getUserPasswordAnswer());


        passwordRepository.addAnswer(answer);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken,"User registration was successful");

    }

    @Override
    public AuthenticationResponse authenticate(User request) {
        log.info("AuthenticationService/authenticate");
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserEmail(),
                        request.getUserPassword()
                )
        );

        User user = repository.getUserImportantInfo(request.getUserEmail()).get();
        if(user.getUserWithdrawalStatus() == 1) {
            return new AuthenticationResponse(null, null, "Withdrawn User");
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(accessToken, refreshToken, user);
        tokenRepository.updateLogin(accessToken);

        return new AuthenticationResponse(accessToken, refreshToken, "User login was successful");
    }

    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getUserId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLogout(1);
        });

        tokenRepository.addAllTokens(validTokens);
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLogout(1);
        token.setUserId(user.getUserId());
        token.setTokenId(null);
        tokenRepository.addToken(token);
    }

    @Override
    public AuthenticationResponse refreshToken(String userEmail, String authHeader) {
        log.info("AuthenticationService/refreshToken");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new AuthenticationResponse(null, null, "Unauthorized Token");
        }

        String token = authHeader.substring(7);

        User user = repository.getPublicInfo(userEmail)
                .orElseThrow(()->new RuntimeException("No user found"));

        if(jwtService.isValidRefreshToken(token, user)) {
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return new AuthenticationResponse(accessToken, refreshToken, "New token generated");
        }

        return new AuthenticationResponse(null, null, "Unauthorized Token");
    }

}
