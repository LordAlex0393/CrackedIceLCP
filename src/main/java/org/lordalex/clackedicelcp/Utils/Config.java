package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("show_time")
    private boolean showTime = false;

    @JsonProperty("mazes")
    private Map<String, ConfigMaze> mazes;

    @JsonProperty("platforms")
    private Map<String, ConfigPlatform> platforms;

    public boolean isShowTime() { return showTime; }
    public Map<String, ConfigMaze> getMazes() { return mazes; }

    // Тот самый геттер:
    public Map<String, ConfigPlatform> getPlatforms() { return platforms; }

    public void setMazes(Map<String, ConfigMaze> mazes) { this.mazes = mazes; }
    public void setPlatforms(Map<String, ConfigPlatform> platforms) { this.platforms = platforms; }
}