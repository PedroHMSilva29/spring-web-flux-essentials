package br.com.pehenmo.springwebfluxessentials.service;

import br.com.pehenmo.springwebfluxessentials.repository.UserSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserSystemUserDetailService implements ReactiveUserDetailsService {

    private final UserSystemRepository userSystemRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userSystemRepository.findByUsername(username)
                .cast(UserDetails.class);
    }
}
