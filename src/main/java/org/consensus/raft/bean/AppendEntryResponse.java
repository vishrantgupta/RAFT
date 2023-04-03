package org.consensus.raft.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AppendEntryResponse extends RaftMessage {

    boolean success;

    private int matchIndex;

    // public String toString() {
    //     Gson gson = new Gson();
    //     return gson.toJson(this);
    // }
    //
    // public static AppendEntryResponse fromString(String str) {
    //     Gson gson = new Gson();
    //     return gson.fromJson(str, AppendEntryResponse.class);
    // }

}
