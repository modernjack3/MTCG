package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserStats {
    @JsonProperty("Username")
    private String username;

    @JsonProperty("Elo")
    private int elo;

    @JsonProperty("Wins")
    private int wins;

    @JsonProperty("Losses")
    private int losses;

    @JsonProperty("TotalGames")
    private int totalGames;

    @JsonProperty("WinRate")
    private double winRate;
}
