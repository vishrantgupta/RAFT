package org.consensus.example;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.consensus.raft.Consensus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class KeyValueController implements KeyValueApi {

    private final Consensus consensus;
    private final KeyValueStore store;

    @Autowired
    public KeyValueController(KeyValueStore store, Consensus consensus) {
        this.store = store;
        this.consensus = consensus;
    }

    // these are basic operation; the goal is to show how to get the consensus before applying to the key-value store
    
    @Override
    public ResponseEntity<String> delete(KeyValueModel model) {
        this.consensus.getConsensus(model);
        String response = this.store.apply(model);

        return ResponseEntity.of(Optional.of(response));
    }

    @Override
    public ResponseEntity<String> get(KeyValueModel model) {
        this.consensus.getConsensus(model);
        String response = this.store.apply(model);

        return ResponseEntity.of(Optional.of(response));
    }

    @Override
    public ResponseEntity<String> put(KeyValueModel model) {
        this.consensus.getConsensus(model);
        String response = this.store.apply(model);

        return ResponseEntity.of(Optional.of(response));
    }

}
