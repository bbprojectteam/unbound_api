package com.badboys.unbound_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comment")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchInfoId", nullable = false)
    private MatchInfoEntity matchInfo;

    @Column(nullable = false)
    private Long userId;  // 댓글 작성자 ID

    @Column(nullable = false)
    private String content;  // 댓글 내용

    @Column(nullable = false)
    private int depth;  // 댓글 깊이 (대댓글 여부)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent;  // 부모 댓글-

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> childList = new ArrayList<>();  // 자식 댓글 리스트

    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 마지막 수정 시간

    /**
     * 엔티티 생성 시 `updatedAt`을 현재 시간으로 설정
     */
    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 댓글 내용 수정 (수정 시 updatedAt 변경)
     */
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 대댓글 추가
     */
    public void addChildComment(CommentEntity child) {
        this.childList.add(child);
    }

    /**
     * 부모 설정
     */
    public void setParentComment(CommentEntity parent) {
        this.parent = parent;
        this.depth = parent.getDepth() + 1;
    }
}
