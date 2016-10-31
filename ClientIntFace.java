package homework3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientIntFace extends Remote {	
	void CallbackWish(String wishmsg) throws RemoteException;
	void CallbackSell(String wishmsg) throws RemoteException;
}
