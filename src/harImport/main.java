package harImport;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

public class main implements BurpExtension, ExtensionUnloadingHandler {
	public MontoyaApi api;
	@Override
	public void initialize(MontoyaApi api) {
		this.api = api;
		GUI UI = new GUI(api);
		api.extension().setName("HAR Importer");	
		api.userInterface().registerSuiteTab("HAR Importer", UI);
		api.logging().logToOutput("[+] HAR Importer Loaded");
		api.extension().registerUnloadingHandler(this);
	}
	
	public static void main(String[] args) {
		
	}

	@Override
	public void extensionUnloaded() {
		api.logging().logToOutput("[+] HAR Importer Unloaded");
	}

}
