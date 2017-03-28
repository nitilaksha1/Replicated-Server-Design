struct TimeStamp {
    1: required i32 timestamp;
    2: required i32 serverid;
}

service ReplicatedBankService {

   string 	multi_deposit 			(1:i32 uID, 2:i32 amount, 3:TimeStamp timestamp, 4:i32 serverid),
   i32 		multi_getBalance 		(1:i32 uID, 2:TimeStamp timestamp, 3:i32 serverid),
   string 	multi_transfer 			(1:i32 srcuID, 2:i32 targuID, 3:i32 amount, 4:TimeStamp timestamp, 5:i32 serverid, 6:i64 clientid),
   void 	multi_deposit_ack		(1:TimeStamp reqTimeStamp, 2:i32 serverid),
   void 	multi_getBalance_ack 	(1:TimeStamp reqTimeStamp, 2:i32 serverid),
   void 	multi_transfer_ack 		(1:string requestID, 2:i32 serverid, 3:TimeStamp timestamp),
   oneway void multi_transfer_server(1:i32 srcuID, 2:i32 targuID, 3:i32 amount, 4:TimeStamp timestamp, 5:i32 serverid, 6:string requestID, 7:i64 clientid)
   oneway  void     halt            ()
   oneway void     stop_execution    (1:TimeStamp timestamp)
   oneway   void     release        (1:string requestID)
   
}

