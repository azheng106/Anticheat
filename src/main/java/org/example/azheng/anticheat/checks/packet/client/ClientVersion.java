package org.example.azheng.anticheat.checks.packet.client;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

public class ClientVersion implements PacketListener {

    private static String clientVersion = "Unknown";

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        clientVersion = parseVersion(event.getUser().getClientVersion().toString());
    }

    /*
    * For simpler readability, lets parse the version and remove unecessary characters
    */
    private static String parseVersion(String version) {
        if (version.startsWith("V_")) {
            return version.substring(2).replace('_', '.');
        }

        return version;
    }

    public static String getClientVersion() {
        return clientVersion;
    }
}