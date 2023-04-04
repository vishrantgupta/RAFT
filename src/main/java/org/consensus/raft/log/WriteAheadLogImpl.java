package org.consensus.raft.log;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WriteAheadLogImpl extends LinkedList<LogEntry> implements WriteAheadLog<LogEntry> {

    // appending first entry to the log
    {
        LogEntry firstEntry = new LogEntry(-1, null);
        this.add(firstEntry);
    }

    // public boolean appendEntries(int prevIndex, int prevTerm, List<LogEntry> entries, LogEntry... log) {
    //     assert log.size() > 0 : "append_entries on empty log";
    //     assert prevIndex >= 0 : "my programming error";
    //
    //     // The log is not allowed to have holes in it.  (Raft)
    //     if (prevIndex >= log.size()) {
    //         return false;
    //     }
    //
    //     // The terms have to match
    //     if (log.get(prevIndex).term != prevTerm) {
    //         return false;
    //     }
    //
    //     if (entries != null && !entries.isEmpty()) {
    //         int n = prevIndex + 1;
    //         for (LogEntry entry : entries) {
    //             if (n < log.size() && log.get(n).term != entry.term) {
    //                 log.subList(n, log.size()).clear();
    //                 break;
    //             }
    //             n++;
    //         }
    //         log.subList(prevIndex + 1, prevIndex + 1 + entries.size()).clear();
    //         log.addAll(prevIndex + 1, entries);
    //     }
    //
    //     List<LogEntry> sortedLog = new ArrayList<>(log);
    //     sortedLog.sort((e1, e2) -> Integer.compare(e1.term, e2.term));
    //     assert sortedLog.equals(log);
    //
    //     return true;
    // }


    @Override
    public boolean appendLogEntry(int leaderPreviousIndex, int leaderPreviousTerm, LogEntry... logEntries) {

        // the follower is lagging behind; and this condition makes sure there are no holes in the log
        if (leaderPreviousIndex >= this.size()) {
            return false;
        }

        // get append entry at index
        LogEntry lastCommittedEntry = get(leaderPreviousIndex);

        // the previous leader term is not same as the last committed entry leader term; possibly miss-match in log term after split-brain (check pg 70 of slides on Log Matching)
        if (lastCommittedEntry.getTerm() != leaderPreviousTerm) {
            // the log entry leader term is less than last committed log term
            return false;
        }

        if (logEntries != null && logEntries.length > 0) {

            int appendAtIndex = leaderPreviousIndex + 1;

            log.debug("appending at index " + appendAtIndex);

            // removing the number of entries that were in this append entry; this situation can occur when a duplicate message may arrive at a later stage; could be a result of slow network
            int entriesToRemove = logEntries.length - appendAtIndex - 1;

            ListIterator<LogEntry> itr = this.listIterator(appendAtIndex);
            while (entriesToRemove != 0 && itr.hasNext()) {

                itr.next(); // calling next to fix IllegalStateException

                itr.remove();
                entriesToRemove--;
            }

            // append
            return this.addAll(appendAtIndex, List.of(logEntries));
        }

        // // if (logEntries != null && logEntries.length > 0) {
        // //
        // //     int appendAtIndex = leaderPreviousIndex + 1;
        // //
        // //     for (LogEntry entry : logEntries) {
        // //         if (appendAtIndex < this.size() && this.get(appendAtIndex).getTerm() != entry.getTerm()) {
        // //             this.subList(appendAtIndex, this.size()).clear();
        // //             break;
        // //         }
        // //         appendAtIndex++;
        // //     }
        // //     this.subList(leaderPreviousIndex + 1, leaderPreviousIndex + 1 + logEntries.length).clear();
        // //     this.addAll(leaderPreviousIndex + 1, List.of(logEntries));
        // // }

        // the heart beat could send empty log entries; in that case just return true
        return true;
    }

}
