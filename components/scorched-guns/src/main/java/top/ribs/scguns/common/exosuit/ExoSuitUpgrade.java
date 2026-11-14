package top.ribs.scguns.common.exosuit;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import top.ribs.scguns.util.INBTSerializable;
import top.ribs.scguns.annotation.Optional;

/**
 * Enhanced ExoSuit upgrade with weapon handling modifiers and pouch support
 */
public class ExoSuitUpgrade implements INBTSerializable<CompoundTag> {

    protected String type = "plating";
    protected Effects effects = new Effects();
    protected Display display = new Display();

    public String getType() {
        return this.type;
    }

    public Effects getEffects() {
        return this.effects;
    }

    public Display getDisplay() {
        return this.display;
    }

    public static class Effects implements INBTSerializable<CompoundTag> {
        @Optional
        float armorBonus = 0.0F;
        @Optional
        float armorToughness = 0.0F;
        @Optional
        float knockbackResistance = 0.0F;
        @Optional
        float speedModifier = 0.0F;
        @Optional
        float jumpBoost = 0.0F;
        @Optional
        float fallDamageReduction = 0.0F;
        @Optional
        boolean nightVision = false;
        @Optional
        boolean flight = false;
        @Optional
        float flightSpeed = 0.0F;
        @Optional
        float energyUse = 0.0F;
        @Optional
        float recoilAngleReduction = 0.0F;
        @Optional
        float recoilKickReduction = 0.0F;
        @Optional
        float spreadReduction = 0.0F;


        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            return this.serializeNBT();
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("ArmorBonus", this.armorBonus);
            tag.putFloat("ArmorToughness", this.armorToughness);
            tag.putFloat("KnockbackResistance", this.knockbackResistance);
            tag.putFloat("SpeedModifier", this.speedModifier);
            tag.putFloat("JumpBoost", this.jumpBoost);
            tag.putFloat("FallDamageReduction", this.fallDamageReduction);
            tag.putBoolean("NightVision", this.nightVision);
            tag.putBoolean("Flight", this.flight);
            tag.putFloat("FlightSpeed", this.flightSpeed);
            tag.putFloat("EnergyUse", this.energyUse);

            tag.putFloat("RecoilAngleReduction", this.recoilAngleReduction);
            tag.putFloat("RecoilKickReduction", this.recoilKickReduction);
            tag.putFloat("SpreadReduction", this.spreadReduction);

            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
            this.deserializeNBT(tag);
        }

        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("ArmorBonus")) {
                this.armorBonus = tag.getFloatOr("ArmorBonus", 0.0F);
            }
            if (tag.contains("ArmorToughness")) {
                this.armorToughness = tag.getFloatOr("ArmorToughness", 0.0F);
            }
            if (tag.contains("KnockbackResistance")) {
                this.knockbackResistance = tag.getFloatOr("KnockbackResistance", 0.0F);
            }
            if (tag.contains("SpeedModifier")) {
                this.speedModifier = tag.getFloatOr("SpeedModifier", 0.0F);
            }
            if (tag.contains("JumpBoost")) {
                this.jumpBoost = tag.getFloatOr("JumpBoost", 0.0F);
            }
            if (tag.contains("FallDamageReduction")) {
                this.fallDamageReduction = tag.getFloatOr("FallDamageReduction", 0.0F);
            }
            if (tag.contains("NightVision")) {
                this.nightVision = tag.getBooleanOr("NightVision", false);
            }
            if (tag.contains("Flight")) {
                this.flight = tag.getBooleanOr("Flight", false);
            }
            if (tag.contains("FlightSpeed")) {
                this.flightSpeed = tag.getFloatOr("FlightSpeed", 0.0F);
            }
            if (tag.contains("EnergyUse")) {
                this.energyUse = tag.getFloatOr("EnergyUse", 0.0F);
            }

            if (tag.contains("RecoilAngleReduction")) {
                this.recoilAngleReduction = tag.getFloatOr("RecoilAngleReduction", 0.0F);
            }
            if (tag.contains("RecoilKickReduction")) {
                this.recoilKickReduction = tag.getFloatOr("RecoilKickReduction", 0.0F);
            }
            if (tag.contains("SpreadReduction")) {
                this.spreadReduction = tag.getFloatOr("SpreadReduction", 0.0F);
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (this.armorBonus != 0.0F) object.addProperty("armorBonus", this.armorBonus);
            if (this.armorToughness != 0.0F) object.addProperty("armorToughness", this.armorToughness);
            if (this.knockbackResistance != 0.0F) object.addProperty("knockbackResistance", this.knockbackResistance);
            if (this.speedModifier != 0.0F) object.addProperty("speedModifier", this.speedModifier);
            if (this.jumpBoost != 0.0F) object.addProperty("jumpBoost", this.jumpBoost);
            if (this.fallDamageReduction != 0.0F) object.addProperty("fallDamageReduction", this.fallDamageReduction);
            if (this.nightVision) object.addProperty("nightVision", true);
            if (this.flight) object.addProperty("flight", true);
            if (this.flightSpeed != 0.0F) object.addProperty("flightSpeed", this.flightSpeed);
            if (this.energyUse != 0.0F) object.addProperty("energyUse", this.energyUse);

            if (this.recoilAngleReduction != 0.0F) object.addProperty("recoilAngleReduction", this.recoilAngleReduction);
            if (this.recoilKickReduction != 0.0F) object.addProperty("recoilKickReduction", this.recoilKickReduction);
            if (this.spreadReduction != 0.0F) object.addProperty("spreadReduction", this.spreadReduction);

            return object;
        }

        public Effects copy() {
            Effects effects = new Effects();
            effects.armorBonus = this.armorBonus;
            effects.armorToughness = this.armorToughness;
            effects.knockbackResistance = this.knockbackResistance;
            effects.speedModifier = this.speedModifier;
            effects.jumpBoost = this.jumpBoost;
            effects.fallDamageReduction = this.fallDamageReduction;
            effects.nightVision = this.nightVision;
            effects.flight = this.flight;
            effects.flightSpeed = this.flightSpeed;
            effects.energyUse = this.energyUse;
            effects.recoilAngleReduction = this.recoilAngleReduction;
            effects.recoilKickReduction = this.recoilKickReduction;
            effects.spreadReduction = this.spreadReduction;
            return effects;
        }

        // Getters
        public float getArmorBonus() { return this.armorBonus; }
        public float getArmorToughness() { return this.armorToughness; }
        public float getKnockbackResistance() { return this.knockbackResistance; }
        public float getSpeedModifier() { return this.speedModifier; }
        public float getJumpBoost() { return this.jumpBoost; }
        public float getFallDamageReduction() { return this.fallDamageReduction; }
        public boolean hasNightVision() { return this.nightVision; }
        public boolean hasFlight() { return this.flight; }
        public float getFlightSpeed() { return this.flightSpeed; }
        public float getRecoilAngleReduction() { return this.recoilAngleReduction; }
        public float getRecoilKickReduction() { return this.recoilKickReduction; }
        public float getSpreadReduction() { return this.spreadReduction; }
        public float getEnergyUse() { return this.energyUse; }
    }

    public static class Display implements INBTSerializable<CompoundTag> {
        @Optional
        String model = "";
        @Optional
        int storageSize = 0;
        @Optional
        int gridWidth = 0;
        @Optional
        int gridHeight = 0;
        @Optional
        String containerType = ""; // "dispenser", "chest", "double_chest"

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries) {
            return this.serializeNBT();
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Model", this.model);
            tag.putInt("StorageSize", this.storageSize);
            tag.putInt("GridWidth", this.gridWidth);
            tag.putInt("GridHeight", this.gridHeight);
            tag.putString("ContainerType", this.containerType);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
            this.deserializeNBT(tag);
        }

        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("Model")) {
                this.model = tag.getStringOr("Model", "");
            }
            if (tag.contains("StorageSize")) {
                this.storageSize = tag.getIntOr("StorageSize", 0);
            }
            if (tag.contains("GridWidth")) {
                this.gridWidth = tag.getIntOr("GridWidth", 0);
            }
            if (tag.contains("GridHeight")) {
                this.gridHeight = tag.getIntOr("GridHeight", 0);
            }
            if (tag.contains("ContainerType")) {
                this.containerType = tag.getStringOr("ContainerType", "");
            }
        }

        public JsonObject toJsonObject() {
            JsonObject object = new JsonObject();
            if (!this.model.isEmpty()) object.addProperty("model", this.model);
            if (this.storageSize > 0) object.addProperty("storageSize", this.storageSize);
            if (this.gridWidth > 0) object.addProperty("gridWidth", this.gridWidth);
            if (this.gridHeight > 0) object.addProperty("gridHeight", this.gridHeight);
            if (!this.containerType.isEmpty()) object.addProperty("containerType", this.containerType);
            return object;
        }

        public Display copy() {
            Display display = new Display();
            display.model = this.model;
            display.storageSize = this.storageSize;
            display.gridWidth = this.gridWidth;
            display.gridHeight = this.gridHeight;
            display.containerType = this.containerType;
            return display;
        }

        // Getters
        public String getModel() { return this.model; }
        public int getStorageSize() { return this.storageSize; }
        public int getGridWidth() { return this.gridWidth; }
        public int getGridHeight() { return this.gridHeight; }
        public String getContainerType() { return this.containerType; }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        return this.serializeNBT();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type);
        tag.put("Effects", this.effects.serializeNBT());
        tag.put("Display", this.display.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        this.deserializeNBT(tag);
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Type")) {
            this.type = tag.getStringOr("Type", "");
        }
        if (tag.contains("Effects")) {
            this.effects.deserializeNBT(tag.getCompoundOrEmpty("Effects"));
        }
        if (tag.contains("Display")) {
            this.display.deserializeNBT(tag.getCompoundOrEmpty("Display"));
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("type", this.type);
        object.add("effects", this.effects.toJsonObject());
        JsonObject displayObj = this.display.toJsonObject();
        if (displayObj.size() > 0) {
            object.add("display", displayObj);
        }
        return object;
    }

    public ExoSuitUpgrade copy() {
        ExoSuitUpgrade upgrade = new ExoSuitUpgrade();
        upgrade.type = this.type;
        upgrade.effects = this.effects.copy();
        upgrade.display = this.display.copy();
        return upgrade;
    }

    public static class Builder {
        private final ExoSuitUpgrade upgrade;

        private Builder() {
            this.upgrade = new ExoSuitUpgrade();
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder setType(String type) {
            this.upgrade.type = type;
            return this;
        }

        public Builder setArmorBonus(float armor) {
            this.upgrade.effects.armorBonus = armor;
            return this;
        }

        public Builder setArmorToughness(float toughness) {
            this.upgrade.effects.armorToughness = toughness;
            return this;
        }

        public Builder setKnockbackResistance(float resistance) {
            this.upgrade.effects.knockbackResistance = resistance;
            return this;
        }

        public Builder setSpeedModifier(float speed) {
            this.upgrade.effects.speedModifier = speed;
            return this;
        }

        public Builder setJumpBoost(float jump) {
            this.upgrade.effects.jumpBoost = jump;
            return this;
        }

        public Builder setNightVision(boolean nightVision) {
            this.upgrade.effects.nightVision = nightVision;
            return this;
        }

        public Builder setRecoilAngleReduction(float reduction) {
            this.upgrade.effects.recoilAngleReduction = reduction;
            return this;
        }

        public Builder setRecoilKickReduction(float reduction) {
            this.upgrade.effects.recoilKickReduction = reduction;
            return this;
        }

        public Builder setSpreadReduction(float reduction) {
            this.upgrade.effects.spreadReduction = reduction;
            return this;
        }

        public Builder setModel(String model) {
            this.upgrade.display.model = model;
            return this;
        }

        public Builder setStorageSize(int size) {
            this.upgrade.display.storageSize = size;
            return this;
        }

        public Builder setGridDimensions(int width, int height) {
            this.upgrade.display.gridWidth = width;
            this.upgrade.display.gridHeight = height;
            return this;
        }

        public Builder setContainerType(String containerType) {
            this.upgrade.display.containerType = containerType;
            return this;
        }

        public ExoSuitUpgrade build() {
            return this.upgrade.copy();
        }
    }
}
