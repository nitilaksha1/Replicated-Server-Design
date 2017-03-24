import org.apache.thrift.TException;
import java.util.*;
import java.io.*;
import clock.*;

class Request {
	private int 	timestamp;
	private String 	reqName;

	public Request (String name, int timestamp) {
		reqName = name;
		this.timestamp = timestamp;
	}

	public String getReqName () {
		return reqName;
	}
	
}

class DepositRequest extends Request {
	private int accountUID;
	private int amount;

	public DepositRequest (String name, int accUID, int amt, int timestamp) {

		super(name, timestamp);

		this.accountUID = accUID;

		if (amt < 0)
			this.amount = 0;
		else
			this.amount = amt;
	}

	public int getAccUID () {
		return accountUID;
	}

	public int getAmount() {
		return amount;
	}
}

class BalanceRequest extends Request {
	private int accountUID;

	public BalanceRequest (String name, int accUID, int timestamp) {
		super(name, timestamp);
		accountUID = accUID;
	}

	public int getAccUID () {
		return accountUID;
	}
}

class TransferRequest extends Request {
	private int sourceAccUID;
	private int targetAccUID;
	private int amount;

	public TransferRequest (String name, int srcAccUID, int targAccUID, int amt, int timestamp) {
		super(name, timestamp);
		sourceAccUID = srcAccUID;
		targetAccUID = targAccUID;

		if (amt < 0)
			amount = 0;
		else
			amount = amt;
	}

	public int getSourceAccUID () {
		return sourceAccUID;
	}

	public int gettargetAccUID () {
		return targetAccUID;
	}

	public int getAmount () {
		return amount;
	}
}

public class ReplicatedServerHandler implements ReplicatedBankService.Iface {
	
	private LamportClock clock;
	private volatile Queue<Request> reqQueue;
	private HashMap<Integer, Integer> map;
	private int id;
	private int nodeCount;
	private BankHandler bankhandler;

	public ReplicatedServerHandler () {
		clock = new LamportClock();
		map = new HashMap<>();
		reqQueue = new LinkedList<>();
		bankhandler = new BankHandler();
		nodeCount = 0;
	}

	public void setNodeCount (String filename) throws FileNotFoundException{

		Scanner scan = new Scanner (new File(filename));
		scan.nextLine();
		
		while (scan.hasNext()) {
			scan.next();
			scan.nextInt();
			scan.nextInt();
			
			nodeCount += 1;
				
		}
		
		//Rest of server logic
		scan.close();
	}

    public void setID(int id){
        this.id = id;
    }

    public int getID(int id){
        return this.id;
    }
		
	@Override
	public String multi_deposit (int uID, int amount, int timestamp) { 
		return "";
	}

	@Override
	public int multi_getBalance 	(int uID, int timestamp) {

		clock.SetClockValueForReceive(timestamp);
		clock.SetClockValueForSend();
		BalanceRequest balreq = new BalanceRequest ("BalanceRequest", uID, clock.getClockValue());
		reqQueue.add((Request)balreq);
		
		map.put (clock.getClockValue(), 0);

		//TODO:Write code for multicast

		while (map.get(timestamp+2) != nodeCount - 1); 

		//TODO: Check if condition for checking if head node has the same timestamp is required
		BalanceRequest balreq1 = (BalanceRequest)reqQueue.remove();
		int uid = balreq1.getAccUID();
		
		int res = bankhandler.getBalance(uid);
		map.remove(timestamp+2);

		return res;

	}

	@Override
	public String multi_transfer (int uID, int targuID, int amount, int timestamp) {

		clock.SetClockValueForReceive(timestamp);
		clock.SetClockValueForSend();
		TransferRequest transreq = new TransferRequest ("TransferRequest", uID, targuID, amount, clock.getClockValue());
		reqQueue.add((Request)transreq);
		
		map.put (clock.getClockValue(), 0);

		//TODO:Write code for multicast

		while (map.get(timestamp+2) != nodeCount - 1); 

		//TODO: Check if condition for checking if head node has the same timestamp is required
		TransferRequest transreq1 = (TransferRequest)reqQueue.remove();
		int srcuid = transreq1.getSourceAccUID();
		int targuid = transreq1.gettargetAccUID();
		int amt = transreq1.getAmount();
		
		String res = bankhandler.transfer(srcuid, targuid, amt);
		map.remove(timestamp+2);

		return res;
	}

	@Override
	public void multi_deposit_ack(int reqtimeStamp) {

	}

	@Override
	public void multi_getBalance_ack (int reqTimeStamp) {

		map.put(reqTimeStamp, map.get(reqTimeStamp) + 1);
	}

	@Override
	public void multi_transfer_ack	(int reqTimeStamp) {

		map.put(reqTimeStamp, map.get(reqTimeStamp) + 1);
	}	
}
