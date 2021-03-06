package example.table;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.util.List;

import io.parcoord.MakeTableModel;

import swingPlus.parcoord.FormatterRenderer;
import swingPlus.parcoord.JColumnList;
import swingPlus.parcoord.JParCoord;
import swingPlus.parcoord.JTableLabel;
import swingPlus.parcoord.PolylineTypeBox;
import swingPlus.shared.JTableST;
import swingPlus.shared.MyFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import model.parcoord.ParCoordRowSorter;
import model.shared.selection.LinkedTableBothSelectionModel;

import util.GraphicsUtil;
import util.Messages;
import util.colour.ColorUtilities;
import util.ui.NewMetalTheme;


public class JTableDemo {

	JFrame jframe;
	final static Logger LOGGER = Logger.getLogger (JTableDemo.class);
	
	public static void main (final String[] args) {
		
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		LOGGER.info ("Logger activated");

		new JTableDemo (args.length > 0 ? args[0] : Messages.getString ("ExampleDataFile"));
	}
	
	
	JTableDemo (final String dataFileName) {
		
		JFrame.setDefaultLookAndFeelDecorated (true);

		final MakeTableModel mtm = new MakeTableModel ();
		final DefaultTableModel dtm = mtm.buildDataModel (dataFileName);
		final List<Format> columnTextFormatters = mtm.getColumnTextFormatters();
		
		final JTable jtable = new JTableST (dtm);
		jtable.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jtable.setRowSelectionAllowed (true);
		//jt.setColumnSelectionAllowed (true);
		jtable.setRowSorter (new TableRowSorter<DefaultTableModel> (dtm));
		jtable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
		
		final JTable jtable2 = new JTableST (dtm);
		jtable2.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jtable2.setRowSelectionAllowed (true);
		//jt2.setColumnSelectionAllowed (true);
		jtable2.setRowSorter (new TableRowSorter<DefaultTableModel> (dtm));
		
		
		final JParCoord jpc = new JParCoord (dtm);
		jpc.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jpc.setRowSelectionAllowed (true);	
		jpc.setRowSorter (new ParCoordRowSorter<DefaultTableModel> (dtm, jpc.getColumnModel()));
		jpc.setForeground (ColorUtilities.decodeWithAlpha (Messages.getString (GraphicsUtil.GRAPHICPROPS, "ParCoordUI.unselectedColour")));
		jpc.setSelectionForeground (ColorUtilities.decodeWithAlpha (Messages.getString (GraphicsUtil.GRAPHICPROPS, "ParCoordUI.selectedColour")));
		jpc.setSelectionBackground (jpc.getSelectionForeground ());
		jpc.setStroke (new BasicStroke (2.0f));
		jpc.setFont (Font.decode ("OCR A Extended-plain-12"));
		jpc.setGridColor (new Color (160, 160, 160));
		
		for (int modelColumn = 0; modelColumn < columnTextFormatters.size(); modelColumn++) {
			final Format format = columnTextFormatters.get (modelColumn);
			if (format != null) {
				final FormatterRenderer fRender = new FormatterRenderer (format);
				int viewColumn = jpc.convertColumnIndexToView (modelColumn);
				jpc.getColumnModel().getColumn(viewColumn).setCellRenderer (fRender);
				viewColumn = jtable2.convertColumnIndexToView (modelColumn);
				jtable2.getColumnModel().getColumn(viewColumn).setCellRenderer (fRender);
			}
		}
		
		
		final LinkedTableBothSelectionModel lrm = new LinkedTableBothSelectionModel ();
		lrm.addJTable (jtable);
		lrm.addJTable (jpc);
		lrm.addJTable (jtable2);
		
		jpc.addSynchroSet (new String[] {"ASKPRICE", "SELPRICE"});
		
		jpc.setBorder (BorderFactory.createEmptyBorder (20, 0, 20, 0));
		
		final JColumnList jcl = new JColumnList (jpc);
		jcl.getColumnModel().getColumn(1).setWidth (30);
		
		final PolylineTypeBox ptb = new PolylineTypeBox ();
		ptb.addToJPCList (jpc);
		
		
		
		SwingUtilities.invokeLater (
			new Runnable () {
				@Override
				public void run() {
					jframe = new MyFrame ("JP Demo");
					jframe.setSize (1024, 768);
					
					final JScrollPane jscp1 = new JScrollPane ();
					final JScrollPane jscp2 = new JScrollPane ();
					final JScrollPane jscp3 = new JScrollPane ();
					final JScrollPane jscp4 = new JScrollPane ();
					
					jscp1.setViewportView (jtable);
					jscp2.setViewportView (jtable2);
					jscp3.setViewportView (jpc);
					jscp4.setViewportView (jcl);
					
					final JSplitPane jsp2 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jscp1, jscp2);
					final JSplitPane jsp3 = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, jscp4, jscp3);
					final JSplitPane jsp = new JSplitPane (JSplitPane.VERTICAL_SPLIT, jsp2, jsp3);
					
					final JPanel additionalInfoPanel = new JPanel (new GridLayout (3, 0));	
					additionalInfoPanel.add (ptb);
					additionalInfoPanel.add (new JTableLabel (jpc));

					final JButton colourButton = new JButton ("Random Colour");
					colourButton.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (final ActionEvent aEvent) {
								jpc.setForeground (new Color ((float)Math.random(), (float)Math.random(), (float)Math.random()));
								jpc.setSelectionForeground (new Color ((float)Math.random(), (float)Math.random(), (float)Math.random()));
							}					
						}
					);
					additionalInfoPanel.add (colourButton);
					
					jframe.getContentPane().add (jsp);
					jframe.getContentPane().add (additionalInfoPanel, BorderLayout.SOUTH);
					jframe.setVisible (true);
					
					jsp.setDividerLocation (0.4f);
					jsp3.setDividerLocation (0.2f);
				}
			}
		);
	}
}
