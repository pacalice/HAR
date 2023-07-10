package harImport;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

public class main implements BurpExtension, ExtensionUnloadingHandler {
	public MontoyaApi api;
	public GUI UI;
	@Override
	public void initialize(MontoyaApi api) {
		this.api = api;
		UI = new GUI(api); // Instantiate GUI
		api.extension().setName("HAR Importer"); // Name extension 	
		api.userInterface().registerSuiteTab("HAR Importer", UI); // Name extension tab
		api.logging().logToOutput("[+] HAR Importer Loaded"); // Extension loaded
		api.extension().registerUnloadingHandler(this); // Register extension unloaded
	}
	
	@Override
	public void extensionUnloaded() {
		UI = null;
		api.logging().logToOutput("[+] HAR Importer Unloaded"); // Extension unloaded
	}
	
	public static void main(String[] args) {
		
	}


}
