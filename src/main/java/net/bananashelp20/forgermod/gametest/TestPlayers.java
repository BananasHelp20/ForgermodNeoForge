package net.bananashelp20.forgermod.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import java.util.UUID;

/** GameTestHelper's default server mock hardcodes creative mode; combat needs a survival player. */
final class TestPlayers {
    private TestPlayers() {}
    static ServerPlayer survival(GameTestHelper test) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"combat-test-player"),false);
        var player=new ServerPlayer(test.getLevel().getServer(),test.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND); new EmbeddedChannel(connection);
        test.getLevel().getServer().getPlayerList().placeNewPlayer(connection,player,cookie);
        player.setGameMode(GameType.SURVIVAL);
        for(int tick=0;tick<61;tick++) player.tick();
        return player;
    }
}
