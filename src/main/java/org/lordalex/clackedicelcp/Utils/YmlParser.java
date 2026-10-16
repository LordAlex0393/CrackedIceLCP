package org.lordalex.clackedicelcp.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

public class YmlParser {
    public static Config parseMapConfig(File file) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(file, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Location parseLocation(World world, String position) {
        String[] coordinates = position.split(", ");
        double X = Double.parseDouble(coordinates[0]);
        double Y = Double.parseDouble(coordinates[1]);
        double Z = Double.parseDouble(coordinates[2]);
        return new Location(world, X, Y, Z);
    }
}