package mcp.mobius.opis.overlay.entperchunk;

import net.minecraft.util.MathHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import mapwriter.map.MapView;
import mapwriter.map.mapmode.MapMode;
import mcp.mobius.opis.data.holders.CoordinatesBlock;
import mcp.mobius.opis.gui.events.MouseEvent;
import mcp.mobius.opis.gui.interfaces.IWidget;
import mcp.mobius.opis.gui.widgets.tableview.TableRow;
import mcp.mobius.opis.gui.widgets.tableview.ViewTable;
import mcp.mobius.opis.network.json.CommandPacket;
import mcp.mobius.opis.network.json.OpisCommand;
//import mcp.mobius.opis.network.client.Packet_ReqData;
import mcp.mobius.opis.overlay.entperchunk.OverlayEntityPerChunk.ReducedData;

public class TableChunks extends ViewTable {
	MapView mapView;
	MapMode mapMode;
	OverlayEntityPerChunk overlay;		
	
	public TableChunks(IWidget parent, OverlayEntityPerChunk overlay) { 	
		super(parent);
		this.overlay = overlay;			
	}
	
	public void setMap(MapView mapView, MapMode mapMode){
	    this.mapView = mapView;
		this.mapMode = mapMode;			
	}
	
	@Override
	public void onMouseClick(MouseEvent event){
		TableRow row = this.getRow(event.x, event.y);
		if (row != null){
			CoordinatesBlock coord = ((ReducedData)row.getObject()).chunk.asCoordinatesBlock();
			this.overlay.selectedChunk = ((ReducedData)row.getObject()).chunk;
			CommandPacket.sendCommand(OpisCommand.GET_DATA, this.overlay.selectedChunk,  "list:chunk:entities");
			this.overlay.showList = false;
			
			this.mapView.setDimension(coord.dim);
			this.mapView.setViewCentre(coord.x, coord.z);
			this.overlay.requestChunkUpdate(this.mapView.getDimension(), 
					MathHelper.ceiling_double_int(this.mapView.getX()) >> 4, 
					MathHelper.ceiling_double_int(this.mapView.getZ()) >> 4);

		}
	}
}
