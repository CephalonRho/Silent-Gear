/*
 * Silent Gear -- LootFunctionSelectGearTier
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.gear.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.api.item.ICoreItem;
import net.silentchaos512.gear.util.GearGenerator;

public final class LootFunctionSelectGearTier extends LootFunction {
    private final int tier;

    private LootFunctionSelectGearTier(ILootCondition[] conditions, int tier) {
        super(conditions);
        this.tier = tier;
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof ICoreItem)) return stack;
        return GearGenerator.create((ICoreItem) stack.getItem(), this.tier);
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionSelectGearTier> {
        public Serializer() {
            super(new ResourceLocation(SilentGear.MOD_ID, "select_tier"), LootFunctionSelectGearTier.class);
        }

        @Override
        public void serialize(JsonObject object, LootFunctionSelectGearTier functionClazz, JsonSerializationContext serializationContext) {
            object.addProperty("tier", functionClazz.tier);
        }

        @Override
        public LootFunctionSelectGearTier deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
            int tier = JSONUtils.getInt(object, "tier", 2);
            return new LootFunctionSelectGearTier(conditionsIn, tier);
        }
    }
}
