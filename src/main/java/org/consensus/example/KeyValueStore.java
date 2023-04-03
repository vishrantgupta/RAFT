package org.consensus.example;

import org.consensus.raft.ApplicationCallback;
import org.springframework.stereotype.Component;

@Component
public class KeyValueStore implements ApplicationCallback {

    @Override
    public void apply(Object command) {
        System.out.println("applying " + command);
    }
}
