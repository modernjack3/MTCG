package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardPackage {
    @JsonProperty("packageId")
    private String packageId;

    @JsonProperty("buyerId")
    private String buyerId;  // Can be null if not yet purchased

    @JsonProperty("isSold")
    private boolean isSold;

    @JsonProperty("cards")
    private List<Card> cards;  // Should contain exactly 5 cards
}
