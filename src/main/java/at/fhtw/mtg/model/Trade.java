package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Trade {
    @JsonProperty("tradeId")
    private String tradeId;

    @JsonProperty("sellerId")
    private String sellerId;

    @JsonProperty("offeredCardId")
    private String offeredCardId;

    @JsonProperty("requiredType")
    private String requiredType;     // "monster" or "spell"

    @JsonProperty("requiredElement")
    private String requiredElement;  // Can be null if not required

    @JsonProperty("requiredMinDamage")
    private int requiredMinDamage;

    @JsonProperty("isActive")
    private boolean isActive;
}
