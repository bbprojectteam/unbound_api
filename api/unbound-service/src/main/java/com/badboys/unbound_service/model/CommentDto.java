package com.badboys.unbound_service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "댓글 DTO")
public class CommentDto {

    @Schema(description = "댓글 ID", example = "123")
    private Long commentId;

    @Schema(description = "댓글 내용", example = "이 경기 정말 대단했어요!")
    private String content;

    @Schema(description = "댓글 깊이 (0 = 부모 댓글, 1 이상 = 대댓글)", example = "0")
    private int depth;

    @Schema(description = "댓글 작성자 ID", example = "1001")
    private Long userId;

    @Schema(description = "댓글 작성자 닉네임", example = "wukim")
    private String username;

    @Schema(description = "댓글 작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImage;

    @Schema(description = "댓글 수정 날짜", example = "2025-02-09T12:34:56")
    private LocalDateTime updatedAt;

    @Schema(description = "부모 댓글 ID", example = "123")
    private Long parentId;

    @Schema(description = "자식 댓글 리스트 (대댓글)")
    private List<CommentDto> childList;

    public CommentDto(Long commentId, String content, int depth,
                      Long userId, String username, String profileImage, LocalDateTime updatedAt, Long parentId) {
        this.commentId = commentId;
        this.content = content;
        this.depth = depth;
        this.userId = userId;
        this.username = username;
        this.profileImage = profileImage;
        this.updatedAt = updatedAt;
        this.parentId = parentId;
        this.childList = new ArrayList<>();
    }
}
