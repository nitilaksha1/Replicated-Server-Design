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
	  
	public BankHandler () {

		map = new HashMap<Integer, Account>();
		accountid = 1;

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
		accountid++;
		return acc.getUID();
	}

	public String deposit (int uID, int amount) {

		if (map.containsKey(uID)) {

			Account acc = map.get(uID);
			acc.deposit(amount);

			return "OK";
		}

		return "FAILED";
	}

	public int getBalance (int uID) {

		if (map.containsKey(uID)) {

			Account acc = map.get(uID);

			return acc.getBalance();
		}

		return 0;	
	}

	public String transfer (int srcuID, int targuID, int amount) {
	
		if (map.containsKey(srcuID) && map.containsKey(targuID)) {

			Account acc1 = map.get(srcuID);
			Account acc2 = map.get(targuID);

			synchronized (lock) {
				
				if ((acc1.getBalance() - amount) < 0) {

					return "FAILED";
				}

				acc1.withdraw(amount);
				acc2.deposit(amount);

				return "OK";	
			}	
		}


		return "FAILED";
	}
}
