package br.com.pehenmo.springwebfluxessentials.repository;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AnimeRepository extends ReactiveCrudRepository<Anime, Integer> {

    Mono<Anime> findById(Integer id);
}
