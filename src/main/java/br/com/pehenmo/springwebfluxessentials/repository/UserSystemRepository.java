package br.com.pehenmo.springwebfluxessentials.repository;

import br.com.pehenmo.springwebfluxessentials.entity.UserSystem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserSystemRepository extends ReactiveCrudRepository<UserSystem, Integer> {

    Mono<UserSystem> findByUsername(String username);
}
