service ReplicatedBankService {

   string 	multi_deposit 			(1:i32 uID, 2:i32 amount, 3:i32 timestamp),
   i32 		multi_getBalance 		(1:i32 uID, 2:i32 timestamp),
   string 	multi_transfer 			(1:i32 srcuID, 2:i32 targuID, 3:i32 amount, 4:i32 timestamp),
   void 	multi_deposit_ack		(1:i32 reqTimeStamp),
   void 	multi_getBalance_ack 	(1:i32 reqTimeStamp),
   void 	multi_transfer_ack 		(1:i32 reqTimeStamp)
   
}

