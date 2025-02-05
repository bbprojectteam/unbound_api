package com.badboys.unbound_chat.api.model;

import lombok.Getter;

import java.util.List;

@Getter
public class RequestCreateChatRoomDto {

    private List<String> userIdList;
}
