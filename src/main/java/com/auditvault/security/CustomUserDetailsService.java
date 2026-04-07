//package com.auditvault.security;
//
//import com.auditvault.model.User;
//import com.auditvault.repository.UserRepository;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.*;
//
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    public CustomUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String email)
//            throws UsernameNotFoundException {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() ->
//                        new UsernameNotFoundException("User not found"));
//
//        return new CustomUserDetails(
//                user.getId(),
//                user.getEmail(),
//                user.getPassword(),
//                List.of(new SimpleGrantedAuthority(
//                                "ROLE_" + user.getRole().name()
//                        )
//                ));
//
//    }
//
//}


package com.auditvault.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final com.auditvault.repository.UserRepository userRepository;

    public CustomUserDetailsService(com.auditvault.repository.UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        com.auditvault.model.User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        return new CustomUserDetails(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUserId(),
                user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().toString() // Assuming role is an Enum
                ))
        );
    }

}


