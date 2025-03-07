package at.fhtw.mtg.util;

import at.fhtw.mtg.model.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BattleLogicTest {

    // Helper function to create a Card with the specified parameters.
    // The remaining fields (cardId, owner, inDeck, locked) are set to dummy values.
    private Card createCard(String name, int damage, String element, boolean isSpell) {
        return new Card("id-" + name, name, damage, element, isSpell, "owner", true, false);
    }

    // --- Tests for Special Rules ---

    @Test
    public void testWinsByDefault_DragonVsGoblin() {
        StringBuilder log = new StringBuilder();
        Card dragon = createCard("Dragon", 50, "fire", false);
        Card goblin = createCard("Goblin", 1000, "normal", false);
        boolean result = BattleLogic.winsByDefault(dragon, goblin, log);
        assertTrue(result, "Dragon should win against Goblin by default");
    }

    @Test
    public void testWinsByDefault_WizardVsOrk() {
        StringBuilder log = new StringBuilder();
        Card wizard = createCard("Wizard", 40, "normal", false);
        Card ork = createCard("Ork", 1000, "normal", false);
        boolean result = BattleLogic.winsByDefault(wizard, ork, log);
        assertTrue(result, "Wizard should win against Ork by default");
    }

    @Test
    public void testWinsByDefault_WaterSpellVsGoblin() {
        StringBuilder log = new StringBuilder();
        Card waterSpell = createCard("WaterSpell", 30, "water", true);
        Card goblin = createCard("Goblin", 1000, "normal", false);
        boolean result = BattleLogic.winsByDefault(waterSpell, goblin, log);
        assertTrue(result, "WaterSpell should win against Goblin by default");
    }

    @Test
    public void testWinsByDefault_KrakenVsSpell() {
        StringBuilder log = new StringBuilder();
        Card kraken = createCard("Kraken", 45, "water", false);
        Card spell = createCard("SomeSpell", 1000, "fire", true);
        boolean result = BattleLogic.winsByDefault(kraken, spell, log);
        assertTrue(result, "Kraken should win against any spell by default");
    }

    @Test
    public void testWinsByDefault_FireElfVsGoblin() {
        StringBuilder log = new StringBuilder();
        Card fireElf = createCard("FireElf", 30, "fire", false);
        Card goblin = createCard("Goblin", 1000, "normal", false);
        boolean result = BattleLogic.winsByDefault(fireElf, goblin, log);
        assertTrue(result, "FireElf should win against Goblin by default");
    }

    @Test
    public void testWinsByDefault_NonSpecialCase() {
        StringBuilder log = new StringBuilder();
        // When no special rule applies, winsByDefault should return false.
        Card goblin = createCard("Goblin", 30, "normal", false);
        Card dragon = createCard("Dragon", 50, "fire", false);
        // Goblin attacking Dragon should not trigger the special rule.
        boolean result = BattleLogic.winsByDefault(goblin, dragon, log);
        assertFalse(result, "Goblin should not win against Dragon by default");
    }

    // --- Tests for Elemental Modifiers ---

    @Test
    public void testApplyElemental_PureMonsterFight() {
        StringBuilder log = new StringBuilder();
        // In pure monster fights (non-spell cards), elemental modifiers are ignored.
        Card monster1 = createCard("MonsterA", 50, "normal", false);
        Card monster2 = createCard("MonsterB", 60, "fire", false);
        double dmg = BattleLogic.applyElemental(monster1, monster2, log);
        assertEquals(50, dmg, "Pure monster fight should return base damage");
    }

    @Test
    public void testApplyElemental_FireVsFire() {
        StringBuilder log = new StringBuilder();
        Card fireSpell1 = createCard("FireSpell1", 50, "fire", true);
        Card fireSpell2 = createCard("FireSpell2", 50, "fire", true);
        double dmg = BattleLogic.applyElemental(fireSpell1, fireSpell2, log);
        assertEquals(50, dmg, "Fire vs Fire should yield base damage");
    }

    @Test
    public void testApplyElemental_FireVsWater() {
        StringBuilder log = new StringBuilder();
        Card fireSpell = createCard("FireSpell", 50, "fire", true);
        Card waterSpell = createCard("WaterSpell", 50, "water", true);
        double dmg = BattleLogic.applyElemental(fireSpell, waterSpell, log);
        assertEquals(25, dmg, 0.0001, "Fire attacking Water should yield half damage");
    }

    @Test
    public void testApplyElemental_FireVsNormal() {
        StringBuilder log = new StringBuilder();
        Card fireSpell = createCard("FireSpell", 50, "fire", true);
        Card normalSpell = createCard("NormalSpell", 50, "normal", true);
        double dmg = BattleLogic.applyElemental(fireSpell, normalSpell, log);
        assertEquals(100, dmg, 0.0001, "Fire attacking Normal should yield double damage");
    }

    @Test
    public void testApplyElemental_WaterVsFire() {
        StringBuilder log = new StringBuilder();
        Card waterSpell = createCard("WaterSpell", 50, "water", true);
        Card fireSpell = createCard("FireSpell", 50, "fire", true);
        double dmg = BattleLogic.applyElemental(waterSpell, fireSpell, log);
        assertEquals(100, dmg, 0.0001, "Water attacking Fire should yield double damage");
    }

    @Test
    public void testApplyElemental_WaterVsNormal() {
        StringBuilder log = new StringBuilder();
        Card waterSpell = createCard("WaterSpell", 50, "water", true);
        Card normalSpell = createCard("NormalSpell", 50, "normal", true);
        double dmg = BattleLogic.applyElemental(waterSpell, normalSpell, log);
        assertEquals(25, dmg, 0.0001, "Water attacking Normal should yield half damage");
    }

    @Test
    public void testApplyElemental_NormalVsFire() {
        StringBuilder log = new StringBuilder();
        Card normalSpell = createCard("NormalSpell", 50, "normal", true);
        Card fireSpell = createCard("FireSpell", 50, "fire", true);
        double dmg = BattleLogic.applyElemental(normalSpell, fireSpell, log);
        assertEquals(25, dmg, 0.0001, "Normal attacking Fire should yield half damage");
    }

    @Test
    public void testApplyElemental_NormalVsWater() {
        StringBuilder log = new StringBuilder();
        Card normalSpell = createCard("NormalSpell", 50, "normal", true);
        Card waterSpell = createCard("WaterSpell", 50, "water", true);
        double dmg = BattleLogic.applyElemental(normalSpell, waterSpell, log);
        assertEquals(100, dmg, 0.0001, "Normal attacking Water should yield double damage");
    }

    @Test
    public void testApplyElemental_NormalVsNormal() {
        StringBuilder log = new StringBuilder();
        Card normalSpell1 = createCard("NormalSpell1", 50, "normal", true);
        Card normalSpell2 = createCard("NormalSpell2", 50, "normal", true);
        double dmg = BattleLogic.applyElemental(normalSpell1, normalSpell2, log);
        assertEquals(50, dmg, 0.0001, "Normal vs Normal should yield base damage");
    }

    // --- Tests for calculateRoundWinner ---

    @Test
    public void testCalculateRoundWinner_SpecialRule() {
        StringBuilder log = new StringBuilder();
        // Using Dragon vs Goblin which triggers the special rule.
        Card dragon = createCard("Dragon", 50, "fire", false);
        Card goblin = createCard("Goblin", 30, "normal", false);
        Boolean winner = BattleLogic.calculateRoundWinner(dragon, goblin, log);
        assertNotNull(winner, "Winner should not be null when a special rule applies");
        assertTrue(winner, "Dragon should win by special rule");
        assertTrue(log.toString().contains("slays"), "Log should indicate a special interaction");
    }

    @Test
    public void testCalculateRoundWinner_ElementalWin() {
        StringBuilder log = new StringBuilder();
        // No special rule applies so elemental damage modifiers decide.
        // For FireSpell vs NormalSpell:
        // FireSpell: 50 * 2 = 100; NormalSpell: 50 * 0.5 = 25.
        Card fireSpell = createCard("FireSpell", 50, "fire", true);
        Card normalSpell = createCard("NormalSpell", 50, "normal", true);
        Boolean winner = BattleLogic.calculateRoundWinner(fireSpell, normalSpell, log);
        assertNotNull(winner, "Winner should not be null");
        assertTrue(winner, "FireSpell should win due to elemental advantage");
        assertTrue(log.toString().contains("deals"), "Log should mention damage dealt");
    }

    @Test
    public void testCalculateRoundWinner_Draw() {
        StringBuilder log = new StringBuilder();
        // Two identical cards (no special rule and equal damage) should result in a draw.
        Card spell1 = createCard("NormalSpell1", 50, "normal", true);
        Card spell2 = createCard("NormalSpell2", 50, "normal", true);
        Boolean winner = BattleLogic.calculateRoundWinner(spell1, spell2, log);
        assertNull(winner, "Identical cards should result in a draw");
    }


}
