package ru.flux.flux.messenger.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.flux.flux.messenger.User;
import ru.flux.flux.messenger.dto.JwtAuthenticationResponse;
import ru.flux.flux.messenger.dto.RefreshTokenRequest;
import ru.flux.flux.messenger.dto.SignInRequest;
import ru.flux.flux.messenger.dto.SignUpRequest;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        var user = userService.registerUser(request, passwordEncoder.encode(request.getPassword()));
        return buildResponse(user);
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
        );

        var user = (User) userService.userDetailsService().loadUserByUsername(request.getPhone());

        return buildResponse(user);
    }

    public JwtAuthenticationResponse refresh(RefreshTokenRequest request) {
        String phone = jwtService.extractSubject(request.getRefreshToken());
        var user = (User) userService.userDetailsService().loadUserByUsername(phone);
        return buildResponse(user);
    }

    private JwtAuthenticationResponse buildResponse(User user) {
        return JwtAuthenticationResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .userId(user.getId().toString())
                .build();
    }
}
