package net.silentchaos512.gear.network;

import net.minecraft.network.PacketBuffer;
import net.silentchaos512.gear.api.parts.IGearPart;
import net.silentchaos512.gear.parts.PartSerializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SyncGearPartsPacket {
    private List<IGearPart> parts;

    public SyncGearPartsPacket() { }

    public SyncGearPartsPacket(Collection<IGearPart> parts) {
        this.parts = new ArrayList<>(parts);
    }

    public static SyncGearPartsPacket fromBytes(PacketBuffer buf) {
        SyncGearPartsPacket packet = new SyncGearPartsPacket();
        packet.parts = new ArrayList<>();
        int count = buf.readVarInt();

        for (int i = 0; i < count; ++i) {
            packet.parts.add(PartSerializers.read(buf));
        }

        return packet;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(this.parts.size());
        this.parts.forEach(part -> PartSerializers.write(part, buf));
    }

    public List<IGearPart> getParts() {
        return this.parts;
    }
}
