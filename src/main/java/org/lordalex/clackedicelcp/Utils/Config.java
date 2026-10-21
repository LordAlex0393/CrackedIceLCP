package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("mazes")
    private Map<String, ConfigMaze> mazes;

    public Map<String, ConfigMaze> getMazes() { return mazes; }
    public void setMazes(Map<String, ConfigMaze> mazes) { this.mazes = mazes; }
}