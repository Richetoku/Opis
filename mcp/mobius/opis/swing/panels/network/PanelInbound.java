package mcp.mobius.opis.swing.panels.network;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import mcp.mobius.opis.api.ITabPanel;
import mcp.mobius.opis.data.holders.newtypes.DataAmountRate;
import mcp.mobius.opis.data.holders.newtypes.DataByteRate;
import mcp.mobius.opis.data.holders.newtypes.DataByteSize;
import mcp.mobius.opis.data.holders.newtypes.DataPacket;
import mcp.mobius.opis.network.enums.Message;
import mcp.mobius.opis.network.packets.server.NetDataRaw;
import mcp.mobius.opis.swing.SelectedTab;
import mcp.mobius.opis.swing.widgets.JPanelMsgHandler;
import mcp.mobius.opis.swing.widgets.JTableStats;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class PanelInbound extends JPanelMsgHandler implements ITabPanel {

	public PanelInbound() {
		setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "cell 0 0,grow");
		
		table = new JTableStats();
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null},
			},
			new String[] {
				"Type", "ID", "Amount", "Rate", "Total Size"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, Integer.class, DataAmountRate.class, DataByteRate.class, DataByteSize.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		table.setAutoCreateRowSorter(true);	
		scrollPane.setViewportView(table);
		
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment( SwingConstants.RIGHT );		
		
		
		for (int i = 1; i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);		
		
	}

	@Override
	public boolean handleMessage(Message msg, NetDataRaw rawdata) {
		switch(msg){
		case LIST_PACKETS_INBOUND:{
			
			((JTableStats)this.getTable()).setTableData(rawdata.array);
			
			DefaultTableModel model = (DefaultTableModel)this.getTable().getModel();
			int               row   = this.updateData(table, model, DataPacket.class);

			for (Object o : rawdata.array){
				DataPacket packet = (DataPacket)o;
				if (packet.type.equals("<UNUSED>")) continue;
				model.addRow(new Object[]  {
					packet.type,
					packet.id,
					packet.amount,
					packet.rate,
					packet.size    
					 });
			}
			
			this.dataUpdated(table, model, row);			
			
			break;
		}		
		
		default:
			return false;
			
		}
		return true;
	}

	@Override
	public SelectedTab getSelectedTab() {
		return SelectedTab.PACKETINBOUND;
	}	
	
}