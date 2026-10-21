package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigLevel {
    @JsonProperty("name") private String name;
    @JsonProperty("spawn") private String spawn;
    @JsonProperty("max_lives") private int maxLives = 3;

    public String getName() { return name; }
    public String getSpawn() { return spawn; }
    public int getMaxLives() { return maxLives; }
}