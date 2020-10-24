
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;



public class Server {

    public void runServer(int port) throws IOException, ClassNotFoundException {
		ServerSocket server = new ServerSocket(port);

		System.out.println("Started server with ServerSocket: " + server);
		
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;	

		while(true){
            System.out.println("Waiting for the client request");
//          creating socket and waiting for client connection
            Socket socket = server.accept();
            
//          read clients message
            ois = new ObjectInputStream(socket.getInputStream());
//          ObjectInputStream -> String
            String message = (String) ois.readObject();
            System.out.println("Client Message: " + message);
            
            if(message.equalsIgnoreCase("exit")) {
            	System.out.println("Client shutted down.");
            	break;
            }
            
            System.out.println("My message: ");
            BufferedReader press = new BufferedReader(new InputStreamReader(System.in));
			String s=press.readLine();
                       
//          send message to client
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(s);
            
            if(s.equalsIgnoreCase("exit")) break;

            
            ois.close();
            oos.close();
            socket.close();

        }
		
        System.out.println("Shutting down server.");
        server.close();
    }
	
}
