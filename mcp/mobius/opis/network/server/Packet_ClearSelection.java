package mcp.mobius.opis.network.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import mcp.mobius.opis.network.Packet251Extended;
import mcp.mobius.opis.network.Packets;
import net.minecraft.network.packet.Packet250CustomPayload;

public class Packet_ClearSelection {
	public byte header;
	
	public Packet_ClearSelection(Packet250CustomPayload packet) {
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try{
			this.header  = inputStream.readByte();
		} catch (IOException e){}			
		
	}

	public static Packet250CustomPayload create(){
		Packet250CustomPayload packet = new Packet250CustomPayload();
		ByteArrayOutputStream bos     = new ByteArrayOutputStream(1);
		DataOutputStream outputStream = new DataOutputStream(bos);

		try{
			outputStream.writeByte(Packets.CLR_SELECTION);
		}catch(IOException e){}
		
		packet.channel = "Opis";
		packet.data    = bos.toByteArray();
		packet.length  = bos.size();		
		
		return packet;
	}		
}
