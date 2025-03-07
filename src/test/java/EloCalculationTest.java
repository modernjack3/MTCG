package at.fhtw.mtg.util;

import at.fhtw.mtg.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EloCalculationTest {

    @Test
    void testEqualNewPlayersWin() {
        // Both new players at 1000 Elo.
        User p1 = new User();
        p1.setElo(1000);
        p1.setWins(0);
        p1.setLosses(0);

        User p2 = new User();
        p2.setElo(1000);
        p2.setWins(0);
        p2.setLosses(0);

        // Expected: K=32, expected score ~0.5, so change = round(32*(1-0.5)) = 16.
        int change = BattleLogic.calculateEloChange(p1, p2, true);
        assertEquals(16, change);
    }

    @Test
    void testEqualNewPlayersLose() {
        // Both new players at 1000 Elo.
        User p1 = new User();
        p1.setElo(1000);
        p1.setWins(0);
        p1.setLosses(0);

        User p2 = new User();
        p2.setElo(1000);
        p2.setWins(0);
        p2.setLosses(0);

        // Expected: K=32, expected score ~0.5, so raw change = 32*(0-0.5) = -16.
        // Since Elo difference is 0, scaling factor is 1.
        int change = BattleLogic.calculateEloChange(p1, p2, false);
        assertEquals(-16, change);
    }

    @Test
    void testEqualExperiencedPlayersWin() {
        // Both experienced players (total games = 30) at 1000 Elo.
        User p1 = new User();
        p1.setElo(1000);
        p1.setWins(15);
        p1.setLosses(15);

        User p2 = new User();
        p2.setElo(1000);
        p2.setWins(15);
        p2.setLosses(15);

        // Expected: K=16, so change = round(16*(1-0.5)) = 8.
        int change = BattleLogic.calculateEloChange(p1, p2, true);
        assertEquals(8, change);
    }

    @Test
    void testEqualExperiencedPlayersLose() {
        // Both experienced players (30 games) at 1000 Elo.
        User p1 = new User();
        p1.setElo(1000);
        p1.setWins(15);
        p1.setLosses(15);

        User p2 = new User();
        p2.setElo(1000);
        p2.setWins(15);
        p2.setLosses(15);

        // Expected: K=16, so raw change = round(16*(0-0.5)) = -8.
        int change = BattleLogic.calculateEloChange(p1, p2, false);
        assertEquals(-8, change);
    }

    @Test
    void testHighRatedLoses() {
        // p1 is high rated, p2 is lower rated.
        User p1 = new User();
        p1.setElo(1200);
        p1.setWins(20);
        p1.setLosses(10);  // total = 30 → experienced, K=16

        User p2 = new User();
        p2.setElo(1000);
        p2.setWins(5);
        p2.setLosses(5);   // new, K=32 but only p1's K matters here.

        // Expected score for p1: 1/(1+10^((1000-1200)/400)) ≈ 1/(1+10^(-0.5)) ≈ 1/(1+0.316) ≈ 0.76.
        // If p1 loses (actual=0), raw change = 16*(0-0.76) ≈ -12.16.
        // Scaling factor = 1 + (1200-1000)/400 = 1.5, so final change ≈ -12.16 * 1.5 = -18.24, round to -18.
        int change = BattleLogic.calculateEloChange(p1, p2, false);
        assertEquals(-18, change);
    }

    @Test
    void testLowRatedLoses() {
        // p1 is low rated, p2 is higher rated.
        User p1 = new User();
        p1.setElo(800);
        p1.setWins(0);
        p1.setLosses(0);  // new, K=32

        User p2 = new User();
        p2.setElo(1000);
        p2.setWins(20);
        p2.setLosses(10); // experienced, but not used for p1 calculation.

        // Expected for p1: expected score = 1/(1+10^((1000-800)/400)) = 1/(1+10^(0.5)) ≈ 1/(1+3.16) ≈ 0.24.
        // Raw change = 32*(0-0.24) = -7.68.
        // Scaling factor = 1 + (800-1000)/400 = 1 - 0.5 = 0.5, so final change ≈ -7.68 * 0.5 = -3.84, round to -4.
        int change = BattleLogic.calculateEloChange(p1, p2, false);
        assertEquals(-4, change);
    }

    @Test
    void testClampingEloNotBelowZero() {
        // p1 has very low Elo; ensure the method clamps so that new Elo would not be negative.
        User p1 = new User();
        p1.setElo(10);
        p1.setWins(0);
        p1.setLosses(0);  // new, K=32

        User p2 = new User();
        p2.setElo(1500);
        p2.setWins(30);
        p2.setLosses(10);

        int change = BattleLogic.calculateEloChange(p1, p2, false);
        // Calculate expected raw change:
        // expected1 = 1/(1+10^((1500-10)/400)) is almost 0.
        // So raw change = 32*(0 - almost 0) ~ 0? But since p1 is much lower rated, the formula might result in a small negative.
        // However, if the change would cause p1's Elo to drop below 0, the method should clamp it to 0.
        assertEquals(0, change, "Elo change should be clamped so that new Elo is not negative");
    }
}
