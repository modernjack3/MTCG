package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    @JsonProperty("userId")
    private String userId;  // Primary key, matches the user's ID

    @JsonProperty("card1Id")
    private String card1Id;

    @JsonProperty("card2Id")
    private String card2Id;

    @JsonProperty("card3Id")
    private String card3Id;

    @JsonProperty("card4Id")
    private String card4Id;
}
