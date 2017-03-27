import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.TException;
import java.util.*;
import java.io.*;
import clock.*;

class Request {
	private int 	timestamp;
	private String 	reqName;
	private int ackCount;
	private String reqID;

	public Request (String name, int timestamp, int ackCount, String reqID) {
		reqName = name;
		this.timestamp = timestamp;
		this.ackCount = ackCount;
		this.reqID = reqID;
	}

	public String getReqName () {
		return reqName;
	}
	public int getAckCount() { return ackCount; }
	public void setAckCount() {ackCount++;}
	public int getTimestamp() { return timestamp; }
	public String getReqID() { return reqID; }
	
}

class DepositRequest extends Request {
	private int accountUID;
	private int amount;

	public DepositRequest (String name, int accUID, int amt, int timestamp, int count, String requestID) {

		super(name, timestamp, count, requestID);

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

	public BalanceRequest (String name, int accUID, int timestamp, int count, String requestID) {
		super(name, timestamp, count, requestID);
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

	public TransferRequest (String name, int srcAccUID, int targAccUID, int amt, int timestamp, int count, String requestID) {
		super(name, timestamp, count, requestID);
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
	private volatile int reqID;
	private PrintWriter writer;
	private ArrayList<Integer> accountList;

	public ReplicatedServerHandler () {

		clock = new LamportClock();
		map = new HashMap<>();
		reqQueue = new LinkedList<>();
		bankhandler = new BankHandler();
		serverlist = new ArrayList<>();
		servermap = new HashMap<>();
		nodeCount = 0;
		reqID = 0;
		accountList = new ArrayList<>();
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

		scan.close();
	}

	public void setAccountList(ArrayList<Integer> serverList){
		accountList = serverList;
	}

	public ArrayList<Integer> getAccountList(){
		return accountList;
	}

	public void initLogFile (String filename) throws FileNotFoundException {
		writer = new PrintWriter (filename + "_" + getID());
	}

	public void closeLogFile () {

		writer.close();
	}
	
    public void setID(int id){

		this.id = id;
    }

    public int getID(){

		return this.id;
    }
		
	@Override
	public String multi_deposit (int uID, int amount, int timestamp, int serverid) { 

		return "NA";
	}

	@Override
	public int multi_getBalance (int uID, int timestamp, int serverid) {

		clock.SetClockValueForReceive(timestamp);
		clock.SetClockValueForSend();
		BalanceRequest balreq = new BalanceRequest ("BalanceRequest", uID, clock.getClockValue(), 0, "dummy");
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
	public void stop_execution(int timestamp){

		synchronized (clock){
			clock.SetClockValueForReceive(timestamp);
		}

		ArrayList<Integer> list = getAccountList();
		for(int i =0 ; i < list.size(); i++){
			System.out.println("Account id: " + list.get(i) + " Balance: " + bankhandler.getBalance(list.get(i)));
		}
	}

	@Override
	public void halt(){

		try{
			for (int i = 0; i < nodeCount - 1; i++) {
				String hostname = serverlist.get(i).hostname;
				int portnumber = serverlist.get(i).portnumber;

				TTransport transport;
				transport = new TSocket(hostname, portnumber);
				transport.open();

				TProtocol protocol = new  TBinaryProtocol(transport);

				ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);
				clock.SetClockValueForSend();
				client.stop_execution(clock.getClockValue());

				System.out.println("serverid: " + "0" + " multi_halt complete");
				transport.close();

			}
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}

		ArrayList<Integer> list = getAccountList();
		for(int i =0 ; i < list.size(); i++){
			System.out.println("Account id: " + list.get(i) + " Balance: " + bankhandler.getBalance(list.get(i)));
		}





	}

	//serverID is used for getting the value of sender server
	@Override
	public void multi_transfer_server (int uID, int targuID, int amount, int timestamp, int serverid, String requestID) {
			System.out.println("Multicast received");

			TransferRequest transreq;

			synchronized (reqQueue){

				clock.SetClockValueForReceive(timestamp);
				writer.println("CLNT-ID" + "	SRV_REQ		" + serverid + "	" + System.currentTimeMillis() + "	[" + clock.getClockValue() + "," + getID() + "]" + "	Transfer Operation" + "	Source ID: " + uID + "	Target ID: " + targuID + "	Amount " + amount);
				writer.flush();
				transreq = new TransferRequest("TransferRequest", uID, targuID, amount, clock.getClockValue(), nodeCount -1, requestID);
				reqQueue.add((Request)transreq);
			}

			ServerInfo info = servermap.get(serverid);
			String hostname = info.hostname;
			int portnumber = info.portnumber;

			try {
				TTransport transport;

				transport = new TSocket(hostname, portnumber);
				transport.open();

				TProtocol protocol = new TBinaryProtocol(transport);
				ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);

				synchronized (clock) {
					clock.SetClockValueForSend();
				}

				client.multi_transfer_ack(requestID, id, clock.getClockValue());

			}catch(TTransportException e){}
			 catch(TException e){}

		    System.out.println("Multicast done");

			while(true){
				synchronized (reqQueue){
					TransferRequest temp = (TransferRequest) reqQueue.peek();
					try {

						while (!temp.getReqID().equals(requestID) || (temp.getAckCount() != nodeCount - 1) ){
							reqQueue.wait();
							temp = (TransferRequest) reqQueue.peek();
						}

					}catch(InterruptedException e){}

					TransferRequest headReq = (TransferRequest) reqQueue.remove();

					int src = headReq.getSourceAccUID();
					int target = headReq.gettargetAccUID();
					int amt = headReq.getAmount();

					String res = bankhandler.transfer(src,target,amt);
					reqQueue.notifyAll();
					break;
				}
			}
	}


	@Override
	public String multi_transfer (int uID, int targuID, int amount, int timestamp, int serverid) {
		System.out.println("Inside multi_transfer of server: " + serverid);
		String currentRequest;
		TransferRequest transreq;
		String res = "Failed";

		synchronized (reqQueue) {
			clock.SetClockValueForReceive(timestamp);
			writer.println("CLNT-ID" + "	CLIENT_REQ	" + System.currentTimeMillis() + "	[" + clock.getClockValue() + "," + getID() + "]" + "	Transfer Operation" + "	Source ID: " + uID + "	Target ID: " + targuID + "	Amount " + amount);
			writer.flush();
			reqID += 1;
			currentRequest = getID() + "_" + reqID;
			transreq = new TransferRequest("TransferRequest", uID, targuID, amount, clock.getClockValue(), 0, currentRequest);
			reqQueue.add((Request) transreq);
		}
		try{
				for (int i = 0; i < nodeCount - 1; i++) {
					String hostname = serverlist.get(i).hostname;
					int portnumber = serverlist.get(i).portnumber;

					TTransport transport;
					transport = new TSocket(hostname, portnumber);
					transport.open();

					TProtocol protocol = new  TBinaryProtocol(transport);

					ReplicatedBankService.Client client = new ReplicatedBankService.Client(protocol);
					clock.SetClockValueForSend();
					client.multi_transfer_server(uID, targuID, amount, clock.getClockValue() , id, currentRequest);

					System.out.println("serverid: " + serverid + " multi_transfer complete");
					transport.close();

					}
		} catch (TTransportException e) {
					e.printStackTrace();
				} catch (TException e) {
					e.printStackTrace();
		}

		while(true){
			synchronized (reqQueue){

				TransferRequest temp = (TransferRequest) reqQueue.peek();
				try {

					while (!temp.getReqID().equals(currentRequest) || (temp.getAckCount() != nodeCount - 1) ){
						reqQueue.wait();
						temp = (TransferRequest) reqQueue.peek();
					}

				}catch(InterruptedException e){}

				TransferRequest headReq = (TransferRequest) reqQueue.remove();
				writer.println("CLNT-ID" + "	PROCESS	" + System.currentTimeMillis() + "	[" + clock.getClockValue() + "," + getID() + "]");
				writer.flush();
				int src = headReq.getSourceAccUID();
				int target = headReq.gettargetAccUID();
				int amt = headReq.getAmount();

				res = bankhandler.transfer(src,target,amt);
				reqQueue.notifyAll();
				break;
			}
		}
		System.out.println("Client response sent");
		return res;


	}


	@Override
	public void multi_deposit_ack(int reqtimeStamp, int serverid) {

	}

	@Override
	public void multi_getBalance_ack (int reqTimeStamp, int serverid) {

	}

	@Override
	public void multi_transfer_ack	(String requestID, int serverid, int timestamp) {
		System.out.println("Ack received");

		Object[] requestArray;
		synchronized (reqQueue) {
			clock.SetClockValueForReceive(timestamp);
			requestArray = reqQueue.toArray();


			for (int i = 0; i < reqQueue.size(); i++) {
				TransferRequest req = (TransferRequest) requestArray[i];

				if (req.getReqID().equals(requestID)) {
					req.setAckCount();

					if (req.getAckCount() == nodeCount - 1) {
						reqQueue.notifyAll();
					}

					break;
				}
			}
		}
	}
}
