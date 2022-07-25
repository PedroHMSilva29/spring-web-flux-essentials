package br.com.pehenmo.springwebfluxessentials.service;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;
import br.com.pehenmo.springwebfluxessentials.repository.AnimeRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimeService {
    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(Integer id){
        return animeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found")));
    }

    public Mono<Anime> save(Anime anime){
        return animeRepository.save(anime);
    }

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        return animeRepository.saveAll(animes)
                .doOnNext(anime -> throwResponseStatusExceptionWhenEmptyName(anime));
    }

    private void throwResponseStatusExceptionWhenEmptyName(Anime anime){
        if(StringUtil.isNullOrEmpty(anime.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name");
        }
    }

    public Mono<Void> update(Anime anime){
        return findById(anime.getId())
                .flatMap(validAnime -> animeRepository.save(anime))
                .then();
    }

    public Mono<Void> delete(int id){
        return findById(id)
                .flatMap(animeRepository::delete)
                .then();
    }

}
