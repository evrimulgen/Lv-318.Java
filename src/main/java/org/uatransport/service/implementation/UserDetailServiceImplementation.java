package org.uatransport.service.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.uatransport.entity.User;
import org.uatransport.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImplementation implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

            final User user = userRepository.findByEmail(email);

            if (user == null) {
                throw new UsernameNotFoundException("User '" + email + "' not found");
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(email)
                    .password(user.getPassword())
                    .authorities(user.getRole())
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
    }
}
