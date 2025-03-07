package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BattleResult {
    @JsonProperty("battleId")
    private String battleId;

    @JsonProperty("waitingUserId")
    private String waitingUserId;

    @JsonProperty("opponentUserId")
    private String opponentUserId;

    @JsonProperty("winnerUserId")
    private String winnerUserId;

    @JsonProperty("battleLog")
    private String battleLog;

    @JsonProperty("waitingEloChange")
    private int waitingEloChange;

    @JsonProperty("opponentEloChange")
    private int opponentEloChange;

    @JsonProperty("createdAt")
    private String createdAt;
}
