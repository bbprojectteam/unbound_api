package com.badboys.unbound_service.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDto {

    private Long commentId;

    private String content;

    private int depth;

    private Long userId;

    private String username;

    private LocalDateTime updatedAt;

    private List<CommentDto> childList;
}
