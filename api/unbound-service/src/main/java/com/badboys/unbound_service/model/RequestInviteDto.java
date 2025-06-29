package com.badboys.unbound_service.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestInviteDto {

    private Long chatRoomId;

    private String chatRoomName;

    private Long userId;
}
