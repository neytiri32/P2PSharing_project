package tracker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import hash.*;

public class Tracker {

	// activeClients is hashMap that contains Clients in shape:
	// <uniqueID, address:port>
	static Map<String, String> activeClients = new HashMap<String, String>();
	static List<String> seeds = new ArrayList<String>(); // remove

	// summary and ID of initial seeder
	static Torrent torrent = null;
	static String initialSeederID = null;

	ServerSocket server = null;

	// my access informations
	String myIP = "";
	int myPort;

	private SecureRandom random = new SecureRandom();

	/**
	 * @return unique ID for every peer
	 */
	public String getUniqueId() {
		return new BigInteger(130, random).toString(32).substring(0, 6);
	}

	public String getMyIP() {
		return myIP;
	}

	public int getMyPort() {
		return myPort;
	}

	/**
	 * @param port
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public void runTracker(int port) {

		Scanner in = new Scanner(System.in);

		// creating socket and waiting for client connection
		try {
			server = new ServerSocket(port);
			System.out.println("Started tracker: " + server);

			// setting my access informations
			myIP = server.getInetAddress().getHostAddress();
			myPort = server.getLocalPort();

			while (true) {
				System.out.println("Waiting for the peer request");

				Socket socket = server.accept();

				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

				// unique id(port cannot be ID because if we use different IP address, the port
				// can be the same)
				// generate again if it's in use
				String uID = "";
				do {
					uID = getUniqueId();
				} while (activeClients.containsKey(uID));

				activeClients.put(uID, socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

				System.out.println("Accepted connection : " + socket.getInetAddress().getHostAddress() + ':'
						+ socket.getPort() + ", ID: " + uID);

				// sending given random ID
				oos.writeObject(uID);

				// this is blocking part so one peer has to wait for another to register
				if (initialSeederID == null) {
					// it is initial seeder
					oos.writeObject("You are initial seeder. Send me a Torrent file.");

					// get torrent
					torrent = (Torrent) ois.readObject();
					initialSeederID = uID;
					seeds.add(uID);

					// OK message
					oos.writeObject("OK");
				} else {
					// send torrent
					oos.writeObject(torrent);
					// send a list of peers that are seeds if you get OK return message
					if (((String) ois.readObject()).startsWith("OK")) {
						System.out.println("Sending list of seeds...");
						Map<String, String> newMap = new HashMap<String, String>();

						activeClients.forEach((k, v) -> {
							if (seeds.contains(k))
								newMap.put(k, v);
						});

						oos.writeObject(newMap);
					}
					// add this peer to seeds
					seeds.add(uID);
				}

				// listening is peer shutting down and handling shutting down
				Thread t = new PeerHandler(socket, oos, ois, uID);
				t.start();

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (server != null)
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			in.close();
		}
	}
}
