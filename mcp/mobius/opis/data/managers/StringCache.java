package mcp.mobius.opis.data.managers;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import mcp.mobius.opis.modOpis;
import mcp.mobius.opis.api.IMessageHandler;
import mcp.mobius.opis.data.holders.ISerializable;
import mcp.mobius.opis.data.holders.newtypes.DataStringUpdate;
import mcp.mobius.opis.network.enums.Message;
import mcp.mobius.opis.network.packets.server.NetDataList;
import mcp.mobius.opis.network.packets.server.NetDataRaw;
import mcp.mobius.opis.network.packets.server.NetDataValue;

public enum StringCache implements IMessageHandler {
	INSTANCE;

	private int currentIndex = -1;
	//private HashBiMap<Integer, String> cache = HashBiMap.create();
	private BiMap<Integer, String> cache = Maps.synchronizedBiMap(HashBiMap.<Integer,String>create());
	private ConcurrentLinkedQueue<DataStringUpdate> fullsync = new ConcurrentLinkedQueue<DataStringUpdate>();
	private ConcurrentLinkedQueue<DataStringUpdate> unsynced = new ConcurrentLinkedQueue<DataStringUpdate>();	// This is the current list of unsynced 
	
	public String getString(int index){
		synchronized(cache){		
			String retVal = this.cache.get(index);
			if (retVal == null){
				return "<ERROR>";
			}else
				return retVal;
		}
	}
	
	public int getIndex(String str){
		synchronized(cache){
			if (cache.inverse().containsKey(str)){
				return cache.inverse().get(str);
			}
			else{
				currentIndex += 1;
				cache.put(currentIndex, str);
	
				DataStringUpdate upd = new DataStringUpdate(str, currentIndex);
				this.fullsync.add(upd);
				this.unsynced.add(upd);
				
				//PacketDispatcher.sendPacketToAllPlayers(NetDataValue.create(Message.STATUS_STRINGUPD, upd).packet);
				return currentIndex;
			}
		}
	}
	
	public void syncCache(Player player){
		
		int i = 0;
		
		ArrayList<DataStringUpdate> toSendCopy = new ArrayList(fullsync);
		
		while (i < toSendCopy.size()){
			PacketDispatcher.sendPacketToPlayer(NetDataList.create(Message.STATUS_STRINGUPD_FULL, toSendCopy.subList(i, Math.min(i + 50, toSendCopy.size()))).packet, player);			
			i += 50;
		}		
		
		
	}
	
	public void syncNewCache(){
		while (!unsynced.isEmpty()){
			PacketDispatcher.sendPacketToAllPlayers(NetDataValue.create(Message.STATUS_STRINGUPD, this.unsynced.poll()).packet);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg, NetDataRaw rawdata) {
		switch(msg){
		case STATUS_STRINGUPD:{
			DataStringUpdate data = (DataStringUpdate)rawdata.value;
			//modOpis.log.info(String.format("++++ Received String Cache update : [%d] %s", data.index, data.str));			
			try{
				this.cache.put(data.index, data.str);
			} catch (IllegalArgumentException e){
				this.cache.remove(data.index);
				this.cache.inverse().remove(data.str);
				this.cache.put(data.index, data.str);
			}
			break;
		}
		case STATUS_STRINGUPD_FULL:{
			modOpis.log.info(String.format("Received full String Cache update containing %d entries", rawdata.array.size()));
			
			for (ISerializable idata : rawdata.array){
				DataStringUpdate data = (DataStringUpdate)idata;
				//modOpis.log.info(String.format("++++ Received String Cache update : [%d] %s", data.index, data.str));					
				try{
					this.cache.put(data.index, data.str);
				} catch (IllegalArgumentException e){
					this.cache.remove(data.index);
					this.cache.inverse().remove(data.str);
					this.cache.put(data.index, data.str);
				}					
			}
			
			break;
		}
		default:
			return false;
			
		}
		return true;
	}

}
