package com.badboys.unbound_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class Region {

    private Long id;

    private String name;

    private RegionType type;

    private int level;
}
