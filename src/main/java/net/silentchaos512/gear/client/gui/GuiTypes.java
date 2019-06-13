package net.silentchaos512.gear.client.gui;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import net.silentchaos512.gear.SilentGear;
import net.silentchaos512.gear.block.analyzer.ContainerPartAnalyzer;
import net.silentchaos512.gear.block.analyzer.GuiPartAnalyzer;
import net.silentchaos512.gear.block.analyzer.TilePartAnalyzer;
import net.silentchaos512.gear.block.craftingstation.ContainerCraftingStation;
import net.silentchaos512.gear.block.craftingstation.GuiCraftingStation;
import net.silentchaos512.gear.block.craftingstation.TileCraftingStation;
import net.silentchaos512.gear.block.salvager.ContainerSalvager;
import net.silentchaos512.gear.block.salvager.GuiSalvager;
import net.silentchaos512.gear.block.salvager.TileSalvager;
import net.silentchaos512.lib.inventory.TileEntityContainerType;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public enum GuiTypes {
    CRAFTING_STATION {
        @Override
        public Container getContainer(TileEntityContainerType<?> tileType, PlayerEntity player) {
            TileCraftingStation tileEntity = (TileCraftingStation) tileType.getTileEntity(player);
            return new ContainerCraftingStation(player, Objects.requireNonNull(tileEntity));
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public ContainerScreen getGui(TileEntityContainerType<?> tileType, PlayerEntity player) {
            return new GuiCraftingStation((ContainerCraftingStation) tileType.createContainer(player.inventory, player));
        }
    },
    PART_ANALYZER {
        @Override
        public Container getContainer(TileEntityContainerType<?> tileType, PlayerEntity player) {
            IInventory tileEntity = (IInventory) tileType.getTileEntity(player);
            return new ContainerPartAnalyzer(player.inventory, Objects.requireNonNull(tileEntity));
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public ContainerScreen getGui(TileEntityContainerType<?> tileType, PlayerEntity player) {
            TilePartAnalyzer tileEntity = (TilePartAnalyzer) tileType.getTileEntity(player);
            return new GuiPartAnalyzer(player.inventory, Objects.requireNonNull(tileEntity));
        }
    },
    SALVAGER {
        @Override
        public Container getContainer(TileEntityContainerType<?> tileType, PlayerEntity player) {
            IInventory tileEntity = (IInventory) tileType.getTileEntity(player);
            return new ContainerSalvager(player.inventory, Objects.requireNonNull(tileEntity));
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public ContainerScreen getGui(TileEntityContainerType<?> tileType, PlayerEntity player) {
            TileSalvager tileEntity = (TileSalvager) tileType.getTileEntity(player);
            return new GuiSalvager(player.inventory, Objects.requireNonNull(tileEntity));
        }
    };

    public void display(PlayerEntity player, BlockPos pos) {
        if (!(player instanceof ServerPlayerEntity)) {
            SilentGear.LOGGER.error("Tried to send GUI packet from client?");
            return;
        }
        TileEntityContainerType<?> tileType = getContainerType(pos);
        IInteractionObject containerSupplier = new Interactable(this, pos);
        NetworkHooks.openGui((ServerPlayerEntity) player, containerSupplier, tileType::toBytes);
    }

    public ResourceLocation getId() {
        return new ResourceLocation(SilentGear.MOD_ID, name().toLowerCase(Locale.ROOT));
    }

    public <C extends Container> TileEntityContainerType<C> getContainerType() {
        return new TileEntityContainerType<>(getId());
    }

    public TileEntityContainerType<?> getContainerType(BlockPos pos) {
        return new TileEntityContainerType<>(getId(), pos);
    }

    public abstract Container getContainer(TileEntityContainerType<?> tileType, PlayerEntity player);

    @OnlyIn(Dist.CLIENT)
    public abstract ContainerScreen getGui(TileEntityContainerType<?> tileType, PlayerEntity player);

    @OnlyIn(Dist.CLIENT)
    public static Optional<GuiTypes> from(FMLPlayMessages.OpenContainer msg) {
        for (GuiTypes type : values()) {
            if (type.getId().equals(msg.getId())) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    static class Interactable implements IInteractionObject {
        private final GuiTypes type;
        private final BlockPos pos;

        Interactable(GuiTypes type, BlockPos pos) {
            this.type = type;
            this.pos = pos;
        }

        @Override
        public Container createContainer(PlayerInventory playerInventory, PlayerEntity playerIn) {
            return type.getContainer(type.getContainerType(pos), playerIn);
        }

        @Override
        public String getGuiID() {
            return type.getId().toString();
        }

        @Override
        public ITextComponent getName() {
            ResourceLocation id = type.getId();
            return new TranslationTextComponent("container." + id.getNamespace() + "." + id.getPath());
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Nullable
        @Override
        public ITextComponent getCustomName() {
            return null;
        }
    }
}
