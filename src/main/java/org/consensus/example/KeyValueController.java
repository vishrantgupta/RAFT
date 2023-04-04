package org.consensus.example;

import java.util.Optional;
import org.consensus.raft.Consensus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KeyValueController implements KeyValueApi {

    private final Consensus consensus;
    private final KeyValueStore store;

    @Autowired
    public KeyValueController(KeyValueStore store, Consensus consensus) {
        this.store = store;
        this.consensus = consensus;
    }

    @Override
    public ResponseEntity<String> createStream(KeyValueModel body) {
        this.consensus.getConsensus(body);
        this.store.apply(body);

        return ResponseEntity.of(Optional.of("ok"));
    }
}
