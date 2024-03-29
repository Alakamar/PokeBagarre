package com.montaury.pokebagarre.metier;

import com.montaury.pokebagarre.erreurs.ErreurMemePokemon;
import com.montaury.pokebagarre.erreurs.ErreurPokemonNonRenseigne;
import com.montaury.pokebagarre.webapi.PokeBuildApi;
import net.bytebuddy.implementation.bytecode.Throw;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static java.time.Duration.ofSeconds;

/*
Les différents tests à réaliser :
- Le pokémon 1 est vide
- Le pokémon 2 est vide
- Les deux pokémons sont vides
- Les deux pokémon sont saisis mais identiques
- Les deux pokémons sont saisis et non identiques
 */

class BagarreTest {
    PokeBuildApi fausseAPI;
    Bagarre bagarre;

    @BeforeEach
    void preparer() {
        fausseAPI = mock(PokeBuildApi.class);
        bagarre = new Bagarre(fausseAPI);
    }
    @Test
    void devrait_lever_une_erreur_si_le_premier_pokemon_est_null() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom(null))
                .thenReturn(CompletableFuture.failedFuture(new ErreurPokemonNonRenseigne("premier")));

        Throwable futurVainqueur = Assertions.catchThrowable(() -> bagarre.demarrer(null, "pikachu"));

        // THEN
        assertThat(futurVainqueur)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void devrait_lever_une_erreur_si_le_second_pokemon_est_null() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom(null))
                .thenReturn(CompletableFuture.failedFuture(new ErreurPokemonNonRenseigne("second")));

        Throwable futurVainqueur = Assertions.catchThrowable(() -> bagarre.demarrer("pikachu", null));

        // THEN
        assertThat(futurVainqueur)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");
    }

    @Test
    void devrait_lever_une_erreur_si_les_deux_pokemons_sont_null() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom(null))
                .thenReturn(CompletableFuture.failedFuture(new ErreurPokemonNonRenseigne("premier")));

        Throwable futurVainqueur = Assertions.catchThrowable(() -> bagarre.demarrer(null, null));

        // THEN
        assertThat(futurVainqueur)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void devrait_lever_une_erreur_si_les_deux_pokemons_sont_saisis_mais_identiques() {
        // GIVEN

        // WHEN
        Throwable erreur = Assertions.catchThrowable(() -> bagarre.demarrer("pikachu", "pikachu"));

        // THEN
        assertThat(erreur)
                .isInstanceOf(ErreurMemePokemon.class).hasMessage("Impossible de faire se bagarrer un pokemon avec lui-meme");
    }

    @Test
    void devrait_lever_une_erreur_si_le_premier_pokemon_est_vide() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom(""))
                .thenReturn(CompletableFuture.failedFuture(new ErreurPokemonNonRenseigne("premier")));

        Throwable futurVainqueur = Assertions.catchThrowable(() -> bagarre.demarrer("", "pikachu"));

        // THEN
        assertThat(futurVainqueur)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void devrait_lever_une_erreur_si_le_second_pokemon_est_vide() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom(""))
                .thenReturn(CompletableFuture.failedFuture(new ErreurPokemonNonRenseigne("second")));

        Throwable futurVainqueur = Assertions.catchThrowable(() -> bagarre.demarrer("pikachu", ""));

        // THEN
        assertThat(futurVainqueur)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le second pokemon n'est pas renseigne");
    }

    @Test
    void devrait_lever_une_erreur_si_les_deux_pokemons_sont_vides() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom(""))
                .thenReturn(CompletableFuture.failedFuture(new ErreurPokemonNonRenseigne("premier")));

        Throwable futurVainqueur = Assertions.catchThrowable(() -> bagarre.demarrer("", ""));

        // THEN
        assertThat(futurVainqueur)
                .isInstanceOf(ErreurPokemonNonRenseigne.class)
                .hasMessage("Le premier pokemon n'est pas renseigne");
    }

    @Test
    void premier_pokemon_devrait_etre_vainqueur_puisque_meilleure_attaque() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1",
                        new Stats(1, 2))));
        when(fausseAPI.recupererParNom("carapuce"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("carapuce", "url2",
                        new Stats(3, 4))));

        var futurVainqueur = bagarre.demarrer("carapuce", "pikachu");

        // THEN
        assertThat(futurVainqueur)
                .succeedsWithin(ofSeconds(2))
                .satisfies (pokemon -> {
                            assertThat(pokemon.getNom())
                                    .isEqualTo("carapuce");
                            assertThat(pokemon.getUrlImage())
                                    .isEqualTo("url2");
                            assertThat(pokemon.getStats().getAttaque())
                                    .isEqualTo(3);
                            assertThat(pokemon.getStats().getDefense())
                                    .isEqualTo(4);
                        }
                );
    }

    @Test
    void second_pokemon_devrait_etre_vainqueur_puisque_meilleure_attaque() {
        // GIVEN

        // WHEN
        when(fausseAPI.recupererParNom("pikachu"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("pikachu", "url1",
                        new Stats(1, 2))));
        when(fausseAPI.recupererParNom("carapuce"))
                .thenReturn(CompletableFuture.completedFuture(new Pokemon("carapuce", "url2",
                        new Stats(3, 4))));

        var futurVainqueur = bagarre.demarrer("pikachu", "carapuce");

        // THEN
        assertThat(futurVainqueur)
                .succeedsWithin(ofSeconds(2))
                .satisfies (pokemon -> {
                            assertThat(pokemon.getNom())
                                    .isEqualTo("carapuce");
                            assertThat(pokemon.getUrlImage())
                                    .isEqualTo("url2");
                            assertThat(pokemon.getStats().getAttaque())
                                    .isEqualTo(3);
                            assertThat(pokemon.getStats().getDefense())
                                    .isEqualTo(4);
                        }
                );
    }
}