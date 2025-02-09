package com.badboys.unbound_service.model;

import lombok.Data;
import lombok.Getter;

@Data
public class RequestUpdateCommentDto {

    private Long commentId;

    private Long matchInfoId;

    private Long parentId;

    private int depth;

    private String content;
}
