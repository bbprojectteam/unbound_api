package com.badboys.unbound_chat.api.model;


import lombok.Data;

import java.util.Set;

@Data
public class RequestCreateChatRoomDto {

    private Set<Long> userIdSet;

    private Set<Long> regionIdSet;
}
