package br.com.pehenmo.springwebfluxessentials.service;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;
import br.com.pehenmo.springwebfluxessentials.repository.AnimeRepository;
import br.com.pehenmo.springwebfluxessentials.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepository;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void setUpBlockHound(){
        BlockHound.install();

    }

    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @BeforeEach
    public void setUp(){
        BDDMockito.when(animeRepository.findAll()).
                thenReturn(Flux.just(anime).log());

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeRepository.delete(ArgumentMatchers.any(Anime.class))).
                thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidUpdatedAnime())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved()))).
                thenReturn(Flux.just(anime, anime).log());

    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful(){
        StepVerifier.create(animeService.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns mono if anime when it exists")
    public void findById_ReturnMonoOfAnime_WhenSuccessful(){
        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns mono error when anime does not exist")
    public void findById_ReturnMonoOfError_WhenEmptyMonoIsReturned(){

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_createAnime_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete an anime when successful")
    public void delete_RemoveAnime_WhenSuccessful(){

        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete error when Anime does not exist")
    public void delete_RemoveMonoError_WhenEmptyAnimeMonoIsReturned(){

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("updated anime and return empty mono when successful")
    public void update_updateAnime_WhenSuccessful(){

        Anime animeUpdated = AnimeCreator.createValidUpdatedAnime();

        StepVerifier.create(animeService.update(animeUpdated))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("updated error when Anime does not exist")
    public void update_Error_WhenReturnsEmptyAnime(){

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        StepVerifier.create(animeService.update(AnimeCreator.createValidUpdatedAnime()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();

    }

    @Test
    @DisplayName("saveAll creates a list of animes when successful")
    public void saveAll_createListOfAnimes_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.saveAll(List.of(animeToBeSaved,animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll returns mono error when  one of the objetos list container invalid name")
    public void saveAll_ReturnsMonoError_WhenInvalidName(){

        BDDMockito.when(animeRepository.saveAll(ArgumentMatchers.anyIterable())).
                thenReturn(Flux.just(anime, anime.withName("")).log());

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.saveAll(List.of(animeToBeSaved,animeToBeSaved.withName(""))))
                .expectSubscription()
                .expectNext(anime)
                .expectError(ResponseStatusException.class)
                .verify();
    }

}