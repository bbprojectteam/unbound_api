package com.badboys.unbound_chat.api.model;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sequence {
    @Id
    private String id;
    private long value;
}
