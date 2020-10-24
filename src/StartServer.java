import java.io.IOException;


public class StartServer {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Server server = new Server();
		server.runServer(8080);
	}

}