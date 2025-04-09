package net.az3l1t.books;

import net.az3l1t.books.dto.AuthRequest;
import net.az3l1t.books.dto.RegisterRequest;
import net.az3l1t.books.exception.UserAlreadyExistsException;
import net.az3l1t.books.model.User;
import net.az3l1t.books.model.roles.Role;
import net.az3l1t.books.repository.UserRepository;
import net.az3l1t.books.service.AuthService;
import net.az3l1t.books.service.details.CustomUserDetailsService;
import net.az3l1t.books.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private AuthRequest authRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();

        authRequest = new AuthRequest("testuser", "password");
        registerRequest = new RegisterRequest("testuser", "password");
    }

    @Test
    void login_ReturnsToken_WhenCredentialsValid() {
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUser);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwtToken");

        String token = authService.login(authRequest);

        assertEquals("jwtToken", token);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(testUser);
    }

    @Test
    void register_SavesNewUser_WhenUsernameUnique() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = authService.register(registerRequest);

        assertEquals(testUser, result);
        assertEquals(Role.ROLE_USER, result.getRole());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerAdmin_SavesAdminUser_WhenUsernameUnique() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authService.registerAdmin(registerRequest);

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(argThat(user -> user.getRole() == Role.ROLE_ADMIN));
    }

    @Test
    void register_ThrowsException_WhenUsernameExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));
        assertEquals("Username already exists : testuser", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerAdmin_ThrowsException_WhenUsernameExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> authService.registerAdmin(registerRequest));
        assertEquals("Username already exists : testuser", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }
}