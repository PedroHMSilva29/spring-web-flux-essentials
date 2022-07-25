package br.com.pehenmo.springwebfluxessentials.controller;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;
import br.com.pehenmo.springwebfluxessentials.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/animes")
@Slf4j
@SecurityScheme(
        name="Basic Authentication",
        type= SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class AnimeController {

    private final AnimeService animeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "list all animes", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Flux<Anime> listAll(){
        return animeService.findAll();
    }

    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "find animes by id", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Anime> findById(@PathVariable Integer id){
        return animeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "save anime", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Anime> save(@Valid @RequestBody Anime anime){
        return animeService.save(anime);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "save batch", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Flux<Anime> saveBatch(@RequestBody List<Anime> animes){
        return animeService.saveAll(animes);
    }

    @PutMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "update anime", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Void> update(@PathVariable int id, @Valid @RequestBody Anime anime){
        return animeService.update(anime.withId(id));
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "delete anime", tags = {"anime"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Void> delete(@PathVariable  int id){
        return animeService.delete(id);
    }

}
