package com.badboys.unbound_service.entity;

import com.badboys.unbound_service.model.RegionType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "region")
public class RegionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private RegionEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<RegionEntity> children = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegionType type; // 지역 타입

    @Column(nullable = false)
    private int level; // 계층 정보

    public List<RegionEntity> getChildren() {
        return children;
    }

}
