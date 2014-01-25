package sep.pack.support;

import java.util.ArrayList;

import com.ib.controller.ApiController.IConnectionHandler;

public class LazyHandler implements IConnectionHandler {
	@Override
	public void connected() {
		System.out.println("Connection Established.");
	}

	@Override
	public void disconnected() {
		System.out.println("Disconnected.");	
	}

	@Override
	public void accountList(ArrayList<String> list) {
		UserInfo.acct = list.get(0); 
		System.out.println("Updating Account Information for Account: " + UserInfo.acct);
	}

	@Override
	public void error(Exception e) { //Go to EReader for ultimate cause
		System.out.println("It's all Long's fault...boom: "+e.getCause().toString());
	}

	@Override
	public void message(int id, int errorCode, String errorMsg) {
		System.out.println("MSG From IB: " + errorMsg);
	}

	@Override
	public void show(String string) {
		System.out.println("Long is a funny guy, he is shown...boom: MSG="+string);
	}
}
