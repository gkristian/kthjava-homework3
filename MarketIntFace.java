package homework3;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface MarketIntFace extends Remote {	
	void BankConnect() throws RemoteException;
	boolean userValidated(String username) throws RemoteException;
	String SendObjectReference(String name) throws RemoteException;
        void Register(String name) throws RemoteException;
	void Unregister(String name) throws RemoteException;
	String ShowAll() throws RemoteException;
	String Buy(String buyitemId,String buyer) throws RemoteException;
	void Sell(String item,int price,String seller) throws RemoteException;
	void Wish(String item,int price,String wisher) throws RemoteException;
}
