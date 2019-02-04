package co.uk.flansmods.common.network;

import co.uk.flansmods.common.FlansMod;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;

public class PacketPlaySound extends FlanPacketCommon {

    public static Random rand = new Random();
    public static final byte packetID = 8;

    public static void sendSoundPacket(double x, double y, double z, double range, int dimension, String s, boolean distort) {
        sendSoundPacket(x, y, z, range, dimension, s, distort, false);
    }

    public static void sendSoundPacket(double x, double y, double z, double range, int dimension, String s, boolean distort, boolean silenced) {
        PacketDispatcher.sendPacketToAllAround(x, y, z, range, dimension, buildSoundPacket(x, y, z, s, distort, silenced));
    }

    public static Packet buildSoundPacket(double x, double y, double z, String s) {
        return buildSoundPacket(x, y, z, s, false);
    }

    public static Packet buildSoundPacket(double x, double y, double z, String s, boolean distort) {
        return buildSoundPacket(x, y, z, s, distort, false);
    }

    public static Packet buildSoundPacket(double x, double y, double z, String s, boolean distort, boolean silenced) {
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = channelFlan;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try {
            data.write(packetID); // this is the packet ID. identifies it as a BreakSoundPacket
            data.writeFloat((float) x);
            data.writeFloat((float) y);
            data.writeFloat((float) z);
            data.writeUTF(s);
            data.writeBoolean(distort);
            data.writeBoolean(silenced);

            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;

            data.close();
            bytes.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packet;
    }

    @Override
    public void interpret(DataInputStream stream, Object[] extradata, Side side) {
        if (side.equals(Side.CLIENT))
            interpretClient(stream, extradata);
        else FlansMod.log("Sound packet recieved on server. Skipping interpretation.");
    }

    @SideOnly(value = Side.CLIENT)
    private void interpretClient(DataInputStream stream, Object[] extradata) {
        try {
            float x = stream.readFloat();
            float y = stream.readFloat();
            float z = stream.readFloat();
            String sound = stream.readUTF();
            boolean distort = stream.readBoolean();
            boolean silenced = stream.readBoolean();

            FMLClientHandler.instance().getClient().sndManager.playSound("flansmod:" + sound, x, y, z, silenced ? 0.5F : 4F, (distort ? 1.0F / (rand.nextFloat() * 0.4F + 0.8F) : 1.0F) * (silenced ? 2F : 1F));
        } catch (Exception e) {
            FlansMod.log("Error reading or playing sound");
            e.printStackTrace();
        }
    }

    @Override
    public byte getPacketID() {
        return packetID;
    }

}
