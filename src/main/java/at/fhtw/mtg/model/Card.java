package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    @JsonProperty("id")
    private String cardId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("damage")
    private int damage;

    @JsonProperty("element")
    private String elementType; // "fire", "water", "normal"

    @JsonProperty("isSpell")
    private boolean isSpell;    // true for spell cards, false for monster cards

    @JsonProperty("ownerId")
    private String ownerId;

    @JsonProperty("inDeck")
    private boolean inDeck;

    @JsonProperty("locked")
    private boolean locked;

    @Override
    public String toString() {
        return "Name: " + this.name + " | Element: " + this.elementType + " | Spellcard: " + this.isSpell + " | Damage: " + this.damage;
    }
}
