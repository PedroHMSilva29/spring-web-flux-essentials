package br.com.pehenmo.springwebfluxessentials.controller;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;
import br.com.pehenmo.springwebfluxessentials.service.AnimeService;
import br.com.pehenmo.springwebfluxessentials.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
class AnimeControllerTest {

    @Mock
    private AnimeService animeService;

    @InjectMocks
    private AnimeController animeController;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void setUpBlockHound(){
        BlockHound.install();

    }

    @BeforeEach
    public void setUp(){
        BDDMockito.when(animeService.findAll()).
                thenReturn(Flux.just(anime).log());

        BDDMockito.when(animeService.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeService.save(AnimeCreator.createAnimeToBeSaved())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeService.delete(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        BDDMockito.when(animeService.update(AnimeCreator.createValidUpdatedAnime())).
                thenReturn(Mono.empty());

        BDDMockito.when(animeService.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved()))).
                thenReturn(Flux.just(anime, anime).log());

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

    @Test
    @DisplayName("listAll returns a flux of anime")
    public void listAll_ReturnFluxOfAnime_WhenSuccessful(){
        StepVerifier.create(animeController.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns mono if anime when it exists")
    public void findById_ReturnMonoOfAnime_WhenSuccessful(){
        StepVerifier.create(animeController.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_createAnime_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeController.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete an anime when successful")
    public void delete_RemoveAnime_WhenSuccessful(){

        StepVerifier.create(animeController.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("updated anime and return empty mono when successful")
    public void update_updateAnime_WhenSuccessful(){

        Anime animeUpdated = AnimeCreator.createValidUpdatedAnime();

        StepVerifier.create(animeController.update(1, animeUpdated))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("saveBatch creates a list of animes when successful")
    public void saveBatch_createListOfAnimes_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeController.saveBatch(List.of(animeToBeSaved,animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

}