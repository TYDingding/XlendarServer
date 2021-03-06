import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;

public class XlendarServerMain {
    ServerSocket server;
    int serverPort = 8888;

    Connection connection = null;
    Statement statement = null;
    ResultSet rs = null;

    XlendarServerMain(){
        try {
            server = new ServerSocket(serverPort);
            System.out.println("ServerSocket: " + server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class.forName("org.sqlite.JDBC");
        }catch (Exception e){
            e.getMessage();
        }
    }

    private void listen() {

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:D:\\Xlendar\\lib\\xlendar.db2");
            statement = connection.createStatement();

            rs = connection.getMetaData().getTables(null,null,"event", null);


            if (rs.next()) {

            }else {
            statement.executeUpdate("DROP TABLE IF EXISTS event");
            statement.executeUpdate("CREATE TABLE event(week string,eventId string , date string , eventTime string ," +
                    " eventName string)");
            }


        }catch (SQLException e){
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

            while (true) { // run until you terminate the program
            try {
                // Wait for connection. Block until a connection is made.
                Socket socket = server.accept();
                System.out.println("Socket: " + socket);
                // Start a new thread for each client to perform block-IO operations.
                new ClientThread(socket).start();
            } catch (BindException e) {
                e.printStackTrace();
                break; // Port already in use
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new XlendarServerMain().listen();
    }

    // Fork out a thread for each connected client to perform block-IO
    class ClientThread extends Thread {

        Socket socket;
        PrintWriter out;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            InputStream in = null;
            try {
                in = socket.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(in));
                String line;
                String[] sourceStrArray;
                while ((line = rd.readLine()) != null) {
                    System.out.println(line);
                    sourceStrArray = line.split(",");
                    //Data into database
                    if(sourceStrArray[1].equals("Hello")){
                        out = new PrintWriter(socket.getOutputStream());
                        try {
                            if (statement.isClosed() != false){
                                statement.executeUpdate("SELECT eventId, eventTime, eventName FROM event WHERE week='"+sourceStrArray[0]+"'");
                                ResultSet rs1 = statement.getResultSet();
                                while (rs1.next()){
                                    String message = rs1.getString("eventId");
                                    message = message + "," + rs.getString("eventTime");
                                    message = message + "," + rs.getString("eventName");
                                    out.print(message);
                                    out.flush();
                                }
                            }else{
                                out.print("No");
                                out.flush();
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }else{
                        try{
                            statement.executeUpdate(line);
                        }catch(SQLException e1){
                            System.out.println(e1.getErrorCode());
                        }
                    }
                }


			/*
            int byteRead;
            // Block until the client closes the connection, results in read() returns -1
            while ((byteRead = in.read()) != -1) {
               System.out.print((char)byteRead);
            }
*/
                System.out.println("Close Socket: " + socket);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try{
                    if(in != null){
                        in.close();
                    }
                    out.close();
                    socket.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
