package at.fhtw.mtg.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data

public class UserCredentials {
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
