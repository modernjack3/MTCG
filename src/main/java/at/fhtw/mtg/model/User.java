package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("coins")
    private int coins = 20;

    @JsonProperty("elo")
    private int elo = 1000;

    @JsonProperty("gamesPlayed")
    private int gamesPlayed = 0;

    @JsonProperty("wins")
    private int wins = 0;

    @JsonProperty("losses")
    private int losses = 0;

    @JsonProperty("name")
    private String name = "Player";

    @JsonProperty("bio")
    private String bio = "I am new here!";

    @JsonProperty("image")
    private String image = "<.<";


}
