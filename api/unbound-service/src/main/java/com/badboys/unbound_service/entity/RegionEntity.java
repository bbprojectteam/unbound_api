package com.badboys.unbound_service.entity;

import com.badboys.unbound_service.model.RegionType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "region")
public class RegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegionType type; // 지역 타입

    @Column(nullable = false)
    private int level; // 계층 정보

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private RegionEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<RegionEntity> children = new ArrayList<>();

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEntity> users = new ArrayList<>(); 
}
