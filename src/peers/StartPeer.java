package peers;


public class StartPeer {

	public static void main(String[] args) throws Exception {
		Peer peer = new Peer();
		peer.runPeer(8080);
	}

}