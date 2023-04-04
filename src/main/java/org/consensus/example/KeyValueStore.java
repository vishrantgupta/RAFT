package org.consensus.example;

import java.util.HashMap;
import java.util.Map;
import org.consensus.raft.ApplicationCallback;
import org.springframework.stereotype.Component;

@Component
public class KeyValueStore implements ApplicationCallback {

    private final Map<String, String> cache = new HashMap<>();

    @Override
    public void apply(Object o) {

        KeyValueModel command = (KeyValueModel) o;

        if (command == null || command.getCmd() == null || command.getCmd().isEmpty()) {
            System.out.println("command cannot be empty");
            return;
        }

        switch (command.getCmd().toLowerCase()) {
            case "set" -> {
                this.put(command.getKey(), command.getValue());
                System.out.println("ok");
            }
            case "get" -> {
                System.out.println(this.get(command.getKey()));
            }
            case "delete" -> {
                this.delete(command.getKey());
                System.out.println("deleted");
            }
            default -> System.out.println("invalid command");
        }
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
        System.out.println(cache);
    }
}
