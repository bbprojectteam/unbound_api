package com.badboys.unbound_chat.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadCount {
    private Map<Long, Integer> readCounts; // msgId → 몇 명 읽음
}
