package com.badboys.unbound_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberDto {

    private Long userId;

    private String username;

    private String profileImage;

    private int mmr;

    private String lastReadMessageId;
}
