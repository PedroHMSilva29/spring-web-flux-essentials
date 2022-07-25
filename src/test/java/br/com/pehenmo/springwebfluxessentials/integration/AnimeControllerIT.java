package br.com.pehenmo.springwebfluxessentials.integration;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;
import br.com.pehenmo.springwebfluxessentials.repository.AnimeRepository;
import br.com.pehenmo.springwebfluxessentials.util.AnimeCreator;
import br.com.pehenmo.springwebfluxessentials.util.WebTestClientUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
//@WebFluxTest
//@Import({AnimeService.class, CustomAttribute.class})
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    private final Anime anime = AnimeCreator.createValidAnime();

    @Autowired
    private WebTestClientUtil webTestClientUtil;

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient client;

    @Autowired
    private WebTestClient clientInvalid;

    private final String REGULAR_USER = "mario";
    private final String ADMIN_USER = "pehenmo";
    private final String INVALID_USER = "x";
    private final String INVALID_PASSWORD = "x";


    @BeforeEach
    public void setUp() {

        clientInvalid = webTestClientUtil.authenticateClient(INVALID_USER, INVALID_PASSWORD);

        BDDMockito.when(animeRepository.findAll()).
                thenReturn(Flux.just(anime).log());

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidUpdatedAnime())).
                thenReturn(Mono.just(anime).log());

        BDDMockito.when(animeRepository.delete(AnimeCreator.createValidAnime())).
                thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved()))).
                thenReturn(Flux.just(anime, anime).log());

    }

    @BeforeAll
    public static void setUpBlockHound() {
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

    @Test
    @DisplayName("findAll returns a flux of anime and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id")
                .isEqualTo(anime.getId())
                .jsonPath("$.[0].name")
                .isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findAll returns a flux of anime and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void findAll_ReturnFluxOfAnime_WhenSuccessfulAdmin() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id")
                .isEqualTo(anime.getId())
                .jsonPath("$.[0].name")
                .isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findAll returns Unauthorized when user is Unauthorized")
    public void findAll_ReturnUnauthorized_WhenUserIsInvalid() {
        clientInvalid
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("findAll Flavor2 returns a flux of anime and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void findAll_Flavor2_WhenSuccessful() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);

    }

    @Test
    @DisplayName("findById returns mono if anime when it exists and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void findByIdUser_ReturnMonoOfAnime_WhenSuccessful(){
        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns mono if anime when it exists and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void findByIdAdmin_ReturnMonoOfAnime_WhenSuccessful(){
        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById error returns mono if anime when it exists and user is unauthorized")
    public void findByIdAdmin_Unauthorized_WhenInvalidUser(){
        clientInvalid
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("findById returns mono error when anime does not exist and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void findById_ReturnMonoOfError_WhenEmptyMonoIsReturned(){

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developmentMessage").isEqualTo("a custom ResponseStatusException");

    }

    @Test
    @DisplayName("save creates an anime when successful and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void save_Forbidden_WhenUserIsNotAdmin(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        client
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("save creates an anime when successful and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void save_createAnime_WhenSuccessfulAdmin(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        client
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("save creates error an anime when user is Unauthorized")
    public void save_Unauthorized_WhenUserIsNotValid(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        clientInvalid
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("save return mono error when anime not valid and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void save_ErrorAnime_WhenAnimeNotValid(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        client
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

    }

    @Test
    @DisplayName("delete an anime when successful and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void delete_Forbidden_WhenUserIsNotAdmin(){
        client
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isForbidden();

    }

    @Test
    @DisplayName("delete an anime when successful and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void delete_RemoveAnime_WhenSuccessful(){
        client
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();

    }

    @Test
    @DisplayName("delete anime error when and user is Unauthorized")
    //@WithUserDetails(INVALID_USER)
    //@WithMockUser(username = INVALID_USER, password = INVALID_USER)
    public void delete_Unauthorized_WhenUserNotValid(){
        clientInvalid
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isUnauthorized();

    }

    @Test
    @DisplayName("delete error when Anime does not exist and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void delete_RemoveMonoError_WhenEmptyAnimeMonoIsReturned(){

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        client
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developmentMessage").isEqualTo("a custom ResponseStatusException");

    }

    @Test
    @DisplayName("updated anime and return empty mono when successful and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void update_Forbidden_WhenUserIsNotAdmin(){

        Anime animeUpdated = AnimeCreator.createValidUpdatedAnime();

        client
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeUpdated))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("updated anime and return empty mono when successful and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void update_updateAnime_WhenSuccessful(){

        Anime animeUpdated = AnimeCreator.createValidUpdatedAnime();

        client
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeUpdated))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("updated anime error and return empty mono when and user is unauthorized")
    public void update_Unauthorized_WhenUserIsNotValid(){

        Anime animeUpdated = AnimeCreator.createValidUpdatedAnime();

        clientInvalid
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeUpdated))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("updated error when Anime does not exist and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void update_Error_WhenReturnsEmptyAnime(){

        Anime animeUpdated = AnimeCreator.createValidUpdatedAnime();

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt())).
                thenReturn(Mono.empty());

        client
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeUpdated))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developmentMessage").isEqualTo("a custom ResponseStatusException");

    }

    @Test
    @DisplayName("saveAll creates a list of animes when successful and user is sucecessful authenticated and has role USER")
    @WithUserDetails(REGULAR_USER)
    public void saveAll_Forbidden_WhenUserIsNotValid(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved,animeToBeSaved)))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("saveAll creates a list of animes when successful and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void saveAll_createListOfAnimes_WhenSuccessful(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved,animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("saveAll error creates a list of animes when user is Unauthorized")
    public void saveAll_Unauthorized_WhenUserIsNotValid(){
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        clientInvalid
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved,animeToBeSaved)))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("saveAll returns mono error when  one of the objetos list container invalid name and user is sucecessful authenticated and has role ADMIN")
    @WithUserDetails(ADMIN_USER)
    public void saveAll_ReturnsMonoError_WhenInvalidName(){

        BDDMockito.when(animeRepository.saveAll(ArgumentMatchers.anyIterable())).
                thenReturn(Flux.just(anime, anime.withName("")).log());

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved,animeToBeSaved.withName(""))))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.developmentMessage").isEqualTo("a custom ResponseStatusException");

    }

}
