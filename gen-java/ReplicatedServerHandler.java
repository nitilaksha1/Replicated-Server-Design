import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
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

class ServerInfo{
      public int portnumber;
      public String hostname;

      public ServerInfo(int port, String host){
          portnumber = port;
          hostname = host;
      }
}

public class ReplicatedServerHandler implements ReplicatedBankService.Iface {
	
	private LamportClock clock;
	private volatile Queue<Request> reqQueue;
	private HashMap<Integer, Integer> map;
	private HashMap<Integer, ServerInfo> servermap;
	private ArrayList<ServerInfo> serverlist;
	private int id;
	private int nodeCount;
	private BankHandler bankhandler;

	public ReplicatedServerHandler () {
		clock = new LamportClock();
		map = new HashMap<>();
		reqQueue = new LinkedList<>();
		bankhandler = new BankHandler();
		serverlist = new ArrayList<>();
		servermap = new HashMap<>();
		nodeCount = 0;
	}

	public void setNodeCountAndList (String filename) throws FileNotFoundException{

		Scanner scan = new Scanner (new File(filename));
		scan.nextLine();
		
		while (scan.hasNext()) {
			String hostname = scan.next();
			int id = scan.nextInt();
			int portnumber = scan.nextInt();
			
			servermap.put(id, new ServerInfo(portnumber, hostname));

			if (id != this.id)
				serverlist.add(new ServerInfo(portnumber, hostname));

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
	public String multi_deposit (int uID, int amount, int timestamp, int serverid) { 
		return "";
	}

	@Override
	public int multi_getBalance 	(int uID, int timestamp, int serverid) {

		clock.SetClockValueForReceive(timestamp);
		clock.SetClockValueForSend();
		BalanceRequest balreq = new BalanceRequest ("BalanceRequest", uID, clock.getClockValue());
		reqQueue.add((Request)balreq);
		
		map.put (clock.getClockValue(), 0);

		while (map.get(timestamp+2) != nodeCount - 1); 

		//TODO: Check if condition for checking if head node has the same timestamp is required
		BalanceRequest balreq1 = (BalanceRequest)reqQueue.remove();
		int uid = balreq1.getAccUID();
		
		int res = bankhandler.getBalance(uid);
		map.remove(timestamp+2);

		return res;

	}

	@Override
	public String multi_transfer (int uID, int targuID, int amount, int timestamp, int serverid) {

		System.out.println("Inside multi_transfer of server: "+ serverid);
		System.out.println("Serverid: " + serverid + "Time stamp param for multi_transfer : " + timestamp);
		clock.SetClockValueForReceive(timestamp);
		clock.SetClockValueForSend();

		TransferRequest transreq = new TransferRequest ("TransferRequest", uID, targuID, amount, clock.getClockValue());
		reqQueue.add((Request)transreq);
	
		System.out.println("Serverid : " + serverid + " Clock value in map: " + clock.getClockValue());	
		map.put (clock.getClockValue(), 0);

		try {
			//Case: Request has arrived from a client, then we multicast the request
			if (timestamp == 0) {

				System.out.println("NodeCount = " + nodeCount);

				//TODO:Write code for multicast
				for (int i = 0; i < nodeCount - 1; i++) {
					System.out.println("Nodes remaining : " + i);
					String hostname = serverlist.get(i).hostname;
					int portnumber = serverlist.get(i).portnumber;

					TTransport transport;
					transport = new TSocket(hostname, portnumber);
					transport.open();

					TProtocol protocol = new  TBinaryProtocol(transport);
					ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);

					String ss = client.multi_transfer(uID, targuID, amount, timestamp+2, id);
					System.out.println("serverid: " + serverid + " multi_transfer complete : Result : " + ss);
					transport.close();

				}
				System.out.println("Multicast complete");


			} else {//Case: Request is from another server, then we will send acknowledgement to that server
				map.put(timestamp+2, nodeCount - 1);
			
				ServerInfo info = servermap.get(serverid);	
				String hostname = info.hostname;
				int portnumber = info.portnumber;
				
				TTransport transport;
				transport = new TSocket(hostname, portnumber);
				transport.open();

				TProtocol protocol = new  TBinaryProtocol(transport);
				ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);

				client.multi_transfer_ack(timestamp, serverid);
				transport.close();
			}

		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}

	
		System.out.println("Serverid: "+serverid+ " Before polling timestamp+2 = " + (timestamp+2));
		while (map.get(timestamp+2) != nodeCount - 1); 

		System.out.println("Polling done!");

		//TODO: Check if condition for checking if head node has the same timestamp is required
		TransferRequest transreq1 = (TransferRequest)reqQueue.remove();
		int srcuid = transreq1.getSourceAccUID();
		int targuid = transreq1.gettargetAccUID();
		int amt = transreq1.getAmount();
	
		System.out.println("Serverid: " + serverid + " Calling bankhandler transfer");	
		String res = bankhandler.transfer(srcuid, targuid, amt);
		System.out.println("Serverid: " + serverid + "bankhandler's transfer complete");
		map.remove(timestamp+2);

		return res;
	}

	@Override
	public void multi_deposit_ack(int reqtimeStamp, int serverid) {

	}

	@Override
	public void multi_getBalance_ack (int reqTimeStamp, int serverid) {

	}

	@Override
	public void multi_transfer_ack	(int reqTimeStamp, int serverid) {
		System.out.println("Serverid: " + serverid + "Acknowledgemnet received with time stamp : " + reqTimeStamp);
		map.put(reqTimeStamp, map.get(reqTimeStamp) + 1);
		System.out.println("Transfer acknowledgment received!!");
	}	
}
