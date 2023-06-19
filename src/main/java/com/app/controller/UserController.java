package com.app.controller;

import com.app.Config.CustomUserDetails;
import com.app.Config.jwt.JwtUtils;
import com.app.entities.UserModel;
import com.app.repo.UserRepository;
import com.app.services.IUserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    IUserServices userServices;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;
   /*
    To Test if the DB is working
    @GetMapping("/test")
    @ResponseBody
    public String test(){
        User user=new User();
        user.setName("Test");
        user.setEmail("Test@test.in");
        user.setPassword("1234");

        userRepository.save(user);
        return "Working";
    }
    //Login

    */
    @GetMapping("/")
    public String Home(){
        return "home";
    }

    @GetMapping("/about")
    public ResponseEntity<String> adminUser(){
        return ResponseEntity.ok("I am Admin");
    }




    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserModel userDetails)
    {
        Map<String, String> response = new HashMap<>();
        try
        {    userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            if(userServices.addUser(userDetails))
            {
                response.put("Status","True");
                //response.put("SessionID","1234");
            }
        }
        catch(Exception e)
        {
            response.put("Status","Fail");
            response.put("Error",e.getMessage());
            return new ResponseEntity<Map<String, String>>(response, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<Map<String, String>>(response, HttpStatus.OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser( @RequestBody Map<String,String> loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.get("Username"), loginRequest.get("Password")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        UserModel response = userRepository.getUserByEmail(loginRequest.get("Username"));

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }
//Change the response

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("Message","You've been signed out!"));
    }





}
