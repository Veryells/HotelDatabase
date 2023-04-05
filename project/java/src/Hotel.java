import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math

public class Hotel {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }
   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if



      Greeting();
      Hotel esql = null;
      try{
         String nothing = "0"; //dead
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         String usertype = "";
         int currID;
         esql = new Hotel (dbname, dbport, user, "");
   	 boolean admin = false;
         boolean keepon = true;
         String mag = "manager";
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: {authorisedUser = LogIn(esql); 
                        usertype = Getusertype(esql,authorisedUser);
                        //System.out.println(usertype.length());
                        //System.out.printf("ID: %stest",authorisedUser);                                               
                        //System.out.printf("Type: %s",usertype);
                        break;}
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null && (usertype.equals("manager   ") || usertype.equals("admin     "))) {//why
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");
                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql,authorisedUser); break;
                   case 4: viewRecentBookingsfromCustomer(esql,authorisedUser); break;
                   case 5: updateRoomInfo(esql,authorisedUser); break;
                   case 6: viewRecentUpdates(esql,authorisedUser); break;
                   case 7: viewBookingHistoryofHotel(esql,authorisedUser); break;
                   case 8: viewRegularCustomers(esql,authorisedUser); break;
                   case 9: placeRoomRepairRequests(esql,authorisedUser); break;
                   case 10: viewRoomRepairHistory(esql,authorisedUser); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
            if (authorisedUser != null && usertype.equals("customer  ")) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");
                //the following functionalities basically used by managers
                /*System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");*/

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql,authorisedUser); break;
                   case 4: viewRecentBookingsfromCustomer(esql,authorisedUser); break;
                   /*case 5: updateRoomInfo(esql,authorisedUser); break;
                   case 6: viewRecentUpdates(esql,authorisedUser); break;
                   case 7: viewBookingHistoryofHotel(esql,authorisedUser); break;
                   case 8: viewRegularCustomers(esql,authorisedUser); break;
                   case 9: placeRoomRepairRequests(esql,authorisedUser); break;
                   case 10: viewRoomRepairHistory(esql,authorisedUser); break;*/
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
			String query = String.format("INSERT INTO Users (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         String query = String.format("SELECT * FROM Users WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewHotels(Hotel esql) {
  	try{
         System.out.print("\tEnter Latatude:");
         String Lat = in.readLine();
         System.out.print("\tEnter Longitude:");
         String Long = in.readLine();
	 String query = String.format("SELECT hotelID\t, hotelName\t,latitude\t,longitude\t,dateEstablished FROM Hotel WHERE calculate_distance(latitude, longitude, '%s', '%s') <= 30", Lat, Long);
	List<List<String>> results = esql.executeQueryAndReturnResult(query);
        System.out.println("Hotel_ID\tHotel_Name\t \t \tLatitude\tLongitude\tDate_Established");
	for (List<String> row : results) {
             System.out.printf("%s\t \t%s\t%s\t%s\t%s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4));
        }
	System.out.println("Total row(s): " + results.size());
         return;
	}catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }

   }
   public static void viewRooms(Hotel esql) {
  	try{
         System.out.print("\tEnter HotelId:");
         String Id = in.readLine();
	 System.out.print("\tEnter Date(Month/Day/Year):");
         String Date = in.readLine();
	 String query = String.format("SELECT Rooms.roomNumber, Rooms.price, CASE WHEN EXISTS (SELECT 1 FROM RoomBookings WHERE RoomBookings.hotelID = '%s' AND RoomBookings.roomNumber = Rooms.roomNumber AND RoomBookings.bookingDate = '%s') THEN 'Booked' ELSE 'Available' END AS availability FROM Rooms WHERE Rooms.hotelID = '%s'",Id,Date,Id);
	 List<List<String>> results = esql.executeQueryAndReturnResult(query);
        System.out.println("RoomNumber\tPrice \tAvalability");
        for (List<String> row : results) {
             System.out.printf("%s\t \t%s\t%s\n", row.get(0), row.get(1), row.get(2));
        }
        System.out.println("Total row(s): " + results.size());
         return;
        }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      } 
   }

   public static void bookRooms(Hotel esql,String user) {
   	try{
	 System.out.print("\tEnter HotelId:");
         String Id = in.readLine();
	 System.out.print("\tEnter Room Number:");
         String Room = in.readLine();
	 System.out.print("\tEnter Date(Month/Day/Year) of your stay:");
         String Date = in.readLine();
	 String query = String.format("SELECT * FROM RoomBookings WHERE hotelID = '%s' AND roomNumber = '%s'  AND bookingDate = '%s' ",Id,Room,Date);
	int check = esql.executeQuery(query);
	if(check > 0){//checks if its already booked
   System.out.print("\tRoom is already booked:\n");
   return;
	}else{
	query = String.format("SELECT * FROM Hotel WHERE hotelID = '%s'",Id);
	check = esql.executeQuery(query);
	if(check > 0){//checks if hotel exists
	query = String.format("SELECT * FROM Rooms WHERE hotelID = '%s' AND roomNumber = '%s'",Id,Room);
   check = esql.executeQuery(query);
	if(check > 0){//checks if room exists

    query = String.format("INSERT INTO RoomBookings (customerID, hotelID, roomNumber, bookingDate) VALUES ('%s','%s', '%s', '%s')", user, Id, Room, Date);

   esql.executeUpdate(query);
   System.out.println ("Room Booked\n");

   query = String.format("SELECT price FROM Rooms WHERE hotelID = '%s' AND roomNumber = '%s'",Id,Room);
   List<List<String>> results = esql.executeQueryAndReturnResult(query);
   for (List<String> row : results) {
             System.out.printf("Price: %s\n", row.get(0));
   }
   return;
   }
   System.out.print("\tThe Room does not exist in that hotel:\n");
	return;
	}
	System.out.print("\tThat Hotel does not exist nice try bud:\n");
	return;
	}
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      } 

   }

   public static void viewRecentBookingsfromCustomer(Hotel esql,String user) {
      try{
      String query = String.format("SELECT rb.hotelID, rb.roomNumber, r.price, rb.bookingDate FROM RoomBookings rb JOIN Rooms r ON rb.hotelID = r.hotelID AND rb.roomNumber = r.roomNumber WHERE rb.customerID = '%s' ORDER BY rb.bookingDate DESC LIMIT 5;",user);
       List<List<String>> results = esql.executeQueryAndReturnResult(query);
       System.out.printf("HotelId\tRoomNumber\tPrice\tbookingDate\n");
   for (List<String> row : results) {
             System.out.printf("%s\t%s\t \t%s\t%s\n", row.get(0),row.get(1),row.get(2),row.get(3));
   }
   return;
  }
      catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      } 
   }



   public static void updateRoomInfo(Hotel esql, String cID) {
      try{
         System.out.print("\tEnter hotel ID:");
         String hID = in.readLine();

         String check = String.format("SELECT managerUserID FROM Hotel WHERE hotelID = %s", hID);
         List<List<String>> bl = esql.executeQueryAndReturnResult(check);
         
         int tmp = esql.executeQuery(check); //Checks if hotel id exists
         if(tmp == 0){System.out.print("hotel ID does not exist\n");return;}

         //checks if manager manages that hotel
         if(!bl.get(0).get(0).equals(cID))
         {
            System.out.print("Don't have access to this hotel. \n");
            return;  
         }

         System.out.print("\tEnter room number:");
         String rNum = in.readLine();
         
         
         String check2 = String.format("SELECT roomNumber FROM Rooms WHERE roomNumber = %s", rNum);
         int tmp2 = esql.executeQuery(check2); //Checks if room number exists
         if(tmp2 == 0){System.out.print("Room number does not exist\n");return;}
         
         System.out.print("\tEnter new price:");
	      String pricetmp = in.readLine();		
         System.out.print("\tEnter new URL:");
		   String imagetmp = in.readLine();		


         String query = String.format("UPDATE Rooms SET price = %s, imageURL = %s WHERE hotelID = %s AND roomNumber = %s", pricetmp, imagetmp, hID, rNum);
    
	      esql.executeUpdate(query);
         System.out.println("Room updated!\n");

         /*String uNum = String.format("SELECT updateNumber FROM RoomUpdatesLog ORDER BY updateNumber DESC LIMIT 1");
	      List<List<String>> upNum = esql.executeQueryAndReturnResult(uNum);
         int reqNum = Integer.parseInt(rNum.get(0).get(0));
         reqNum += 1;
         String reqNum2 = Integer.toString(reqNum);*/

         String query2 = String.format("INSERT INTO RoomUpdatesLog (managerID,hotelID,roomNumber,updatedON) VALUES ( %s, %s, %s, CURRENT_TIMESTAMP)", cID, hID, rNum);
    
	      esql.executeUpdate(query2);
         System.out.println("Logs updated!\n");

    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void viewRecentUpdates(Hotel esql, String cID) {
   try{

      String query = String.format("SELECT * FROM RoomUpdatesLog WHERE managerID = %s ORDER BY updatedOn DESC LIMIT 5", cID);
      
      List<List<String>> results = esql.executeQueryAndReturnResult(query);
         System.out.println("Printing last 5 updates: \n");
         System.out.println("Update Number\tHotel ID\tRoom Number\tUpdate Date");
      for (List<String> row : results) {
               System.out.printf("%s\t \t%s\t \t%s\t \t%s\t\n", row.get(0), row.get(2), row.get(3), row.get(4));
         }
      System.out.println("Total row(s): " + results.size());
            return;
      }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
         }
      }

   public static void viewBookingHistoryofHotel(Hotel esql, String cID) {
      try{
         System.out.print("\tEnter start date of bookings:");
         String st = in.readLine();
         System.out.print("\tEnter end date of bookings:");
         String nd = in.readLine();

         String query = String.format("SELECT rb.bookingID, u.name, rb.hotelID, rb.roomNumber, rb.bookingDate FROM RoomBookings rb JOIN Users u ON rb.customerID = u.userID JOIN Hotel h ON h.hotelID = rb.hotelID WHERE h.managerUserID = '%s' AND rb.bookingDate >= '%s' AND rb.bookingDate <= '%s'",cID, st, nd);
    
	List<List<String>> results = esql.executeQueryAndReturnResult(query);
        System.out.println("Booking ID\t Name\t \t \t \t \t \t Hotel ID\tRoom Number\t Booking Date");
    for (List<String> row : results) {
             System.out.printf("%s\t \t %s\t %s\t%s\t %s\t\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4));
        }
    System.out.println("Total row(s): " + results.size());
         return;
    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
   public static void viewRegularCustomers(Hotel esql, String cID) {
      try{
         System.out.print("\tEnter hotel ID:");
         String hID = in.readLine();

         String check = String.format("SELECT managerUserID FROM Hotel WHERE hotelID = %s", hID);
         List<List<String>> bl = esql.executeQueryAndReturnResult(check);
         
         int tmp = esql.executeQuery(check); //Checks if hotel id exists
         if(tmp == 0){System.out.print("hotel ID does not exist\n");return;}
         
         //checks if manager manages that hotel
         if(!bl.get(0).get(0).equals(cID))
         {
            System.out.print("Don't have access to this hotel. \n");
            return;  
         }

         String query = String.format("SELECT u.name, COUNT(*) AS num_book FROM USERS u JOIN RoomBookings rb ON rb.customerID = u.userID WHERE rb.hotelID = %s AND u.userType = 'customer' GROUP BY u.name ORDER BY num_book DESC LIMIT 5", hID);
    
	      List<List<String>> results = esql.executeQueryAndReturnResult(query);
         System.out.println("User Name\t \t \t \t \tCount");
         for (List<String> row : results) {
            System.out.printf("%s\t%s\t\n", row.get(0), row.get(1));
         }
         System.out.println("Total row(s): " + results.size());
         return;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }

   }

public static void placeRoomRepairRequests(Hotel esql, String cID) {
      try{
         System.out.print("\tEnter hotel ID:");
         String hID = in.readLine();

         String check = String.format("SELECT managerUserID FROM Hotel WHERE hotelID = %s", hID);

         List<List<String>> bl = esql.executeQueryAndReturnResult(check);
      
         int tmp = esql.executeQuery(check); //Checks if hotel id exists
         if(tmp == 0){System.out.print("hotel ID does not exist\n");return;}

         //checks if manager manages that hotel
         if(!bl.get(0).get(0).equals(cID))
         {
            System.out.print("Don't have access to this hotel. \n");
            return;  
         }

         System.out.print("\tEnter room number:");
         String rmNum = in.readLine();
         System.out.print("\tEnter company ID:");
         String cmpID = in.readLine();

	/* add one to repaid id and request id*/
         /*String rqNum = String.format("SELECT requestNumber FROM RoomRepairRequests ORDER BY requestNumber DESC LIMIT 1");
         String rpID = String.format("SELECT repairID FROM RoomRepairs ORDER BY repairID DESC LIMIT 1");
	      List<List<String>> rNum = esql.executeQueryAndReturnResult(rqNum);
	      List<List<String>> rID = esql.executeQueryAndReturnResult(rpID);
         int reqNum = Integer.parseInt(rNum.get(0).get(0));
         int repID = Integer.parseInt(rID.get(0).get(0));
         reqNum += 1;
         repID += 1;
         String reqNum2 = Integer.toString(reqNum);
         String repID2 = Integer.toString(repID);*/
     
     
         String query1 = String.format("INSERT INTO RoomRepairs VALUES (%s, %s, %s, CURRENT_TIMESTAMP)", cmpID, hID, rmNum);
         esql.executeQuery(query1);

         String query2 = String.format("INSERT INTO RoomRepairRequests VALUES (%s, %s)", cID, cmpID);
         esql.executeQuery(query2);
	/*esql.executeQuery*/
    

    System.out.println("Room repair request sent");
         return;
    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static void viewRoomRepairHistory(Hotel esql, String cID) {
      try{
     String query = String.format("SELECT rr.repairID, ru.hotelID, ru.roomNumber, ru.updatedOn FROM RoomUpdatesLog ru JOIN Hotel h ON h.hotelID = ru.hotelID JOIN RoomRepairRequests rr ON rr.managerID = ru.managerID WHERE ru.managerID = %s ",cID);
    
	List<List<String>> results = esql.executeQueryAndReturnResult(query);
        System.out.println("Company ID\t Hotel ID\t Room Number\t Repair Date");
    for (List<String> row : results) {
             System.out.printf("%s\t \t %s\t \t %s\t \t %s\t\n", row.get(0), row.get(1), row.get(2), row.get(3));
        }
    System.out.println("Total row(s): " + results.size());
         return;
    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }

   public static String Getusertype(Hotel esql,String user){
    try{
      String query = String.format("SELECT userType FROM Users WHERE userID = '%s'",user);
      List<List<String>> results = esql.executeQueryAndReturnResult(query);
       
      for (List<String> row : results) {
      //System.out.printf("%s\n",results.get(0).get(0));
      //System.out.printf("%s\n",results.get(0).get(0).length());

         return row.get(0);
      }
      return null;
    }
      catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      } 
   }
   }
   //end Hotel


