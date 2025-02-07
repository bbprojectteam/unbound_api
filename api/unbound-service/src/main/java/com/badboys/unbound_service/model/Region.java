package com.badboys.unbound_service.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Region implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private RegionType type;
    private int level;
    private Long parentId;
}
