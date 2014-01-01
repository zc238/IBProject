package sep.pack;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import sep.pack.MarketDataRetriever;
import apidemo.AccountInfoPanel;
import apidemo.AdvisorPanel;
import apidemo.ApiDemo;
import apidemo.ComboPanel;
import apidemo.ContractInfoPanel;
import apidemo.MarketDataPanel;
import apidemo.OptionsPanel;
import apidemo.StratPanel;
import apidemo.TradingPanel;
import apidemo.ApiDemo.ConnectionPanel;
import apidemo.ApiDemo.Logger;
import apidemo.util.NewLookAndFeel;
import apidemo.util.NewTabbedPanel;

import com.ib.controller.ApiConnection.ILogger;
import com.ib.controller.ApiController;
import com.ib.controller.ApiController.IBulletinHandler;
import com.ib.controller.ApiController.IConnectionHandler;
import com.ib.controller.ApiController.ITimeHandler;
import com.ib.controller.Formats;
import com.ib.controller.Types.NewsType;

public class strategy implements IConnectionHandler {
	static { NewLookAndFeel.register(); }
	static strategy INSTANCE = new strategy();

	private final MarketDataRetriever retriever = new MarketDataRetriever( this, m_inLogger, m_outLogger);

	public static void main(String[] args) {
		INSTANCE.myController().makeconnection();
	}
}
// request market data

// save data in local file

// send market order