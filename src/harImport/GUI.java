package harImport;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import java.io.FileInputStream;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class GUI extends JPanel {
	private MontoyaApi api;
	public JTable table;
	public DefaultTableModel tmodel;
	public JTextArea txtRequest;
	public JTextArea txtResponse;
	public HARData harData;
	public List<Integer> ImportedRows;
	public List<HARData> har;
	public SendRequest sendreq;
	
	public GUI(MontoyaApi api) {
		this.api = api;
		ImportedRows = new ArrayList<>();
		setLayout(new BorderLayout(0, 0));
		JPanel pnlTop = new JPanel();
		pnlTop.setLayout(new BorderLayout(0, 0));
		
		// Setup btnImport
		JButton btnImport = new JButton("Import HAR");
		btnImport.setIcon(new ImageIcon(main.class.getResource("/img/import.png")));
		btnImport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				JFileChooser fileChooser = new JFileChooser(); // Select HAR file
				FileNameExtensionFilter filter = new FileNameExtensionFilter("HAR Files", "har");
		        fileChooser.setFileFilter(filter);
		        int returnValue = fileChooser.showOpenDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION) { // If file was selected
		            File selectedFile = fileChooser.getSelectedFile();
		            if (table.getRowCount() >0) clear();
		            try {
						har = parseHARFile(selectedFile.getAbsolutePath()); // Parse HAR file and store in list
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					}
		        }
			}
		});
		pnlTop.add(btnImport);
		// End setup btnImport
		
		add(pnlTop, BorderLayout.NORTH);
		tmodel = new DefaultTableModel(null,new String[] {"Method", "Status", "URL", "Date/Time", "Elapsed", "Server IP"});
		
		// Create context menu
		JPopupMenu ctxMenu = new JPopupMenu("Edit");
		JMenuItem itemint = new JMenuItem("Send Selected to Sitemap");
		itemint.setIcon(new ImageIcon(main.class.getResource("/img/send.png")));
		itemint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				if (table.getSelectedRowCount() > 1) {
					for (int row : table.getSelectedRows()) { // Send selected requests
						HttpRequest httpreq = HttpRequest.httpRequestFromUrl((String) table.getValueAt(row, 2).toString());
						SendRequest sendreq = new SendRequest(httpreq,api);
						sendreq.execute();
					}
				} else if (table.getSelectedRowCount() == 1) { // Send selected request
					HttpRequest httpreq = HttpRequest.httpRequestFromUrl((String) table.getModel().getValueAt(table.getSelectedRow(), 2));
					sendreq = new SendRequest(httpreq,api);
					sendreq.execute();		
				}
			}
		});
		JMenuItem itemall = new JMenuItem("Send All to Sitemap");
		itemall.setIcon(new ImageIcon(main.class.getResource("/img/sendall.png")));
		itemall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				for (int row=0; row<table.getRowCount(); row++) { // Send all requests
					HttpRequest httpreq = HttpRequest.httpRequestFromUrl((String) table.getValueAt(row, 2).toString());
					sendreq = new SendRequest(httpreq,api);
					sendreq.execute();
				}
			}
		});
		JMenuItem itemclear = new JMenuItem("Clear List");
		itemclear.setIcon(new ImageIcon(main.class.getResource("/img/clear.png")));
		itemclear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Clear table
				int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the list?", "Confirm",
				        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				        
				if (response == JOptionPane.YES_OPTION) {
					clear();
				}
			}
		});
		ctxMenu.add(itemint);
		ctxMenu.add(itemall);
		ctxMenu.add(itemclear);
		// End Context menus
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);		
		JPanel pnlNorth = new JPanel();
		splitPane.setLeftComponent(pnlNorth);
		table = new JTable();
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setModel(tmodel);
		table.getColumnModel().getColumn(0).setMinWidth(100);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(1).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(575);
		pnlNorth.setLayout(new BorderLayout(0, 0));
		JScrollPane pnlTable = new JScrollPane(table);		
		pnlNorth.add(pnlTable);
		pnlTable.setLayout(new ScrollPaneLayout());	
		table.setComponentPopupMenu(ctxMenu);		
		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);		
		JScrollPane pnlRequest = new JScrollPane();
		tabbedPane_1.addTab("Request", null, pnlRequest, null);		
		JScrollPane pnlResponse = new JScrollPane();
		tabbedPane_1.addTab("Response", null, pnlResponse, null);		
	    txtResponse = new JTextArea();
		pnlResponse.setViewportView(txtResponse);
		splitPane.setRightComponent(tabbedPane_1);
		pnlRequest.setLayout(new ScrollPaneLayout());		
		txtRequest = new JTextArea();
		pnlRequest.setViewportView(txtRequest);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {				
				if (e.getClickCount() == 1) { // Get selected row
					txtRequest.setText("");
					txtResponse.setText("");
					int row = table.rowAtPoint(e.getPoint());
					int col = table.columnAtPoint(e.getPoint());
					HARData a = har.get(row); // Get HARData custom data type from list
					//Populate response/request headers
					int r = 0;
					for (String header : a.requestHeaders) { // Loop through request headers
						String txt = txtRequest.getText();
						if (r == 0) {
							txtRequest.setText(txt + header.replace("name: ", "").replace("\"","").replace("value",""));
							r++;
						} else {
							txtRequest.setText(txt + header.replace("name: ", "").replace("\"","").replace("value","") + "\n");
							r = 0;
						} 
					}
					txtRequest.setCaretPosition(0);
					int s = 0;
					for (String header : a.responseHeaders) { // Loop through response headers
						String txt = txtResponse.getText();
						if (s == 0) {
							txtResponse.setText(txt + header.replace("name: ", "").replace("\"","").replace("value",""));
							s++;
						} else {
							txtResponse.setText(txt + header.replace("name: ", "").replace("\"","").replace("value","") + "\n");
							s = 0;
						} 
					}
					txtResponse.setCaretPosition(0);
				}
			}
		});
	}
		
	public void clear() { // Clears table, har list, and text boxes
		tmodel.setRowCount(0);
		har.clear();
		txtRequest.setText("");
		txtResponse.setText(" ");
	}
	
	// Function for parsing the HAR file contents and storing in a List of the HARData type
    public List<HARData> parseHARFile(String fileName) throws FileNotFoundException {
    	// Init objects for reading HAR file
        List<HARData> harDataList = new ArrayList<>();
        JsonReader reader = Json.createReader(new FileInputStream(fileName));
        JsonObject harObject = reader.readObject();
        JsonArray entries = harObject.getJsonObject("log").getJsonArray("entries");

        for (JsonObject entry : entries.getValuesAs(JsonObject.class)) { // Loop through each entry
            // Grab entry values and store to appropriate variable
        	JsonObject request = entry.getJsonObject("request");
            JsonObject response = entry.getJsonObject("response");
            String url = request.getString("url");
            String method = request.getString("method");
            int statusCode = response.getInt("status");
            String datetime = entry.getString("startedDateTime");
            int elapsed = entry.getInt("time");
            String ip = entry.getString("serverIPAddress");
            // Add a new row to the table
            this.tmodel.addRow(new Object[] {method,statusCode,url,datetime,elapsed+"ms",ip});
            harData = new HARData(url,method,statusCode); // Instantiate harData and store values
            harData.method = method;
            harData.bodySize = String.valueOf(request.getInt("bodySize"));
            harData.httpVersion = request.getString("httpVersion");
            JsonArray headers = request.getJsonArray("headers");
            try { // Loop through request headers and add to harData variable
				for (JsonValue headerValue : headers) {
				    JsonObject header = (JsonObject) headerValue;
				    for (Map.Entry<String, JsonValue> headerEntry : header.entrySet()) {
				    	String key = headerEntry.getKey();
				    	String val = String.valueOf(headerEntry.getValue());
				    	harData.requestHeaders.add(key + ": " + val);
				    }
				}
			} catch (Exception e) {
				api.logging().logToError(e.getMessage());
			}
            JsonArray rspHeaders = response.getJsonArray("headers");
            try { // Loop through response headers and add to harData variable
				for (JsonValue headerValue : rspHeaders) { 
				    JsonObject header = (JsonObject) headerValue;
				    for (Map.Entry<String, JsonValue> headerEntry : header.entrySet()) {
				    	String key = headerEntry.getKey();
				    	String val = String.valueOf(headerEntry.getValue());
				    	harData.responseHeaders.add(key + ": " + val);
				    }
				}
			} catch (Exception e) {
				api.logging().logToError(e.getMessage());
			}
            harData.dateTime = datetime;            
            harDataList.add(harData); // Add variable harData to list
        }

        reader.close(); // Close reader object
        return harDataList; // Return complete list of HARData
    }
	
	// Custom data type for importing HAR data
	public class HARData {
		public String url;
		public String method;
		public int statusCode;
		public String dateTime;
		public String bodySize;
		public String httpVersion;
		public List<String> requestHeaders = new ArrayList<>();
		public List<String> responseHeaders = new ArrayList<>();		
		HARData(String url, String method, int status) {	
			this.url = url;
			this.method = method;
			this.statusCode = status;
		}	
	}
	
	// Class for sending request in swingworker
	public class SendRequest extends SwingWorker<Integer, String> {
		HttpRequest req;
		MontoyaApi api;
		Boolean stop;

		public SendRequest(HttpRequest req, MontoyaApi api) {
			this.api = api;
			this.req = req;	
			this.stop = false;
		}
	    @Override
	    protected Integer doInBackground() throws Exception {
	    	// Add to Sitemap
	    	if (!this.stop) {
	    		api.siteMap().add(api.http().sendRequest(this.req));
	    	}
	    	return 0;
	    }
	    public void stop() {
	    	this.stop = true;
	    }
	}
	
	
}

