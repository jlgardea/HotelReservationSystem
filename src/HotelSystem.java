import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.System;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class HotelSystem {
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) {
        DBFunctions hotel_db = null;

        try {
            hotel_db = new DBFunctions();
            hotel_db.connect_to_db("hotel", "5432", "jgard046", "UCR2023");

            boolean exit = false;
            while (!exit) {
                int menuChoice = mainMenu();
                String currUser = null;
                switch (menuChoice) {
                    case 0:
                        exit = true;
                        break;
                    case 1:
                        CreateUser(hotel_db);
                        break;
                    case 2:
                        currUser = login(hotel_db);
                        break;
                    default:
                        System.out.println("That choice is unrecognized.");
                        break;
                }

                if (currUser != null) {
                    boolean loggedIn = true;
                    while (loggedIn) {
                        menuChoice = userMenu(hotel_db, currUser);
                        switch (menuChoice) {
                            case 0:
                                loggedIn = false;
                                break;
                            case 1:
                                viewHotels(hotel_db);
                                break;
                            case 2:
                                viewRooms(hotel_db);
                                break;
                            case 3:
                                bookRooms(hotel_db, currUser);
                                break;
                            case 4:
                                viewRecentBookingsFromCustomers(hotel_db, currUser);
                                break;
                            case 5:
                                updateRoomInfo(hotel_db, currUser);
                                break;
                            case 6:
                                viewRecentUpdates(hotel_db, currUser);
                                break;
                            case 7:
                                viewBookingHistoryOfHotel(hotel_db, currUser);
                                break;
                            case 8:
                                viewRegularCustomers(hotel_db, currUser);
                                break;
                            case 9:
                                placeRoomRepairRequests(hotel_db, currUser);
                                break;
                            case 10:
                                viewRoomRepairHistory(hotel_db, currUser);
                                break;
                            default:
                                System.out.println("That choice is unrecognized.");
                                break;
                        }
                    }
                }
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally{
            try{
                if(hotel_db != null){
                    hotel_db.cleanup();
                }
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Displays the main log-in menu
     * @return menu selection
     */
    public static int mainMenu(){
        int selection;
        while(true){
            System.out.println();
            System.out.println(" Log In Menu ");
            System.out.println("-------------");
            System.out.println("1 - Create a new user");
            System.out.println("2 - Log in");
            System.out.println("0 - Quit");
            System.out.println();
            System.out.print("Enter your choice: ");
            try{
                selection = Integer.parseInt(in.readLine());
                break;
            }catch(Exception e){
                System.out.println("Your input choice is invalid.");
            }
        }
        return selection;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * @return true if userType is customer; false otherwise
     */
    public static boolean isACustomer(DBFunctions hotel_db, String currUser){
        List<List<String>> userType;
        String currUserType;
        boolean customer = true;

        try {
            //query to get the currUser userType
            String q = String.format("SELECT userType " +
                    "FROM Users " +
                    "WHERE userID = '%s';", currUser);

            userType = hotel_db.executeQueryAndReturnResult(q);
            currUserType = userType.get(0).get(0).replaceAll("\\s", "");

            //determine if the userType is a manager
            customer = !currUserType.equals("manager");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return customer;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * @return menuSelection
     * Displays menu based off of useType - customer/manager and verifies selection is valid
     */

    public static int userMenu(DBFunctions hotel_db, String currUser){
        int selection;
        boolean customer = isACustomer(hotel_db, currUser);

        while(true){
            //display items for userType "customer"
            if(customer){
                System.out.println();
                System.out.println("  User Menu   ");
                System.out.println("--------------");
                System.out.println("1 - View Hotels (within 30 Units)");
                System.out.println("2 - View Rooms");
                System.out.println("3 - Book a Room");
                System.out.println("4 - View recent booking history");
            }
            //displays items for userType "manager"
            else{
                System.out.println();
                System.out.println("5 - Update Room Information");
                System.out.println("6 - View 5 recent room updates info");
                System.out.println("7 - View booking history of the hotel");
                System.out.println("8 - View 5 regular customers");
                System.out.println("9 - Place room repair request to a company");
                System.out.println("10 - View room repair requests history");
            }
            System.out.println("0 - Logout");
            System.out.println();
            System.out.print("Enter your choice: ");

            //reads user input and verifies the selection is valid
            try{
                selection = Integer.parseInt(in.readLine());

                if(selection == 0){
                    return selection;
                }
                else if(customer && selection > 4){
                    System.out.println("Your input choice is invalid.");
                    continue;
                }
                else if(!customer && selection < 5 ){
                    System.out.println("Your input choice is invalid.");
                    continue;
                }
                break;
            }catch(Exception e){
                System.out.println("Your input choice is invalid.");
            }
        }
        return selection;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * Create a user using input information
     */
    public static void CreateUser(DBFunctions hotel_db){
        try{
            //get name and password from user
            System.out.print("Enter name: ");
            String name = in.readLine();

            System.out.print("Enter password: ");
            String password = in.readLine();

            String type = "Customer";

            //insert new user record into Users and display successful message
            String query = String.format("INSERT INTO Users (name, password, userType) " +
                    "VALUES ('%s', '%s', '%s')", name, password, type);
            hotel_db.executeUpdate(query);
            int newUserID = hotel_db.getLastSequenceID("SELECT last_value FROM users_userID_seq");

            System.out.printf("User Successfully Created - UserID: %d", newUserID);
            System.out.println();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @return string userID
     * verify userID and password match record in users using user input
     */
    public static String login(DBFunctions hotel_db){
        try{
            //get userID and password from user
            System.out.print("Enter UserID: ");
            String userID = in.readLine();

            System.out.print("Enter Password: ");
            String password = in.readLine();

            //query to check if user exists in Users
            String q = String.format("SELECT * " +
                    "FROM Users " +
                    "WHERE userID = '%s' AND password = '%s'", userID, password);
            int userNumber = hotel_db.executeQuery(q);

            //if users exists then return userID
            if(userNumber > 0){
                return userID;
            }
            //else let them know their information is incorrect
            else {
                System.out.println("Please make sure you are using your userID and correct password.");
            }
            return null;
        }catch(Exception e) {
            System.out.println("There is no account associated with that login information.");
            System.out.println("Please make sure you are using your userID and correct password.");
            return null;
        }
    }

    /***
     *
     * @param lat1 latitude for hotel1/userinput
     * @param long1 longitude for hotel1/userinput
     * @param lat2 latitude for hotel1/userinput
     * @param long2 longitude for hotel1/userinput
     * @return the distance between hotel1 and userinput
     */
    public static double calculateDistance (double lat1, double long1, double lat2, double long2){
        double t1 = (lat1 - lat2) * (lat1 - lat2);
        double t2 = (long1 - long2) * (long1 - long2);
        return Math.sqrt(t1 + t2);
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @return valid hotelID for selectedHotel
     */
    public static int getHotelID(DBFunctions hotel_db){
        int selectedHotel = -1;
        String q;
        boolean valid = false;


        //verify the selectedHotel is valid in hotel table; if not then keep requesting.
        while (!valid) {
            try {
                System.out.print("Enter the Hotel ID: ");
                selectedHotel = Integer.parseInt(in.readLine());

                //query to check if selectedHotel is in hotel table
                q = String.format("SELECT hotelID " +
                        "FROM Hotel " +
                        "WHERE hotelID = %s;", selectedHotel);

                if (hotel_db.executeQuery(q) == 0) {
                    System.out.println("There is no hotel with that ID. Please try again.");
                } else {
                    valid = true;
                }

            }catch (NumberFormatException e) {
                System.out.println("That is not a valid HotelID.");
            }catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return selectedHotel;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @param selectedHotel: hotelID from hotel
     * @return valid selection of roomNumber in selectedHotel
     */
    public static int getRoomNumber(DBFunctions hotel_db, int selectedHotel){
        int selectedRoom = -1;
        String q;
        boolean valid = false;

        //verify the roomNumber is in the selectedHotel and is valid. if not then keep requesting
        while (!valid) {
            try {
                System.out.print("Enter the Room Number: ");
                selectedRoom = Integer.parseInt(in.readLine());

                //query to check if roomNumber is in selectedHotel
                q = String.format("SELECT roomNumber " +
                        "FROM Rooms " +
                        "WHERE hotelID = %s AND roomNumber = %s;", selectedHotel, selectedRoom);

                if (hotel_db.executeQuery(q) == 0) {
                    System.out.println("That is not a valid room number.");
                } else {
                    valid = true;
                }
            }catch (NumberFormatException e) {
                System.out.println("That is not a valid room number");
            }catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return selectedRoom;
    }

    /**
     *
     * @param request: string to get type of request (requested, start, end)
     * @return verified date
     *
     */
    public static Date getDate(String request){
        Date enteredDate;
        DateFormat dateForm = DateFormat.getDateInstance(DateFormat.SHORT);
        dateForm.setLenient(false);

        //verify the dateEntered is valid if not keep requesting
        while(true) {
            System.out.printf("Enter %s date (MM/DD/YY): ", request);
            try {
                enteredDate = dateForm.parse(in.readLine());
                break;
            } catch (ParseException e) {
                System.out.println("The date entered is invalid. Please try again.");
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        return enteredDate;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @return int for selected maintenance company maintenanceCompanyID
     *
     * Gets the companyID from user input and verifies the maintenance companyID is in the table
     */
    public static int getCompanyID(DBFunctions hotel_db){
        int selectedCompany = -1;
        String q;
        boolean valid = false;

        while (!valid) {
            try {
                //gets user input for companyID
                System.out.print("Enter the Company ID: ");
                selectedCompany = Integer.parseInt(in.readLine());

                //query to see if companyID exists in MaintenanceCompany table
                q = String.format("SELECT companyID " +
                        "FROM MaintenanceCompany " +
                        "WHERE companyID = %s;", selectedCompany);

                //if it does not exist then ask for a correct companyID
                if (hotel_db.executeQuery(q) == 0) {
                    System.out.println("There is no company with that ID. Please try again");
                } else {
                    valid = true;
                }

            }catch (NumberFormatException e) {
                System.out.println("That is not a valid CompanyID.");
            }catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return selectedCompany;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * Displays hotels that are located within 30 units of user input latitude & longitude
     */
    public static void viewHotels(DBFunctions hotel_db){
        try {
            //get user input for latitude and longitude
            System.out.print("Enter your latitude: ");
            double userLat = Double.parseDouble(in.readLine());

            System.out.print("Enter longitude: ");
            double userLong = Double.parseDouble(in.readLine());

            //query to get all latitude and longitudes for hotels
            String q = "SELECT hotelName, latitude, longitude " +
                    "FROM hotel";
            List<List<String>> hotelResults = hotel_db.executeQueryAndReturnResult(q);
            int count = 0;

            //calculates the distance between user lat&long and hotel lat&long
            //if distance is less than 30 units it displays list
            System.out.println();
            System.out.println("Hotels found within 30 units:");
            for (List<String> hotelResult : hotelResults) {
                double hotelLat = Double.parseDouble(hotelResult.get(1));
                double hotelLong = Double.parseDouble(hotelResult.get(2));
                double distance = calculateDistance(userLat, userLong, hotelLat, hotelLong);

                if (distance <= 30) {
                    System.out.println(hotelResult.get(0));
                    count++;
                }
            }
            //if there are no hotels within 30 units it displays informational message
            if(count == 0){System.out.println("There were no hotels found within 30 units");}
        }catch(NumberFormatException e){
            System.out.println("That is not a valid number.");

        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     *
     * @param hotel_db: Connection to database
     * Displays rooms that are available at selectedHotel on enteredDate
     */
    public static void viewRooms(DBFunctions hotel_db){
        DateFormat dateForm = DateFormat.getDateInstance(DateFormat.SHORT);
        dateForm.setLenient(false);
        String q;

        //gets user input for selectedHotel and enteredDate
        int selectedHotel = getHotelID(hotel_db);
        Date enteredDate = getDate("requested");

        try {
            //query to get all available rooms from selectedHotels on enteredDate
            q = String.format("SELECT R.roomNumber, R.price " +
                    "FROM Rooms as R " +
                    "WHERE R.hotelID = '%s' AND R.roomNumber " +
                    "NOT IN (" +
                    "SELECT RB.roomNumber " +
                    "FROM RoomBookings AS RB " +
                    "WHERE RB.hotelID = '%s' AND RB.bookingDate = '%s') " +
                    "ORDER BY R.roomNumber;", selectedHotel, selectedHotel, enteredDate);

            if(hotel_db.executeQuery(q) == 0){
                System.out.printf("%n There are no hotel rooms available on %s.", dateForm.format(enteredDate));
            }
            else{

                System.out.printf("%n List of Rooms Available on %s %n", dateForm.format(enteredDate));
                System.out.println("-------------------------------");
                hotel_db.executeQueryAndPrintResults(q);
            }

        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Books Room using user input:
     *       selectedHotel
     *       selectedRoom
     *       enteredDate
     */
    public static void bookRooms(DBFunctions hotel_db, String currUser){
        DateFormat dateForm = DateFormat.getDateInstance(DateFormat.SHORT);
        dateForm.setLenient(false);
        String q;

        //get user input for Hotel, Room & Date
        int selectedHotel = getHotelID(hotel_db);
        int selectedRoom = getRoomNumber(hotel_db, selectedHotel);
        Date enteredDate = getDate("booking");

        try {
            //query to get all available rooms for selectedHotel on enteredDate
            q = String.format("SELECT R.roomNumber, R.price " +
                    "FROM Rooms as R " +
                    "WHERE R.hotelID = '%d' AND R.roomNumber " +
                    "NOT IN (" +
                    "SELECT RB.roomNumber " +
                    "FROM RoomBookings AS RB " +
                    "WHERE RB.hotelID = '%d' AND RB.bookingDate = '%s') " +
                    "ORDER BY R.roomNumber;", selectedHotel, selectedHotel, enteredDate);

            List<List<String>> availableRooms = hotel_db.executeQueryAndReturnResult(q);

            /* check to see if the selectedRoom is available for that date.
                if room is available then insert record into roomBookings
                else display a message saying room could not be booked
            */
            boolean booked = false;
            System.out.println();
            for (List<String> availableRoom : availableRooms) {
                if (Integer.parseInt(availableRoom.get(0)) == selectedRoom) {
                    q = String.format("INSERT INTO RoomBookings (customerID, hotelID, roomNumber, bookingDate) " +
                            "VALUES ('%s', '%d', '%d', '%s');",
                            currUser, selectedHotel, selectedRoom, enteredDate);
                    hotel_db.executeUpdate(q);
                    System.out.printf("Your room has been booked! The price for your stay is $%s.", availableRoom.get(1));
                    booked = true;
                }
            }
            if(!booked){
                System.out.println("Sorry your room could not be booked.");
                System.out.printf("Room %d at HotelID %d is not available on " +
                        "%s.", selectedRoom, selectedHotel, dateForm.format(enteredDate));
            }
            System.out.println();

        } catch (Exception e){
            System.err.println(e.getMessage());
        }

    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Displays the last 5 roomBookings for currUser
     */
    public static void viewRecentBookingsFromCustomers(DBFunctions hotel_db, String currUser){
            try {
                //query to get the last 5 roomBookings for currUser
                String q = String.format("SELECT RoomBookings.bookingDate, RoomBookings.hotelID, " +
                        "RoomBookings.roomNumber, Rooms.price " +
                        "FROM RoomBookings " +
                        "INNER JOIN Rooms " +
                        "ON RoomBookings.hotelID = Rooms.hotelID " +
                        "AND RoomBookings.roomNumber = Rooms.roomNumber " +
                        "WHERE RoomBookings.customerID = '%s' " +
                        "ORDER BY RoomBookings.bookingDate " +
                        "DESC " +
                        "LIMIT 5;", currUser);

                //displays last 5 roomBookings if they exist or information message
                if (hotel_db.executeQuery(q) > 0) {
                    System.out.println();
                    System.out.println("Here is your most recent booking history");
                    System.out.println("-----------------------------------------");
                    hotel_db.executeQueryAndPrintResults(q);
                }
                else{
                    System.out.println("You have no recent booking history.");
                }
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
    }

    /**
     * @param hotel_db: Connection to database
     * @param selectedHotel: hotel selection
     * @param currUser: String userID from Users for current logged-in user
     * @return true if currUser is the manager for the selected hotel; returns false otherwise
     */
    public static boolean userManagesHotel(DBFunctions hotel_db, int selectedHotel, String currUser){
        //check to see if currUser is the manager for selectedHotel
        try{
            String q = String.format("SELECT hotelID " +
                    "FROM Hotel " +
                    "WHERE managerUserID = %s " +
                    "AND hotelID = %d;", currUser, selectedHotel);
            //let user know they are no the manager and do not have access to this information
            if(hotel_db.executeQuery(q) == 0) {
                System.out.printf("Only the current manager of HotelID %s " +
                        "can access information for this hotel.", selectedHotel);
                System.out.println();
                return false;
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * displays menu for updateRoomInfo
     * @return int for user selection
     */
    public static int updateRoomSelectionMenu(){
        int selection = -1;
        System.out.println();
        System.out.println("UPDATE MENU");
        System.out.println("---------------------------");
        System.out.println("1 - Update Price");
        System.out.println("2 - Update Image URL");
        System.out.println();
        System.out.print("Enter your choice: ");

        //get selection from user input
        try {
            selection = Integer.parseInt(in.readLine());
            return selection;
        }catch(NumberFormatException e) {
            System.out.println("That is not a valid number.");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return selection;
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * @param selectedHotel: int hotelID from Hotel
     * @param selectedRoom: int roomID from Rooms for selectedHotel
     *
     *  Updates imageURL for record in rooms where roomID = selectedRoom and hotelID = selectedHotel
     */
    public static void updatePrice(DBFunctions hotel_db, String currUser, int selectedHotel, int selectedRoom){
        int newPrice;
        String q ;
        boolean valid = false;

        while (!valid) {
            try {
                //get the new price for the selectedRoom at the selectedHotel
                System.out.print("Enter the updated Price Amount: ");
                newPrice = Integer.parseInt(in.readLine());

                //query to update room record with new price
                q = String.format("UPDATE Rooms " +
                        "SET price = %d " +
                        "WHERE hotelID = %d AND roomNumber = %d;", newPrice, selectedHotel, selectedRoom);
                hotel_db.executeUpdate(q);

                //query to insert record into roomUpdatesLog after updating price
                q = String.format("INSERT INTO roomUpdatesLog (managerID, hotelID, roomNumber, updatedOn) " +
                        "VALUES (%s, %d, %d, CURRENT_TIMESTAMP);", currUser, selectedHotel, selectedRoom);
                hotel_db.executeUpdate(q);

                System.out.printf("The price for HotelID:%d , RoomID:%d has been updated to $%d.",
                        selectedHotel, selectedRoom, newPrice);
                System.out.println();

                valid = true;

            }catch (NumberFormatException e) {
                System.out.println("That is not a valid number");
            }catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     *
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * @param selectedHotel: int hotelID from Hotel
     * @param selectedRoom: int roomID from Rooms for selectedHotel
     *
     *  Updates imageURL for record in rooms where roomID = selectedRoom and hotelID = selectedHotel
     */
    public static void updateImageURL(DBFunctions hotel_db, String currUser, int selectedHotel, int selectedRoom){
        String newURL;
        String q;

        try {
            //get the new imageURL for the selectedRoom at the selectedHotel
            System.out.print("Enter the updated imageURL: ");
            newURL = in.readLine();

            //query to update room record with new imageURL
            q = String.format("UPDATE Rooms " +
                    "SET imageURL = %s " +
                    "WHERE hotelID = %d AND roomNumber = %d;", newURL, selectedHotel, selectedRoom);
            hotel_db.executeUpdate(q);

            //query to insert record into roomUpdatesLog after updating imageURL
            q = String.format("INSERT INTO roomUpdatesLog (managerID, hotelID, roomNumber, updatedOn) " +
                    "VALUES (%s, %d, %d, CURRENT_TIMESTAMP);",
                    currUser, selectedHotel, selectedRoom);
            hotel_db.executeUpdate(q);

            System.out.printf("The imageURL for HotelID:%d , RoomID:%d has been updated to %s.",
                    selectedHotel, selectedRoom, newURL);
            System.out.println();

        }catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Updated Room Information:
     *      calls updatePrice - update price for selectedRoom in selectedHotel
     *      calls updateImageURL - update url string for selectedRoom in selectedHotel
     * Restriction: currUser must be manager for selectedHotel
     */
    public static void updateRoomInfo(DBFunctions hotel_db, String currUser){
        int selectedHotel;
        boolean isHotelManager = false;

        //Check to see if currUser is the manager of selectedHotel
        do {
            selectedHotel = getHotelID(hotel_db);
            isHotelManager = userManagesHotel(hotel_db, selectedHotel, currUser);
        }while(!isHotelManager);

        //get selectedRoom
        int selectedRoom = getRoomNumber(hotel_db, selectedHotel);

        //get user selection for update
        int updateSelection = updateRoomSelectionMenu();
        switch(updateSelection){
            case 1: updatePrice(hotel_db, currUser, selectedHotel, selectedRoom); break;
            case 2: updateImageURL(hotel_db, currUser, selectedHotel, selectedRoom); break;
            case 0: break;
            default: System.out.println("That choice is unrecognized.");
        }

    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Displays the most recent 5 room updates entered by currUser
     */
    public static void viewRecentUpdates(DBFunctions hotel_db, String currUser){
        String q = String.format("SELECT updateNumber, hotelID, roomNumber, updatedOn " +
                "FROM roomUpdatesLog " +
                "WHERE managerID = %s " +
                "ORDER BY updateNumber " +
                "DESC " +
                "LIMIT 5;", currUser);

        try {
            if (hotel_db.executeQuery(q) > 0) {
                System.out.println();
                System.out.println("5 Most Recent Room Updates");
                System.out.println("-----------------------------------------");
                hotel_db.executeQueryAndPrintResults(q);
            }
            else{
                System.out.println("There is no update history.");
            }

        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Displays the most recent 20 booking history for ALL hotels managed by currUser for a selected date range
     */
    public static void viewBookingHistoryOfHotel(DBFunctions hotel_db, String currUser){
        //get the start and end dates for the range
        Date startDate = getDate("start");
        Date endDate = getDate("end");

        int selectedHotel;
        boolean isHotelManager = false;

        //Check to see if currUser is the manager of selectedHotel
        do {
            selectedHotel = getHotelID(hotel_db);
            isHotelManager = userManagesHotel(hotel_db, selectedHotel, currUser);
        }while(!isHotelManager);

        try{
            //query to get 20 recent bookings for all hotels managed by currUser
            String q = String.format("SELECT * " +
                    "FROM (SELECT * " +
                    "FROM roomBookings " +
                    "WHERE bookingDate BETWEEN '%s' AND '%s') as bookings " +
                    "WHERE hotelID " +
                    "IN (SELECT h.hotelID " +
                    "FROM Hotel as h " +
                    "WHERE h.managerUserID = %s) " +
                    "ORDER BY bookingDate " +
                    "DESC " +
                    "LIMIT 20;", startDate, endDate, currUser);

            System.out.println();
            if(hotel_db.executeQuery(q) > 0){
                System.out.println("Booking History");
                System.out.println("----------------------------");
                hotel_db.executeQueryAndPrintResults(q);
            }
            else{
                System.out.println("There is no recent booking history.");
            }

        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Displays the top 5 customers for the selected hotel
     * Restriction: currUser must be manager for selectedHotel
     */
    public static void viewRegularCustomers(DBFunctions hotel_db, String currUser){
        int selectedHotel;
        boolean isHotelManager = false;

        //Check to see if currUser is the manager of selectedHotel
        do {
            selectedHotel = getHotelID(hotel_db);
            isHotelManager = userManagesHotel(hotel_db, selectedHotel, currUser);
        }while(!isHotelManager);

        try{
            //query to get the top 5 customers for selectedHotel
            String q = String.format("SELECT userID, name " +
                    "FROM Users " +
                    "WHERE userID " +
                    "IN (SELECT RB.customerID " +
                    "FROM (SELECT * " +
                    "FROM roomBookings " +
                    "WHERE roomBookings.hotelID = %d) AS RB " +
                    "GROUP BY RB.customerID " +
                    "ORDER BY COUNT(RB.customerID) " +
                    "DESC " +
                    "LIMIT 5);", selectedHotel);

            if(hotel_db.executeQuery(q) > 0){
                System.out.println();
                System.out.printf("Top 5 Customers for HotelID %d", selectedHotel);
                System.out.println();
                System.out.println("-----------------------------------");
                hotel_db.executeQueryAndPrintResults(q);
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Inserts record into roomRepairs
     * Inserts records into roomRepairRequests
     * Restriction: currUser must be manager for selectedHotel
     */
    public static void placeRoomRepairRequests(DBFunctions hotel_db, String currUser){
        int selectedHotel;
        int selectedRoom;
        int selectedCompany;
        String q;
        boolean isHotelManager = false;

        //Check to see if currUser is the manager of selectedHotel
        do {
            selectedHotel = getHotelID(hotel_db);
            isHotelManager = userManagesHotel(hotel_db, selectedHotel, currUser);
        }while(!isHotelManager);

        //get room number and repair maintenance company ID
        selectedRoom = getRoomNumber(hotel_db, selectedHotel);
        selectedCompany = getCompanyID(hotel_db);

        try {
            //insert records into roomRepair; newRepairID = repairID
            q = String.format("INSERT INTO roomRepairs (companyID, hotelID, roomNumber, repairDate) " +
                "VALUES (%d, %d, %d, CURRENT_TIMESTAMP)", selectedCompany, selectedHotel, selectedRoom);
            hotel_db.executeUpdate(q);
            int newRepairID = hotel_db.getLastSequenceID("SELECT last_value FROM roomRepairs_repairID_seq;");

            //inserts records into roomRepairRequest; newRequestID = requestNumber
            q = String.format("INSERT INTO roomRepairRequest (managerID, repairID) " +
                    "VALUES (%s, %d);", currUser, newRepairID);
            hotel_db.executeUpdate(q);
            int newRequestID = hotel_db.getLastSequenceID("SELECT last_value FROM roomRepairRequest_requestNumber_seq;");

            //Confirms that records have been added
            System.out.printf("Request %d has been submitted for HotelID: %d, RoomID: %d.",
                    newRequestID, selectedHotel, selectedRoom);
            System.out.println();
            System.out.printf("Your repairID confirmation is %d.", newRepairID);
            System.out.println();

        }catch(Exception e){
            System.err.println(e.getMessage());
        }

    }

    /**
     * @param hotel_db: Connection to database
     * @param currUser: String userID from Users for current logged-in user
     * Displays 15 room repair records for selectedHotel
     * Restriction: currUser must be manager for selectedHotel
     */
    public static void viewRoomRepairHistory(DBFunctions hotel_db, String currUser){
        int selectedHotel;
        boolean isHotelManager = false;

        //Check to see if currUser is the manager of selectedHotel
        do {
            selectedHotel = getHotelID(hotel_db);
            isHotelManager = userManagesHotel(hotel_db, selectedHotel, currUser);
        }while(!isHotelManager);

        try{
            //query to get room repair records for hotel
            String q = String.format("SELECT companyID, hotelID, roomNumber, repairDate " +
                    "FROM roomRepairs " +
                    "WHERE hotelID " +
                    "IN (SELECT h.hotelID " +
                    "FROM Hotel as h " +
                    "WHERE h.managerUserID = %s AND h.hotelID = %d) " +
                    "ORDER BY repairID " +
                    "DESC " +
                    "LIMIT 15;", currUser, selectedHotel);

            if(hotel_db.executeQuery(q) > 0){
                System.out.println();
                System.out.println("Room Repair History");
                System.out.println("-----------------------------------");
                hotel_db.executeQueryAndPrintResults(q);
            }
            else{
                System.out.println("There is no recent booking history.");
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

}