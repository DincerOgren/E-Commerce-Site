package org.example.project.controller;

import jakarta.validation.Valid;
import org.example.project.model.AppRole;
import org.example.project.model.Role;
import org.example.project.model.User;
import org.example.project.payload.APIResponse;
import org.example.project.repositories.RoleRepository;
import org.example.project.repositories.UserRepository;
import org.example.project.security.jwt.JwtUtils;
import org.example.project.security.request.LoginRequest;
import org.example.project.security.request.SignUpRequest;
import org.example.project.security.response.UserInfoResponse;
import org.example.project.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword()));

        }catch (Exception e){
            return new ResponseEntity<>("Bad credentials",HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),
                roles);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {

        if(userRepository.existsByEmail(signUpRequest.getEmail())){
            return new ResponseEntity<>(
                    new APIResponse("User with email "+signUpRequest.getEmail()+" already exist.",false),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByUserName(signUpRequest.getUsername())){
            return new ResponseEntity<>(
                    new APIResponse("User with username "+signUpRequest.getUsername()+" already exist.",false),
                    HttpStatus.BAD_REQUEST);
        }


        //Create a new user
        User user = new User(signUpRequest.getUsername(),signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles==null || strRoles.isEmpty()){
            Role role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

            roles.add(role);
        }
        else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: role not found."));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: role not found."));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role defRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: role not found."));
                        roles.add(defRole);
                        break;
                }
            });

        }

        user.setRoles(roles);
        userRepository.save(user);
        return new ResponseEntity<>(new APIResponse("User registered successfully.",true), HttpStatus.OK);
    }


    @PostMapping("signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie emptyCookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header
                (HttpHeaders.SET_COOKIE, emptyCookie.toString())
                .body(new APIResponse("You have been signed out.",true));
    }


    @GetMapping("/username")
    public String currentUserName(Authentication authentication) {

        if (authentication == null) {
            return "NULL";
        }


        return authentication.getName();

    }

    @GetMapping("user")
    public ResponseEntity<?> getUserDetails(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>("NULL", HttpStatus.UNAUTHORIZED);
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),
                roles);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
