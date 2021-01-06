package tracker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PeerHandler extends Thread {
	Socket socket = null;
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	String uID = "";

	public PeerHandler(Socket socket, ObjectOutputStream oos, ObjectInputStream ois, String uID) {
		this.socket = socket;
		this.oos = oos;
		this.ois = ois;
		this.uID = uID;
	}

	/**
	 * invoked when peer shuts down
	 * 
	 * @throws IOException
	 */
	public void peerShuttingDown() {

		if (socket != null) { // try to close everything that is open
			try {
				oos.close();
			} catch (IOException e) {
			}
			try {
				ois.close();
			} catch (IOException e) {
			}
			try {
				socket.close();
			} catch (IOException e) {
			}
			System.out.println("Peer ID(" + uID + ") exited.");
			// delete peer form lists if its in them
			Tracker.activeClients.remove(uID);
			Tracker.seeds.remove(uID);
		}

		// if there is no more seeds, remove torrent and initial seeder ID
		if (Tracker.seeds.isEmpty()) {
			Tracker.torrent = null;
			Tracker.initialSeederID = null;
		}

	}

	@Override
	public void run() {

		while (true) {

			try {
				String exitMsg = (String) ois.readObject();
				if (exitMsg.equals("EXIT")) {
					peerShuttingDown();
					return;
				}
			} catch (ClassNotFoundException | IOException e) {
				peerShuttingDown();
				return;
			}
		}

	}

}
