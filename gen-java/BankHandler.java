import org.apache.thrift.TException;
import java.util.*;
import java.io.*;

class Account {
	int UID;
	int balance;

	public Account (int UID, int balance) {
		this.UID = UID;
		this.balance = balance;
	}

	public int getUID () {
		return UID;
	}

	public void deposit (int amount) {
		balance += amount;
	}

	public void withdraw (int amount) {

		if ((balance - amount) < 0)
			return;

		balance -= amount;
	}

	public int getBalance () {
		return balance;
	}

}

public class BankHandler {
	private static HashMap<Integer, Account> map; 
	private int accountid;
	private Object lock = new Object();
     	private int id=0;
	private static PrintWriter writer;
	  
	public BankHandler () {

		map = new HashMap<Integer, Account>();
		accountid = 1;

		try {

			writer = new PrintWriter("serverLog.txt", "UTF-8");
		} catch (FileNotFoundException e){

		} catch (UnsupportedEncodingException e){
		}

	}


	public void setID(int id){
		this.id = id;
	}

	public int getID(int id){
		return this.id;
	}

	public int createAccount() throws TException {

		Account acc = new Account(accountid, 0);
		map.put(accountid, acc);
		writer.println("RequestName: createAccount "+ "Return status: "+accountid);
		writer.flush(); 
		accountid++;
		return acc.getUID();
	}

	public String deposit (int uID, int amount) {

		if (map.containsKey(uID)) {

			Account acc = map.get(uID);
			acc.deposit(amount);

			writer.println("RequestName: Deposit "+ "Parameters : "+ uID + " "+amount + " Return status: OK");
			writer.flush();
			return "OK";
		}

		writer.println("RequestName: Deposit "+ "Parameters : "+ uID + " "+amount + " Return status: FAILED");
		writer.flush();
	
		return "FAILED";
	}

	public int getBalance (int uID) {

		if (map.containsKey(uID)) {

			Account acc = map.get(uID);

			writer.println("RequestName: GetBalance "+ "Parameters : "+ uID + " Return status: "+ acc.getBalance());
			writer.flush();
	
			return acc.getBalance();
		}		

		writer.println("RequestName: GetBalance "+ "Parameters : "+ uID + " Return status: 0");
		writer.flush();

		return 0;	
	}

	public String transfer (int srcuID, int targuID, int amount) {
	
		if (map.containsKey(srcuID) && map.containsKey(targuID)) {

			Account acc1 = map.get(srcuID);
			Account acc2 = map.get(targuID);

			synchronized (lock) {
				
				if ((acc1.getBalance() - amount) < 0) {

					writer.println("RequestName: Transfer "+ "Parameters : "+ srcuID + " "+targuID + " "+ amount + " Return status: FAILED");
					writer.flush();
					return "FAILED";
				}

				acc1.withdraw(amount);
				acc2.deposit(amount);
				writer.println("RequestName: Transfer "+ "Parameters : "+ srcuID + " "+targuID + " "+ amount + " Return status: OK");
				writer.flush();

				return "OK";	
			}	
		}


		return "FAILED";
	}
}
