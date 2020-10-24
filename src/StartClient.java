
import java.io.IOException;
import java.net.UnknownHostException;

public class StartClient {

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
		Client client = new Client();
		client.runClient(8080);
	}

}
