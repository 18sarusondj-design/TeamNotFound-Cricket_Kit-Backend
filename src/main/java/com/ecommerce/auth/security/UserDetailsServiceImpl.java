package com.ecommerce.auth.security;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Find user by either email or mobile number
        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByMobileNumber(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with identifier: " + identifier)));

        return UserDetailsImpl.build(user);
    }
}
