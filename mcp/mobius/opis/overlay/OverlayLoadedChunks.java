package mcp.mobius.opis.overlay;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import mapwriter.Mw;
import mapwriter.api.IMwChunkOverlay;
import mapwriter.api.IMwDataProvider;
import mapwriter.map.MapView;
import mapwriter.map.mapmode.MapMode;
import mcp.mobius.opis.data.holders.CoordinatesBlock;
import mcp.mobius.opis.data.holders.CoordinatesChunk;
import mcp.mobius.opis.data.holders.TicketData;
import mcp.mobius.opis.data.managers.ChunkManager;
import mcp.mobius.opis.gui.events.MouseEvent;
import mcp.mobius.opis.gui.interfaces.CType;
import mcp.mobius.opis.gui.interfaces.IWidget;
import mcp.mobius.opis.gui.interfaces.WAlign;
import mcp.mobius.opis.gui.widgets.LayoutBase;
import mcp.mobius.opis.gui.widgets.LayoutCanvas;
import mcp.mobius.opis.gui.widgets.WidgetGeometry;
import mcp.mobius.opis.gui.widgets.tableview.TableRow;
import mcp.mobius.opis.gui.widgets.tableview.ViewTable;
//import mcp.mobius.opis.network.client.Packet_ReqChunks;
//import mcp.mobius.opis.network.client.Packet_ReqChunksInDim;
//import mcp.mobius.opis.network.client.Packet_ReqTeleport;
//import mcp.mobius.opis.network.client.Packet_ReqTickets;
//import mcp.mobius.opis.network.client.Packet_UnregisterPlayer;
import mcp.mobius.opis.network.json.CommandPacket;
import mcp.mobius.opis.network.json.OpisCommand;

public class OverlayLoadedChunks implements IMwDataProvider {

	public class TicketTable extends ViewTable{
		MapView mapView;
		MapMode mapMode;
		OverlayLoadedChunks overlay;
		
		public TicketTable(IWidget parent, OverlayLoadedChunks overlay) { 	
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
				CoordinatesChunk coord = ((TicketData)row.getObject()).coord;
				
				if (this.mapView.getX() != coord.x || this.mapView.getZ() != coord.z || this.mapView.getDimension() != coord.dim){
					this.mapView.setDimension(coord.dim);
					this.mapView.setViewCentre(coord.x, coord.z);
					this.overlay.requestChunkUpdate(this.mapView.getDimension(), 
							MathHelper.ceiling_double_int(this.mapView.getX()) >> 4, 
							MathHelper.ceiling_double_int(this.mapView.getZ()) >> 4);
				} else {
					CommandPacket.sendCommand(OpisCommand.TELEPORT, new CoordinatesBlock(coord));
					Minecraft.getMinecraft().setIngameFocus();
				}
			}
		}
	}	
	
	public class ChunkOverlay implements IMwChunkOverlay{

		Point coord;
		boolean forced;
		
		public ChunkOverlay(int x, int z, boolean forced){
			this.coord = new Point(x, z);
			this.forced = forced;
		}
		
		@Override
		public Point getCoordinates() {	return this.coord; }

		@Override
		public int getColor() {	return this.forced ? 0x500000ff : 0x5000ff00; }

		@Override
		public float getFilling() {	return 1.0f; }

		@Override
		public boolean hasBorder() { return true; }

		@Override
		public float getBorderWidth() { return 0.5f; }

		@Override
		public int getBorderColor() { return 0xff000000; }
		
	}	
	
	//public static OverlayLoadedChunks instance = new OverlayLoadedChunks();
	//private OverlayLoadedChunks(){};
	
	CoordinatesChunk selectedChunk = null;
	private static OverlayLoadedChunks _instance;
	public boolean    showList = false;
	public LayoutCanvas canvas = null;
	
	private OverlayLoadedChunks(){}
	
	public static OverlayLoadedChunks instance(){
		if(_instance == null)
			_instance = new OverlayLoadedChunks();			
		return _instance;
	}	
	
	@Override
	public ArrayList<IMwChunkOverlay> getChunksOverlay(int dim, double centerX,	double centerZ, double minX, double minZ, double maxX, double maxZ) {
		ArrayList<IMwChunkOverlay> overlays = new ArrayList<IMwChunkOverlay>();
		for (ChunkCoordIntPair chunk : ChunkManager.chunksLoad.keySet())
			overlays.add(new ChunkOverlay(chunk.chunkXPos, chunk.chunkZPos, ChunkManager.chunksLoad.get(chunk)));
		return overlays;
	}

	@Override
	public String getStatusString(int dim, int bX, int bY, int bZ) {
		int xChunk = bX >> 4;
		int zChunk = bZ >> 4;
		ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(xChunk, zChunk);
		
		if (ChunkManager.chunksLoad.containsKey(chunkCoord)){
			if (ChunkManager.chunksLoad.get(chunkCoord))
				return ", Force loaded";
			else
				return ", Player loaded";
		}
		else
			return ", Not loaded";
	}

	@Override
	public void onMiddleClick(int dim, int bX, int bZ, MapView mapview) {
		int chunkX = bX >> 4;
		int chunkZ = bZ >> 4;
		this.requestChunkUpdate(dim, chunkX, chunkZ);
	}

	private void requestChunkUpdate(int dim, int chunkX, int chunkZ){
		ArrayList<CoordinatesChunk> chunks = new ArrayList<CoordinatesChunk>();
		//HashSet<ChunkCoordIntPair> chunkCoords = new HashSet<ChunkCoordIntPair>(); 

		for (int x = -5; x <= 5; x++){
			for (int z = -5; z <= 5; z++){
				chunks.add(new CoordinatesChunk(dim, chunkX + x, chunkZ + z));
				if (chunks.size() >= 1){
					CommandPacket.sendCommand(OpisCommand.GET_CHUNKS, dim, chunks);
					chunks.clear();
				}
			}
		}

		/*
		for (int x = -5; x <= 5; x++){
			for (int z = -5; z <= 5; z++){
				CoordinatesChunk coord = new CoordinatesChunk(dim, chunkX + x, chunkZ + z); 
				chunks.add(coord);
				chunkCoords.add(coord.toChunkCoordIntPair());
			}
		}
		
		if (chunks.size() > 0){
			Mw.instance.chunkManager.addForcedChunks(chunkCoords);
			PacketDispatcher.sendPacketToServer(Packet_ReqChunks.create(dim, chunks));
		}
		*/
		
		if (chunks.size() > 0)
			CommandPacket.sendCommand(OpisCommand.GET_CHUNKS, dim, chunks);				
	}
	
	@Override
	public void onDimensionChanged(int dimension, MapView mapview) {
		CommandPacket.sendCommand(OpisCommand.GET_LOADED_CHUNKS_IN_DIM, dimension);
	}

	@Override
	public void onMapCenterChanged(double vX, double vZ, MapView mapview) {
	}

	@Override
	public void onZoomChanged(int level, MapView mapview) {
	}

	@Override
	public void onOverlayActivated(MapView mapview) {
		this.selectedChunk = null;		
		CommandPacket.sendCommand(OpisCommand.GET_LOADED_CHUNKS_IN_DIM, mapview.getDimension());
		CommandPacket.sendCommand(OpisCommand.GET_LOADED_CHUNKS_TICKETS);
	}

	@Override
	public void onOverlayDeactivated(MapView mapview) {
		this.showList = false;
		this.selectedChunk = null;
		CommandPacket.sendCommand(OpisCommand.UNREGISTER_USER);
	}

	@Override
	public void onDraw(MapView mapview, MapMode mapmode) {
		if (this.canvas == null)
			this.canvas = new LayoutCanvas();
		
		if (mapmode.marginLeft != 0){
			this.canvas.hide();
			return;
		}
		
		if (!this.showList)
			this.canvas.hide();
		else{
			this.canvas.show();		
			this.canvas.draw();
		}
	}

	@SideOnly(Side.CLIENT)
	public void setupTable(HashSet<TicketData> tickets){
		if (this.canvas == null)
			this.canvas = new LayoutCanvas();		
		
		LayoutBase layout = (LayoutBase)this.canvas.addWidget("Table", new LayoutBase(null));
		//layout.setGeometry(new WidgetGeometry(100.0,0.0,300.0,100.0,CType.RELXY, CType.REL_Y, WAlign.RIGHT, WAlign.TOP));
		layout.setGeometry(new WidgetGeometry(100.0,0.0,30.0,100.0,CType.RELXY, CType.RELXY, WAlign.RIGHT, WAlign.TOP));
		layout.setBackgroundColors(0x90202020, 0x90202020);
		
		TicketTable  table  = (TicketTable)layout.addWidget("Table_", new TicketTable(null, this));
		
		table.setGeometry(new WidgetGeometry(0.0,0.0,100.0,100.0,CType.RELXY, CType.RELXY, WAlign.LEFT, WAlign.TOP));
	    table.setColumnsAlign(WAlign.CENTER, WAlign.CENTER, WAlign.CENTER)
		     //.setColumnsTitle("\u00a7a\u00a7oPos", "\u00a7a\u00a7oMod", "\u00a7a\u00a7oChunks")
	    	 .setColumnsTitle("Pos", "Mod", "Chunks")
			 .setColumnsWidth(50,25,25)
			 .setRowColors(0xff808080, 0xff505050)
			 .setFontSize(1.0f);			 

		
		for (TicketData data : tickets)
			//table.addRow(data, String.format("[%s %s %s]", data.coord.dim, data.coord.chunkX, data.coord.chunkZ), "\u00a79\u00a7o" + data.modID, String.valueOf(data.nchunks));
			table.addRow(data, String.format("[%s %s %s]", data.coord.dim, data.coord.chunkX, data.coord.chunkZ), data.modID, String.valueOf(data.nchunks));

		this.showList = true;
	}	
	
	@Override
	public boolean onMouseInput(MapView mapview, MapMode mapmode) {
		if (this.canvas != null && this.canvas.shouldRender() && ((LayoutCanvas)this.canvas).hasWidgetAtCursor()){
			((TicketTable)this.canvas.getWidget("Table").getWidget("Table_")).setMap(mapview, mapmode);
			this.canvas.handleMouseInput();
			return true;
		}
		return false;
	}
}
