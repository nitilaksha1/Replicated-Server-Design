import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.*;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

class Servers{
	  public int portnumber;
      public String hostname;

      public Servers(int port, String host){
      	  portnumber = port;
		  hostname = host;
      }
}
public class BankClient {
	  private ArrayList<Integer> accountlist = new ArrayList<Integer>();
	  public static Random rand = new Random();
          public static Object lock = new Object();
	  public static BankService.Client client;
	  public static boolean isUsed = false;
      public static ArrayList<String> serverlist = new ArrayList<>();
	  public static List<Thread> th = new ArrayList<Thread>();
	  ArrayList<Integer> getAccountList () 
	  {
		return accountlist;
	  }

	  void newAccountRequest(BankService.Client client, PrintWriter writer) throws TException{

	  	int val = client.createAccount();
                accountlist.add(val);
	  }

	  void depositRequest(BankService.Client client, PrintWriter writer, int id, int amount) throws TException{

	  	client.deposit(id,amount);
	  }

	  int balanceRequest(BankService.Client client, PrintWriter writer, int id) throws TException{

	  	return client.getBalance(id);
	  }

	  void transferRequest(BankService.Client client, PrintWriter writer, int src, int target, int amount) throws TException{


	  	String s= client.transfer(src,target,amount);

		if (s.equals("FAILED")) {

			writer.println("Request: Transfer " + "Parameters: " + src + " "+target + " " + amount + " Request Status: FAILED");
			writer.flush();
		}


	  }

      public void listServers(int filename){
		    
			Scanner scan = new Scanner (new File(filename));
			scan.nextLine();
		
			while (scan.hasNext()) {
				String hostname = scan.next();
				int fid = scan.nextInt();
				int portnumber = scan.nextInt();
			    serverlist.add(new Servers(portnumber,hostname));
				if (id == fid) {
				serverportnumber = portnumber;
				break;
				}
				
			}
		
			//Rest of server logic
			scan.close();

		
	  }

      public Servers getRamdomServer(){
              
            return serverlist.get(rand.nextInt(serverlist.size())); 
	  }


	public static void main(String[] args){


	  try{
			final BankClient bc = new BankClient();
            final String hostname = args[0];
			final int portname = 9990;
			int threadCount = Integer.parseInt(args[2]);
			final int iterationCount = 100;
            final String filename = args[1];
            bc.listServers(filename);
            


			
   	  for(int i=0; i < 100; i++)
      {

			String hostname = bc.getRandomServer();
			TTransport transport;
			transport = new TSocket(hostname, portname);
  			transport.open();

  			TProtocol protocol = new  TBinaryProtocol(transport);
  			client = new BankService.Client(protocol);
  			final PrintWriter writer = new PrintWriter("clientLog.txt", "UTF-8");
  			

			for (int i = 0; i < 100; i++) {
				ArrayList<Integer> list = bc.getAccountList();
				int accid = list.get(i);
				bc.depositRequest(client, writer, accid, 100);
			}

			int sum = 0;

			for (int i = 0; i < 100; i++) {
				ArrayList<Integer> list = bc.getAccountList();
				int accid = list.get(i);
				int bal = bc.balanceRequest(client, writer, accid);

				if (bal == -1)
					bal = 0;

				sum += bal;
			}

			System.out.println("Sum of balances = " + sum);
			writer.println("Sum of balances = " + sum);
			writer.println();	
			writer.flush();		
			//ExecutorService threads = Executors.newFixedThreadPool(threadCount);
			
			for (int i = 0; i < threadCount; i++) {

			      Thread t = new Thread( new Runnable() 
				{				
						public void run () {
				
						try {           
						  		TTransport transport;
								transport = new TSocket(hostname, portname);
					  			transport.open();

					  			TProtocol protocol = new  TBinaryProtocol(transport);
					  			BankService.Client client = new BankService.Client(protocol);


							      for(int i=0; i < iterationCount; i++){

									int a = rand.nextInt(bc.getAccountList().size());
                                             				int b = rand.nextInt(bc.getAccountList().size());                                                                	
									ArrayList<Integer> arr = bc.getAccountList();
									bc.transferRequest (client, writer, arr.get(a), arr.get(b), 10);	
								}

						transport.close();



						} catch (TTransportException e) {

							e.printStackTrace();
						}catch (TException e){ e.printStackTrace();}
					      
					}
				});
				t.start();
				th.add(t);
			
			}

			sum = 0;

			for(Thread t : th){
				try{t.join();}catch(InterruptedException e){}
			}
         }
			for (int i = 0; i < 100; i++) {
				ArrayList<Integer> list = bc.getAccountList();
				int accid = list.get(i);
				int bal = bc.balanceRequest(client, writer, accid);

				if (bal == -1)
					bal = 0;

				sum += bal;
			}

			System.out.println("Sum of balances = " + sum);
			writer.println("Sum of balances = " + sum);
			writer.println();
			writer.flush();
			transport.close();			
			writer.close();
	
	  }catch(TException e){
	  	e.printStackTrace();
	  }catch(FileNotFoundException e){
                e.printStackTrace(); 
          }catch(UnsupportedEncodingException e){ e.printStackTrace();}
	  
      

	}
}
