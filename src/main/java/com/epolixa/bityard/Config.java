package com.epolixa.bityard;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Config {
    private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("bityard.json");
    private int motdMinX;
    private int motdMaxX;
    private int motdMinY;
    private int motdMaxY;
    private int motdMinZ;
    private int motdMaxZ;
    private int spawnGatewayX;
    private int spawnGatewayY;
    private int spawnGatewayZ;
    private int spawnGatewayExitX;
    private int spawnGatewayExitY;
    private int spawnGatewayExitZ;
    private int returnGatewayX;
    private int returnGatewayY;
    private int returnGatewayZ;

    public Config() {
        JsonObject json;
        try {
            json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
            loadFromJson(json);
        } catch (IOException e) {
            // Create default
            try {
                Files.copy(Objects.requireNonNull(Config.class.getResourceAsStream("/data/bityard_fabric/files/default_config.json")), path);
                json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
                loadFromJson(json);
                Bityard.LOG.fatal("Unable to load config file for Bityard");
                Bityard.LOG.fatal("Please fill out the config file for Bityard, found in config/bityard.json");
            } catch (IOException ioException) {
                ioException.printStackTrace();
                Bityard.LOG.fatal("Unable to create default config");
            }
        }
    }

    private void loadFromJson(JsonObject json) {
        motdMinX = json.get("motd_x_min").getAsInt();
        motdMaxX = json.get("motd_x_max").getAsInt();
        motdMinY = json.get("motd_y_min").getAsInt();
        motdMaxY = json.get("motd_y_max").getAsInt();
        motdMinZ = json.get("motd_z_min").getAsInt();
        motdMaxZ = json.get("motd_z_max").getAsInt();
        spawnGatewayX = json.get("spawn_gateway_x").getAsInt();
        spawnGatewayY = json.get("spawn_gateway_y").getAsInt();
        spawnGatewayZ = json.get("spawn_gateway_z").getAsInt();
        spawnGatewayExitX = json.get("spawn_gateway_exit_x").getAsInt();
        spawnGatewayExitY = json.get("spawn_gateway_exit_y").getAsInt();
        spawnGatewayExitZ = json.get("spawn_gateway_exit_z").getAsInt();
        returnGatewayX = json.get("return_gateway_x").getAsInt();
        returnGatewayY = json.get("return_gateway_x").getAsInt();
        returnGatewayZ = json.get("return_gateway_x").getAsInt();
    }

    public int getMotdMinX() {
        return this.motdMinX;
    }

    public void setMotdMinX(int motdMinX) {
        this.motdMinX = motdMinX;
    }

    public int getMotdMaxX() {
        return motdMaxX;
    }

    public void setMotdMaxX(int motdMaxX) {
        this.motdMaxX = motdMaxX;
    }

    public int getMotdMinY() {
        return motdMinY;
    }

    public void setMotdMinY(int motdMinY) {
        this.motdMinY = motdMinY;
    }

    public int getMotdMaxY() {
        return motdMaxY;
    }

    public void setMotdMaxY(int motdMaxY) {
        this.motdMaxY = motdMaxY;
    }

    public int getMotdMinZ() {
        return motdMinZ;
    }

    public void setMotdMinZ(int motdMinZ) {
        this.motdMinZ = motdMinZ;
    }

    public int getMotdMaxZ() {
        return motdMaxZ;
    }

    public void setMotdMaxZ(int motdMaxZ) {
        this.motdMaxZ = motdMaxZ;
    }

    public int getSpawnGatewayX() {
        return spawnGatewayX;
    }

    public void setSpawnGatewayX(int spawnGatewayX) {
        this.spawnGatewayX = spawnGatewayX;
    }

    public int getSpawnGatewayY() {
        return spawnGatewayY;
    }

    public void setSpawnGatewayY(int spawnGatewayY) {
        this.spawnGatewayY = spawnGatewayY;
    }

    public int getSpawnGatewayZ() {
        return spawnGatewayZ;
    }

    public void setSpawnGatewayZ(int spawnGatewayZ) {
        this.spawnGatewayZ = spawnGatewayZ;
    }

    public int getSpawnGatewayExitX() {
        return spawnGatewayExitX;
    }

    public void setSpawnGatewayExitX(int spawnGatewayExitX) {
        this.spawnGatewayExitX = spawnGatewayExitX;
    }

    public int getSpawnGatewayExitY() {
        return spawnGatewayExitY;
    }

    public void setSpawnGatewayExitY(int spawnGatewayExitY) {
        this.spawnGatewayExitY = spawnGatewayExitY;
    }

    public int getSpawnGatewayExitZ() {
        return spawnGatewayExitZ;
    }

    public void setSpawnGatewayExitZ(int spawnGatewayExitZ) {
        this.spawnGatewayExitZ = spawnGatewayExitZ;
    }

    public int getReturnGatewayX() {
        return returnGatewayX;
    }

    public void setReturnGatewayX(int returnGatewayX) {
        this.returnGatewayX = returnGatewayX;
    }

    public int getReturnGatewayY() {
        return returnGatewayY;
    }

    public void setReturnGatewayY(int returnGatewayY) {
        this.returnGatewayY = returnGatewayY;
    }

    public int getReturnGatewayZ() {
        return returnGatewayZ;
    }

    public void setReturnGatewayZ(int returnGatewayZ) {
        this.returnGatewayZ = returnGatewayZ;
    }

    public void shutdown() {
        JsonObject o = new JsonObject();
        o.addProperty("motd_x_min", this.motdMinX);
        o.addProperty("motd_x_max", this.motdMaxX);
        o.addProperty("motd_y_min", this.motdMinY);
        o.addProperty("motd_y_max", this.motdMaxY);
        o.addProperty("motd_z_min", this.motdMinZ);
        o.addProperty("motd_z_max", this.motdMaxZ);
        o.addProperty("spawn_gateway_x", this.spawnGatewayX);
        o.addProperty("spawn_gateway_y", this.spawnGatewayY);
        o.addProperty("spawn_gateway_z", this.spawnGatewayZ);
        o.addProperty("spawn_gateway_exit_x", this.spawnGatewayExitX);
        o.addProperty("spawn_gateway_exit_y", this.spawnGatewayExitY);
        o.addProperty("spawn_gateway_exit_z", this.spawnGatewayExitZ);
        o.addProperty("return_gateway_x", this.returnGatewayX);
        o.addProperty("return_gateway_y", this.returnGatewayY);
        o.addProperty("return_gateway_z", this.returnGatewayZ);

        try {
            Files.write(path, new GsonBuilder().setPrettyPrinting().create().toJson(o).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Bityard.LOG.error("Unable to save Bityard config");
        }
    }
}
