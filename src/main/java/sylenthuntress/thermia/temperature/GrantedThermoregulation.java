package sylenthuntress.thermia.temperature;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.UUID;

public record GrantedThermoregulation(List<UUID> playerUUIDs) {
    public final static GrantedThermoregulation DEFAULT = new GrantedThermoregulation(List.of());

    public static Codec<GrantedThermoregulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(UUIDUtil.STRING_CODEC).fieldOf("player_uuids").forGetter(GrantedThermoregulation::playerUUIDs)
    ).apply(instance, GrantedThermoregulation::new));

    public static StreamCodec<ByteBuf, GrantedThermoregulation> PACKET_CODEC = StreamCodec.composite(
        ByteBufCodecs.<ByteBuf, UUID>list().apply(UUIDUtil.STREAM_CODEC), GrantedThermoregulation::playerUUIDs,
        GrantedThermoregulation::new
    );

    public static GrantedThermoregulation addPlayer(List<UUID> playerUUIDs, GameProfile profile) {
        final UUID uuid = profile.id();
        if (uuid != null) {
            playerUUIDs.add(uuid);
        }

        return new GrantedThermoregulation(playerUUIDs);
    }
}
