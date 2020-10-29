package server;

import java.io.IOException;

public class StartServer {

	public static void main(String[] args) throws Exception, IOException, ClassNotFoundException {
		Server server = new Server();
		server.runServer(8080);
	}

}