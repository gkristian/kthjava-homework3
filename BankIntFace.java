package homework3;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankIntFace extends Remote {	
	//String CreateAccount(String name) throws RemoteException;
	int getBalance(String name) throws RemoteException;
	String Deposit(String name,int depositamount) throws RemoteException;
	String Withdraw(String name,int withdrawamount) throws RemoteException;
}
