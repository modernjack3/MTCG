package at.fhtw;

import at.fhtw.httpserver.server.Server;
import at.fhtw.httpserver.utils.Router;
import at.fhtw.mtg.dal.DatabaseConnection;
import at.fhtw.mtg.service.*;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Connection test = DatabaseConnection.getConnection();

        Server server = new Server(10001, configureRouter());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Router configureRouter()
    {
        Router router = new Router();
        router.addService("/users", new UserService());
        router.addService("/sessions", new SessionService());
        router.addService("/packages", new PackageCreationService());
        router.addService("/transactions", new PackageBuyService());
        router.addService("/cards", new CardService());
        router.addService("/deck", new DeckService());
        router.addService("/stats", new StatsService());
        router.addService("/scoreboard", new ScoreboardService());
        router.addService("/battles", new BattleService());
        router.addService("/tradings", new TradeService());
        //Special feature
        router.addService("/history", new HistoryService());
        return router;
    }
}
