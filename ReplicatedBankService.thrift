service ReplicatedBankService {

   string 	multi_deposit 			(1:i32 uID, 2:i32 amount, 3:i32 timestamp, 4:i32 serverid),
   i32 		multi_getBalance 		(1:i32 uID, 2:i32 timestamp, 3:i32 serverid),
   string 	multi_transfer 			(1:i32 srcuID, 2:i32 targuID, 3:i32 amount, 4:i32 timestamp, 5:i32 serverid),
   void 	multi_deposit_ack		(1:i32 reqTimeStamp, 2:i32 serverid),
   void 	multi_getBalance_ack 	(1:i32 reqTimeStamp, 2:i32 serverid),
   void 	multi_transfer_ack 		(1:string requestID, 2:i32 serverid),
oneway void multi_transfer_server(1:i32 srcuID, 2:i32 targuID, 3:i32 amount, 4:i32 timestamp, 5:i32 serverid, 6:string requestID)


   
}

