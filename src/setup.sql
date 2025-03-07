DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

CREATE TABLE users (
                       user_id VARCHAR(36) PRIMARY KEY,
                       username        VARCHAR(50) UNIQUE NOT NULL,
                       password        VARCHAR(50) NOT NULL,
                       coins           INT NOT NULL DEFAULT 20,
                       elo             INT NOT NULL DEFAULT 1000,
                       games_played    INT NOT NULL DEFAULT 0,
                       wins            INT NOT NULL DEFAULT 0,
                       losses          INT NOT NULL DEFAULT 0,
                       name          VARCHAR(100) NOT NULL DEFAULT 'Player',
                       bio           TEXT NOT NULL DEFAULT 'I am new here!',
                       image         TEXT NOT NULL DEFAULT '<.<'
);

CREATE TABLE cards (
                       card_id VARCHAR(36) PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       damage INT NOT NULL,
                       element_type VARCHAR(10) NOT NULL,  -- 'fire', 'water', 'normal'
                       spell BOOLEAN NOT NULL,     -- 'monster', 'spell'
                       owner_id VARCHAR(36) REFERENCES users(user_id),
                       in_deck BOOLEAN NOT NULL DEFAULT FALSE,
                       locked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE packages (
                          package_id VARCHAR(36) PRIMARY KEY,
                          created_at timestamp NOT NULL DEFAULT current_timestamp,
                          buyer_id VARCHAR(36) REFERENCES users(user_id),
                          is_sold BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE package_cards (
                               package_id VARCHAR(36) NOT NULL REFERENCES packages(package_id),
                               card_id VARCHAR(36) NOT NULL REFERENCES cards(card_id),
                               PRIMARY KEY (package_id, card_id)
);

CREATE TABLE trades (
                        trade_id VARCHAR(36) PRIMARY KEY,
                        seller_id VARCHAR(36) NOT NULL REFERENCES users(user_id),
                        offered_card_id VARCHAR(36) NOT NULL REFERENCES cards(card_id),
                        required_type VARCHAR(10) NOT NULL,  -- 'monster' or 'spell'
                        required_element VARCHAR(10),
                        required_min_damage INT DEFAULT 0,
                        is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE decks (
                       user_id VARCHAR(36) PRIMARY KEY REFERENCES users(user_id),
                       card1_id VARCHAR(36) REFERENCES cards(card_id),
                       card2_id VARCHAR(36) REFERENCES cards(card_id),
                       card3_id VARCHAR(36) REFERENCES cards(card_id),
                       card4_id VARCHAR(36) REFERENCES cards(card_id)
);

CREATE TABLE battles (
                         battle_id VARCHAR(36) PRIMARY KEY,
                         waiting_user_id VARCHAR(36) REFERENCES users(user_id),
                         opponent_user_id VARCHAR(36) REFERENCES users(user_id),
                         winner_user_id VARCHAR(36) REFERENCES users(user_id),
                         battle_log TEXT NOT NULL,
                         waiting_elo_change INT,
                         opponent_elo_change INT,
                         polled BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);