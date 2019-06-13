package net.silentchaos512.gear.traits;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.stats.ItemStat;
import net.silentchaos512.gear.api.traits.ITrait;
import net.silentchaos512.gear.api.traits.ITraitSerializer;
import net.silentchaos512.gear.api.traits.TraitActionContext;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleTrait implements ITrait {
    public static final Serializer<SimpleTrait> SERIALIZER = new Serializer<>(Serializer.NAME, SimpleTrait::new);

    private final ResourceLocation objId;
    int maxLevel;
    Set<String> cancelsWith = new HashSet<>();
    ITextComponent displayName;
    ITextComponent description;

    public SimpleTrait(ResourceLocation id) {
        this.objId = id;
    }

    @Override
    public ResourceLocation getId() {
        return objId;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean willCancelWith(ITrait other) {
        return cancelsWith.contains(other.getId().toString());
    }

    @Override
    public ITextComponent getDisplayName(int level) {
        return displayName
                .deepCopy()
                .appendText(" ")
                .appendSibling(new TranslationTextComponent("enchantment.level." + level));
    }

    @Override
    public ITextComponent getDescription(int level) {
        return description.deepCopy();
    }

    @Override
    public ITraitSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public float onAttackEntity(TraitActionContext context, LivingEntity target, float baseValue) {
        return baseValue;
    }

    @Override
    public float onDurabilityDamage(TraitActionContext context, int damageTaken) {
        return damageTaken;
    }

    @Override
    public void onGearCrafted(TraitActionContext context) {
    }

    @Override
    public float onGetStat(TraitActionContext context, ItemStat stat, float value, float damageRatio) {
        return value;
    }

    @Override
    public void onUpdate(TraitActionContext context, boolean isEquipped) {
    }

    public static final class Serializer<T extends SimpleTrait> implements ITraitSerializer<T> {
        private static final ResourceLocation NAME = new ResourceLocation(SilentGear.MOD_ID, "simple_trait");

        private final ResourceLocation serializerId;
        private final Function<ResourceLocation, T> factory;
        private final BiConsumer<T, JsonObject> readJson;

        public Serializer(ResourceLocation serializerId, Function<ResourceLocation, T> factory) {
            this(serializerId, factory, null);
        }

        public Serializer(ResourceLocation serializerId,
                          Function<ResourceLocation, T> factory,
                          @Nullable BiConsumer<T, JsonObject> readJson) {
            this.serializerId = serializerId;
            this.factory = factory;
            this.readJson = readJson;
        }

        @Override
        public T read(ResourceLocation id, JsonObject json) {
            T trait = factory.apply(id);
            trait.maxLevel = JSONUtils.getInt(json, "max_level", 1);
            trait.displayName = readTextComponent(json, "name");
            trait.description = readTextComponent(json, "description");

            if (json.has("cancels_with")) {
                JsonArray array = json.getAsJsonArray("cancels_with");
                for (JsonElement elem : array) {
                    trait.cancelsWith.add(elem.getAsString());
                }
            }

            if (readJson != null) {
                readJson.accept(trait, json);
            }

            return trait;
        }

        @Override
        public T read(ResourceLocation id, PacketBuffer buffer) {
            T trait = factory.apply(id);
            trait.maxLevel = buffer.readByte();
            trait.displayName = buffer.readTextComponent();
            trait.description = buffer.readTextComponent();
            int cancelsCount = buffer.readVarInt();
            for (int i = 0; i < cancelsCount; ++i) {
                trait.cancelsWith.add(buffer.readString(255));
            }
            return trait;
        }

        @Override
        public void write(PacketBuffer buffer, T trait) {
            buffer.writeByte(trait.maxLevel);
            buffer.writeTextComponent(trait.displayName);
            buffer.writeTextComponent(trait.description);
            buffer.writeVarInt(trait.cancelsWith.size());
            for (String str : trait.cancelsWith) {
                buffer.writeString(str);
            }
        }

        @Override
        public ResourceLocation getName() {
            return serializerId;
        }

        private static ITextComponent readTextComponent(JsonObject json, String name) {
            JsonElement element = json.get(name);
            if (element != null && element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                final boolean translate = JSONUtils.getBoolean(obj, "translate", false);
                final String value = JSONUtils.getString(obj, "name");
                return translate
                        ? new TranslationTextComponent(value)
                        : new StringTextComponent(value);
            } else if (element != null) {
                throw new JsonParseException("Expected '" + name + "' to be an object");
            } else {
                throw new JsonParseException("Missing required object '" + name + "'");
            }
        }
    }
}
