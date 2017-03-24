
service BankService {

   i32 createAccount(),
   string deposit (1:i32 uID, 2:i32 amount),
   i32 getBalance (1:i32 uID),
   string transfer (1:i32 srcuID, 2:i32 targuID, 3:i32 amount)

}

