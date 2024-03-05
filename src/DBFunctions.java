import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBFunctions {
    Connection _connection = null;
    public Connection connect_to_db(String dbname, String dbport, String user, String password) {
        try{
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println ("Connection URL: " + url);

            _connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to Database");
        }catch(Exception e){
            System.err.println("Error - Unable to connect to database " + e.getMessage());
            System.exit(-1);
        }
        return _connection;
    }

    public void cleanup(){
        try{
            if(this._connection != null){this._connection.close();}
        }catch( SQLException e){
            System.err.println(e.getMessage());
        }
    }

    public int executeQuery(String q) throws SQLException{
        Statement stmt = this._connection.createStatement();
        ResultSet rs = stmt.executeQuery(q);

        int n_rows = 0;

        while(rs.next()){
            n_rows++;
        }

        stmt.close();

        return n_rows;
    }

    public int executeQueryAndPrintResults(String q) throws SQLException{
        Statement stmt = this._connection.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        ResultSetMetaData rsmd = rs.getMetaData();

        int n_cols = rsmd.getColumnCount();
        int n_rows = 0;

        //print query header row
        for(int i = 1; i <= n_cols; i++){
            System.out.print(rsmd.getColumnName(i) + "\t");
        }
        System.out.println();

        //print query results
        while(rs.next()){
            for(int i = 1; i <= n_cols; i++){
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
            n_rows++;
        }
        stmt.close();

        return n_rows;
    }

    public List<List<String>> executeQueryAndReturnResult(String q) throws SQLException{
        Statement stmt = this._connection.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        ResultSetMetaData rsmd = rs.getMetaData();

        int n_cols = rsmd.getColumnCount();

        List<List<String>> results = new ArrayList<List<String>>();
        while(rs.next()){
            List<String> record = new ArrayList<String>();
            for(int i = 1; i <=n_cols; i++){
                record.add(rs.getString(i));
            }
            results.add(record);
        }
        stmt.close();

        return results;
    }


    public int getLastSequenceID(String q) throws SQLException{
        Statement stmt = this._connection.createStatement();
        ResultSet rs = stmt.executeQuery(q);

        if(rs.next()){
            return rs.getInt(1);
        }

        return -1;
    }

    public void executeUpdate(String q) throws SQLException {
        Statement stmt = this._connection.createStatement();
        stmt.executeUpdate(q);
        stmt.close();
    }

}


