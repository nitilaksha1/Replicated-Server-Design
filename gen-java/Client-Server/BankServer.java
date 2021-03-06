import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportFactory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;

import java.io.*;
import java.util.*;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
public class BankServer {

    public static ReplicatedServerHandler handler;
    public static ReplicatedBankService.Processor processor;
    public static int serverportnumber;
    public static ArrayList<Integer> accList;

    public BankServer() {

        serverportnumber = 9999;
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.exit(1);
        }

        int id = Integer.parseInt(args[0]);

        String filename = args[1];
        int numberOfAccounts = 10;

        try {
            Scanner scan = new Scanner(new File(filename));
            scan.nextLine();

            while (scan.hasNext()) {
                String hostname = scan.next();
                int fid = scan.nextInt();
                int portnumber = scan.nextInt();

                if (id == fid) {
                    BankServer.serverportnumber = portnumber;
                    break;
                }

            }

            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        try {
            handler = new ReplicatedServerHandler();
            processor = new ReplicatedBankService.Processor(handler);

            handler.setID(id);
            handler.setNodeCountAndList(args[1]);
            handler.initLogFile("serverLog");

            BankHandler bh = new BankHandler();

            accList = new ArrayList<>();

            for (int i = 0; i < numberOfAccounts; i++) {
                int accID = bh.createAccount();
                accList.add(accID);
                String creationStatus = bh.deposit(accID, 1000);
            }
            handler.setAccountList(accList);

            Runnable simple = new Runnable() {
                public void run() {
                    someMethod(processor, BankServer.serverportnumber, handler);
                }
            };

            new Thread(simple).start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void someMethod(ReplicatedBankService.Processor processor, int portnumber, ReplicatedServerHandler handler) {
        try {
            TTransportFactory factory = new TFramedTransport.Factory();
            TServerTransport serverTransport = new TServerSocket(portnumber);
            TServer server = new TThreadPoolServer(new Args(serverTransport).processor(processor));
            System.out.println("Starting the simple server...");
            server.serve();
            handler.closeLogFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



