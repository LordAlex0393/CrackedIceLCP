package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigPlatform {
    @JsonProperty("type") private String type;
    @JsonProperty("world") private String world;
    @JsonProperty("pos1") private String pos1;
    @JsonProperty("pos2") private String pos2;
    @JsonProperty("move_to") private String moveTo;
    @JsonProperty("speed_ticks") private int speedTicks = 10;
    @JsonProperty("time_on") private int timeOn = 100;
    @JsonProperty("time_off") private int timeOff = 100;

    public String getType() { return type; }
    public String getWorld() { return world; }
    public String getPos1() { return pos1; }
    public String getPos2() { return pos2; }
    public String getMoveTo() { return moveTo; }
    public int getSpeedTicks() { return speedTicks; }
    public int getTimeOn() { return timeOn; }
    public int getTimeOff() { return timeOff; }
}