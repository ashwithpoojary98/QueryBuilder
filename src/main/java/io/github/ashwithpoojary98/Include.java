package io.github.ashwithpoojary98;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Include {

    private String name;

    private Query query;

    private String foreignKey;

    private String localKey;

    private boolean isMany;
}
