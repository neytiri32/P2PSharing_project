
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

	
	public void runClient(int port) throws UnknownHostException, IOException, InterruptedException, ClassNotFoundException  {

        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;	


		while(true){
//          establish socket connection to server
            socket = new Socket(host.getHostName(), port);
            
//          write to server
            oos = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("My Message: ");
			BufferedReader press = new BufferedReader(new InputStreamReader(System.in));
			String s=press.readLine();
			oos.writeObject(s);
			
            if(s.equalsIgnoreCase("exit")) break;

			
//          read server response 
            ois = new ObjectInputStream(socket.getInputStream());
            String message = (String) ois.readObject();
            System.out.println("Server Message: " + message);
            
            ois.close();
            oos.close();
            
            if(message.equalsIgnoreCase("exit")) break;
        }
		
        System.out.println("Shutting down client");
        socket.close();
        
		
	}
	

}
