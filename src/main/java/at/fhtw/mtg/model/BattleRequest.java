package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BattleRequest {
    @JsonProperty("userId1")
    private String userId1;

    @JsonProperty("userId2")
    private String userId2;
}
