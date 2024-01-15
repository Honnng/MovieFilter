import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.ibatis.jdbc.ScriptRunner;
import oracle.jdbc.driver.*;

public class Student{
    static Connection con;
    static Statement stmt;

    public static void main(String argv[])
    {
        String username;
        String password;
        Scanner scan = new Scanner(System.in);
        System.out.println("Welcome");
	    do{
            // Let the user input their user name and password.
            System.out.print("Please enter your username: ");
            username = scan.nextLine();
            System.out.print("Please enter your password: ");
            password = scan.nextLine();
        }
        // Connect to the data base
        while(connectToDatabase(username, password) == 0);
        // execute paper.sql
        executePaper();
        // menu loop
        while(true){
            menu();
            String choice = scan.next();
            // View table contents
            if(choice.equals("1")){
                int p_flag = 2;
                int a_flag = 2;
                while(p_flag >= 2){
                    // Determine which table user wants to see
                    System.out.print("Would you like to see PUBLICATIONS? (y/n): ");
                    String p = scan.next();
                    if(p.equals("y")){
                        p_flag = 1;
                        break;
                    }
                    else if(p.equals("n")){
                        p_flag = 0;
                        break;
                    }
                    if(p_flag >= 2){
                        System.out.println("Invaild input, please try again.");
                    }
                }
                while(a_flag >= 2){
                    System.out.print("Would you like to see AUTHORS? (y/n): ");
                    String a = scan.next();
                    if(a.equals("y")){
                        a_flag = 1;
                    }
                    else if(a.equals("n")){
                        a_flag = 0;
                    }
                    if(a_flag >= 2){
                        System.out.println("Invaild input, please try again.");
                    }
                }
                viewTable(p_flag, a_flag);
            }
            // Search by PUBLICATIONID
            else if(choice.equals("2")){
                int flag = 0;
                // Enter publication id first
                while(flag < 1 || flag > 91){
                    System.out.print("Please enter the publication id (1 - 91): ");
                    flag = scan.nextInt();
                    if(flag > 0 && flag < 92){
                        // search it in database
                        searchByID(flag);
                    }
                    else{
                        System.out.println("Invaild input, please try again.");
                    }
                }
            }
            // Update URL by PUBLICATIONID
            else if(choice.equals("3")){
                int flag = 0;
                String path;
                while(flag < 1 || flag > 91){
                    // Enter id
                    System.out.print("Please enter the publication id (1 - 91): ");
                    flag = scan.nextInt();
                    if(flag > 0 && flag < 92){
                        // Enter path
                        System.out.print("Please enter the path of csv file: ");
                        path = scan.next();
                        if(updateURL(flag, path) == 0){
                            System.out.println("csv file not found");
                            flag = 0;
                        }
                        else{
                            break;
                        }
                    }
                    else{
                        System.out.println("Invaild input, please try again.");
                    }
                }
            }
            // Exit
            else if(choice.equals("4")){
                System.out.println("Program exited, Thank you for using");
                break;
            }
            else{
                System.out.println("Invaild input, Please try again.");
            }
            System.out.println("----------------------------------");
            System.out.println("\n\n");
        }
        scan.close();
        return;
    }

    public static void viewTable(int p_flag, int a_flag){
        if(p_flag == 0 && a_flag == 0){
            return;
        }
        // Display PUBLICATIONS
        if(p_flag == 1){
            try{
                String sqlQuery = "SELECT * FROM PUBLICATIONS";
                PreparedStatement pstmt = con.prepareStatement (sqlQuery);
                ResultSet rset = pstmt.executeQuery();
                System.out.printf("\nPUBLICATIONS\nID\tYEAR\tTYPE\t%-75s\tURL\n", "TITLE");
                while(rset.next()){
                    System.out.printf("%s\t%s\t%s\t%-75s\t%s\n", rset.getString(1), rset.getString(2),
                    rset.getString(3),rset.getString(4),rset.getString(5));
                }
                pstmt.close();
            }catch(Exception e){
                System.out.println("Error!");
            };
        }
        // Display AUTHORS
        if(a_flag == 1){
            try{
                String sql2 = "SELECT * FROM AUTHORS";
                PreparedStatement pstmt = con.prepareStatement(sql2);
                ResultSet rset = pstmt.executeQuery();
                System.out.println("\nAUTHORS\nID\tAUTHOR");
                while(rset.next()){
                    System.out.println(rset.getString(1) + "\t" + rset.getString(2));
                }
                pstmt.close();
            }catch(Exception e){
                System.out.println("Error!");
            };
        }
    }

    public static void searchByID(int id){
        try{
            // natural join the publications table and another table which only has id and count, then locate the id
            String sqlQuery = "Select * From publications p NATURAL JOIN (Select PUBLICATIONID, COUNT(*) as c FROM AUTHORS GROUP BY PUBLICATIONID) a WHERE PUBLICATIONID = ";
            sqlQuery += String.valueOf(id);
            PreparedStatement pstmt = con.prepareStatement (sqlQuery);
            ResultSet rset = pstmt.executeQuery();
            System.out.printf("\nPUBLICATIONS\nID\tYEAR\tTYPE\t%-75s\tURL\tCOUNT\n", "TITLE");
            while(rset.next()){
                System.out.printf("%s\t%s\t%s\t%-75s\t%s\t%s\n", rset.getString(1), rset.getString(2),
                rset.getString(3),rset.getString(4),rset.getString(5), rset.getString(6));
            }
            pstmt.close();
        }catch(Exception e){
            System.out.println("Error!");
        };
    }

    public static int updateURL(int id, String path){
        try{
            // Open file
            File f = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";  
            String splitBy = ",";
            String newURL = "";
            String[] url;
            while ((line = br.readLine()) != null){  
                url = line.split(splitBy);
                if(url[0].equals(Integer.toString(id))){
                    newURL = url[1];
                    break;
                }
                url = null;
            }
            // Update URL
            String sql = "Update Publications set url = ? where publicationid = ?";
            PreparedStatement pstmt = con.prepareStatement (sql);
            pstmt.setString(1, newURL);
            pstmt.setString(2,Integer.toString(id));
            pstmt.executeUpdate();
            String sqlQuery = "select * from publications where publicationid = ";
            sqlQuery += Integer.toString(id);
            pstmt = con.prepareStatement (sqlQuery);
            ResultSet rset = pstmt.executeQuery();
            System.out.printf("\nPUBLICATIONS\nID\tYEAR\tTYPE\t%-75s\tURL\n", "TITLE");
            while(rset.next()){
                System.out.printf("%s\t%s\t%s\t%-75s\t%s\n", rset.getString(1), rset.getString(2),
                rset.getString(3),rset.getString(4),rset.getString(5));
            }
            br.close();
            pstmt.close();
            return 1;
        }catch(Exception e){
            return 0;
        }
    }

    public static void menu(){
        // Menu page
        System.out.println("---------- Main Menu ----------");
        System.out.println("1. View table contents\n2. Search by PUBLICATIONID\n3. Update URL by PUBLICATIONID\n4. Exit");
        System.out.print("Please enter your choice (1-4): ");
    }

    public static void executePaper(){
        // Use scriptrunner to run .sql
        File f = new File("paper.sql");
        try{
            Reader r = new FileReader(f);
            ScriptRunner s = new ScriptRunner(con);
            s.runScript(r);
        }
        catch(Exception e){
            System.out.println("There is an error to read paper.sql.");
        }

    }

    public static int connectToDatabase(String username, String password)
    {
	String driverPrefixURL="jdbc:oracle:thin:@";
	String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";
	
        try{
	    //Register Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return 0;
        }

       try{
            System.out.println(driverPrefixURL+jdbc_url);
            con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData dbmd=con.getMetaData();
            stmt=con.createStatement();

            System.out.println("Connected.");

            if(dbmd==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
                System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+dbmd.getDriverName());
                System.out.println("Database Driver Version: "+dbmd.getDriverVersion());
            }
            return 1;
        }catch(Exception e) {e.printStackTrace();}
        return 0;

    }// End of connectToDatabase()
}// End of class