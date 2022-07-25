package br.com.pehenmo.springwebfluxessentials.util;

import br.com.pehenmo.springwebfluxessentials.entity.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved(){
        return Anime.builder().name("Tensei Shitara").build();
    }

    public static Anime createValidAnime(){
        return Anime.builder().name("Tensei Shitara").id(1).build();
    }

    public static Anime createValidUpdatedAnime(){
        return Anime.builder().name("Tensei Shitara 2").id(1).build();
    }
}
