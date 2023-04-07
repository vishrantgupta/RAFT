package org.consensus.raft.log;

import java.util.Deque;
import java.util.List;

public interface WriteAheadLog<T> extends List<T>, Deque<T> {

    boolean appendLogEntry(int leaderPreviousIndex, int leaderPreviousTerm, List<LogEntry> logEntries);
}
