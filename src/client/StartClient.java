package client;


public class StartClient {

	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.runClient(8080);
	}

}