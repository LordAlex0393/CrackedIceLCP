package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigMaze {

    @JsonProperty("world")       private String world;
    @JsonProperty("corner1")     private String corner1;
    @JsonProperty("corner2")     private String corner2;
    @JsonProperty("corner3")     private String corner3;
    @JsonProperty("corner4")     private String corner4;
    @JsonProperty("height")      private int height = 1;
    @JsonProperty("start")       private String start;
    @JsonProperty("fail_message")   private String failMessage;
    @JsonProperty("finish_message") private String finishMessage;

    public String getWorld() { return world; }
    public String getCorner1() { return corner1; }
    public String getCorner2() { return corner2; }
    public String getCorner3() { return corner3; }
    public String getCorner4() { return corner4; }
    public int getHeight() { return height; }
    public String getStart() { return start; }
    public String getFailMessage() { return failMessage; }
    public String getFinishMessage() { return finishMessage; }
}