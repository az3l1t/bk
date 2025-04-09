package net.az3l1t.books.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.az3l1t.books.dto.AuthRequest;
import net.az3l1t.books.dto.RegisterRequest;
import net.az3l1t.books.exception.UserAlreadyExistsException;
import net.az3l1t.books.model.User;
import net.az3l1t.books.model.roles.Role;
import net.az3l1t.books.repository.UserRepository;
import net.az3l1t.books.service.details.CustomUserDetailsService;
import net.az3l1t.books.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String login(AuthRequest authRequest) {
        log.debug("Login for username: {}", authRequest.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        log.info("User {} logged in successfully, token generated", authRequest.getUsername());
        return token;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        log.debug("Registering new user with username: {}", registerRequest.getUsername());
        return register(registerRequest, Role.ROLE_USER);
    }

    @Transactional
    public void registerAdmin(RegisterRequest registerRequest) {
        log.debug("Registering new admin with username: {}", registerRequest.getUsername());
        register(registerRequest, Role.ROLE_ADMIN);
    }

    private User register(RegisterRequest registerRequest, Role role) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            log.error("Username {} already exists", registerRequest.getUsername());
            throw new UserAlreadyExistsException("Username already exists : " + registerRequest.getUsername());
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .build();
        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully with role: {}", savedUser.getUsername(), role);
        return savedUser;
    }
}