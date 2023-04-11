package org.consensus.example;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.ApplicationCallback;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KeyValueStore implements ApplicationCallback {

    private final Map<String, String> cache = new HashMap<>();

    @Override
    public String apply(Object o) {

        KeyValueModel command = (KeyValueModel) o;

        if (command == null || command.getCmd() == null || command.getCmd().isEmpty()) {
            log.warn("command cannot be empty");
            return "null";
        }

        switch (command.getCmd().toLowerCase()) {
            case "set" -> {
                this.put(command.getKey(), command.getValue());
            }
            case "get" -> {
                return this.get(command.getKey());
            }
            case "delete" -> {
                return this.delete(command.getKey());
            }
            default -> log.error("invalid command");
        }
        return "ok";
    }

    public String delete(String key) {

        if (cache.containsKey(key)) {
            return cache.remove(key);
        }
        return "null";
    }

    public String get(String key) {
        return cache.get(key);
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }
}
