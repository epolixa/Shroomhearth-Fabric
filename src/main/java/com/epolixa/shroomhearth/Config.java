package com.epolixa.shroomhearth;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Config {
    private static final Path path = FabricLoader.getInstance().getConfigDir().resolve("shroomhearth.json");

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

    private int commonMinPrice;
    private int commonMaxPrice;
    private int commonUsesBonus;
    private int uncommonMinPrice;
    private int uncommonMaxPrice;
    private int uncommonUsesBonus;
    private int rareMinPrice;
    private int rareMaxPrice;
    private int rareUsesBonus;
    private int epicMinPrice;
    private int epicMaxPrice;
    private int ominousMinPrice;
    private int ominousMaxPrice;

    public Config() {
        JsonObject json;
        try {
            json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
            loadFromJson(json);
        } catch (IOException e) {
            // Create default
            try {
                Files.copy(Objects.requireNonNull(Config.class.getResourceAsStream("/data/shroomhearth_fabric/files/default_config.json")), path);
                json = new JsonParser().parse(new String(Files.readAllBytes(path))).getAsJsonObject();
                loadFromJson(json);
                Shroomhearth.LOG.fatal("Unable to load config file for Shroomhearth");
                Shroomhearth.LOG.fatal("Please fill out the config file for Shroomhearth, found in config/shroomhearth.json");
            } catch (IOException ioException) {
                ioException.printStackTrace();
                Shroomhearth.LOG.fatal("Unable to create default config");
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
        returnGatewayY = json.get("return_gateway_y").getAsInt();
        returnGatewayZ = json.get("return_gateway_z").getAsInt();

        commonMinPrice = json.get("common_price_min").getAsInt();
        commonMaxPrice = json.get("common_price_max").getAsInt();
        commonUsesBonus = json.get("common_uses_bonus").getAsInt();
        uncommonMinPrice = json.get("uncommon_price_min").getAsInt();
        uncommonMaxPrice = json.get("uncommon_price_max").getAsInt();
        uncommonUsesBonus = json.get("uncommon_uses_bonus").getAsInt();
        rareMinPrice = json.get("rare_price_min").getAsInt();
        rareMaxPrice = json.get("rare_price_max").getAsInt();
        rareUsesBonus = json.get("rare_uses_bonus").getAsInt();
        epicMinPrice = json.get("epic_price_min").getAsInt();
        epicMaxPrice = json.get("epic_price_max").getAsInt();
        ominousMinPrice = json.get("ominous_price_min").getAsInt();
        ominousMaxPrice = json.get("ominous_price_max").getAsInt();
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

    public int getCommonMinPrice() {
        return commonMinPrice;
    }

    public void setCommonMinPrice(int commonMinPrice) {
        this.commonMinPrice = commonMinPrice;
    }

    public int getCommonMaxPrice() {
        return commonMaxPrice;
    }

    public void setCommonMaxPrice(int commonMaxPrice) {
        this.commonMaxPrice = commonMaxPrice;
    }

    public int getCommonUsesBonus() {
        return commonUsesBonus;
    }

    public void setCommonUsesBonus(int commonUsesBonus) {
        this.commonUsesBonus = commonUsesBonus;
    }

    public int getUncommonMinPrice() {
        return uncommonMinPrice;
    }

    public void setUncommonMinPrice(int uncommonMinPrice) {
        this.uncommonMinPrice = uncommonMinPrice;
    }

    public int getUncommonMaxPrice() {
        return uncommonMaxPrice;
    }

    public void setUncommonMaxPrice(int uncommonMaxPrice) {
        this.uncommonMaxPrice = uncommonMaxPrice;
    }

    public int getUncommonUsesBonus() {
        return uncommonUsesBonus;
    }

    public void setUncommonUsesBonus(int uncommonUsesBonus) {
        this.uncommonUsesBonus = uncommonUsesBonus;
    }

    public int getRareMinPrice() {
        return rareMinPrice;
    }

    public void setRareMinPrice(int rareMinPrice) {
        this.rareMinPrice = rareMinPrice;
    }

    public int getRareMaxPrice() {
        return rareMaxPrice;
    }

    public void setRareMaxPrice(int rareMaxPrice) {
        this.rareMaxPrice = rareMaxPrice;
    }

    public int getRareUsesBonus() {
        return rareUsesBonus;
    }

    public void setRareUsesBonus(int rareUsesBonus) {
        this.rareUsesBonus = rareUsesBonus;
    }

    public int getEpicMinPrice() {
        return epicMinPrice;
    }

    public void setEpicMinPrice(int epicMinPrice) {
        this.epicMinPrice = epicMinPrice;
    }

    public int getEpicMaxPrice() {
        return epicMaxPrice;
    }

    public void setEpicMaxPrice(int epicMaxPrice) {
        this.epicMaxPrice = epicMaxPrice;
    }

    public int getOminousMinPrice() {
        return ominousMinPrice;
    }

    public void setOminousMinPrice(int ominousMinPrice) {
        this.ominousMinPrice = ominousMinPrice;
    }

    public int getOminousMaxPrice() {
        return ominousMaxPrice;
    }

    public void setOminousMaxPrice(int ominousMaxPrice) {
        this.ominousMaxPrice = ominousMaxPrice;
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

        o.addProperty("common_price_min", this.commonMinPrice);
        o.addProperty("common_price_max", this.commonMaxPrice);
        o.addProperty("common_uses_bonus", this.commonUsesBonus);
        o.addProperty("uncommon_price_min", this.uncommonMinPrice);
        o.addProperty("uncommon_price_max", this.uncommonMaxPrice);
        o.addProperty("uncommon_uses_bonus", this.uncommonUsesBonus);
        o.addProperty("rare_price_min", this.rareMinPrice);
        o.addProperty("rare_price_max", this.rareMaxPrice);
        o.addProperty("rare_uses_bonus", this.rareUsesBonus);
        o.addProperty("epic_price_min", this.epicMinPrice);
        o.addProperty("epic_price_max", this.epicMaxPrice);
        o.addProperty("ominous_price_min", this.ominousMinPrice);
        o.addProperty("ominous_price_max", this.ominousMaxPrice);

        try {
            Files.write(path, new GsonBuilder().setPrettyPrinting().create().toJson(o).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Shroomhearth.LOG.error("Unable to save Shroomhearth config");
        }
    }
}
