package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("maze")
    private ConfigMaze maze;

    public ConfigMaze getMaze() { return maze; }
    public void setMaze(ConfigMaze maze) { this.maze = maze; }
}