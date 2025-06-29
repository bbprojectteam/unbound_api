package com.badboys.unbound_chat.api.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestInviteDto {

    private Long chatRoomId;

    private String chatRoomName;

    private Long userId;
}
