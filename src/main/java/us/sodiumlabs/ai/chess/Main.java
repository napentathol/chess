package us.sodiumlabs.ai.chess;

import com.fasterxml.jackson.databind.ObjectMapper;
import us.sodiumlabs.ai.chess.service.UserService;

public class Main {
    public static void main(String[] args) {
        final ObjectMapper objectMapper = new ObjectMapper();

        final UserService userService = new UserService(objectMapper);
        userService.initialize();
    }
}
