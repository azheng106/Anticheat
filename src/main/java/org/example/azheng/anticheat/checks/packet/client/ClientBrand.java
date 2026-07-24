package org.example.azheng.anticheat.checks.packet.client;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;

import java.nio.charset.StandardCharsets;

public class ClientBrand implements PacketListener {

    private static String clientBrand = "Unknown";

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLUGIN_MESSAGE) {
            return;
        }

        WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);

        String channelName = packet.getChannelName();

        if (!channelName.equals("minecraft:brand") && !channelName.equals("MC|Brand")) {
            return;
        }

        byte[] data = packet.getData();

        if (data == null || data.length == 0) {
            return;
        }

        clientBrand = new String(data, StandardCharsets.UTF_8)
                .replaceAll("[^\\x20-\\x7E]", "")
                .trim();
    }

    public static String getClientBrand() {
        return clientBrand;
    }
}