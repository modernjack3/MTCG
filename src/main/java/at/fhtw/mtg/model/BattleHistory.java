package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BattleHistory {
    @JsonProperty("battleId")
    private String battleId;

    @JsonProperty("outcome")
    private String outcome;

    @JsonProperty("opponentUserId")
    private String opponentUserId;

    @JsonProperty("eloChange")
    private int eloChange;

    @JsonProperty("createdAt")
    private String createdAt;
}