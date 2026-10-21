package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigSnowman {
    @JsonProperty("world") private String world;
    @JsonProperty("location") private String location;
    @JsonProperty("fire_rate_ticks") private int fireRateTicks = 40;
    @JsonProperty("range") private int range = 15;

    public String getWorld() { return world; }
    public String getLocation() { return location; }
    public int getFireRateTicks() { return fireRateTicks; }
    public int getRange() { return range; }
}