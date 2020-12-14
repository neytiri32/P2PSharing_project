package tracker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
	Map<String, String> activeClients = new HashMap<String, String>();
	ArrayList<String> sharingFiles = new ArrayList<String>();
	List<String> seeds = new ArrayList<String>();
	Map<String, String> sharedSummaries = new HashMap<String, String>();
	

	private static SecureRandom random = new SecureRandom();

	public static String getUniqueId() {
		return new BigInteger(130, random).toString(32).substring(0, 6);
	}

	public void shareFile(Socket socket, String path) throws Exception {
		File file = new File(path);

		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = null;

		Torrent summary = new Torrent(file);

		byte[] bytearray = new byte[(int) file.length()];
		System.out.println("Sending Size...");
		oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(bytearray.length);
		oos.writeObject(summary);

		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
		bin.read(bytearray, 0, bytearray.length);

		System.out.println("Sending Files...");
		os.write(bytearray, 0, bytearray.length);
		os.flush();

	}

	public void runTracker(int port) throws Exception {
		ServerSocket server = null;
		Socket socket = null;

		// creating socket and waiting for client connection
		try {
			server = new ServerSocket(port);
			System.out.println("Started tracker(server) with ServerSocket: " + server);
			while (true) {
				System.out.println("Waiting for the client request");
				try {
					socket = server.accept();
					System.out.println("Accepted connection : " + socket.getInetAddress().getHostAddress() + ':'
							+ socket.getPort());

					// can there be same ports and different address?
					String uID = getUniqueId();
					activeClients.put(uID, socket.getPort() + ":" + socket.getInetAddress().getHostAddress());

					if (true) { // Check did he registered as a seed
						seeds.add(uID);
					}

					// send file
					String path1 = "SharingFiles/article1.txt";
					String path = "/home/matea/Desktop/new.txt"; // isto je ok

//					System.out.println("Please write a full path to the file you want to share.\n");
//					Scanner in = new Scanner(System.in);
//					String path2 = in.nextLine();

					sharingFiles.add(path);

					shareFile(socket, path);

				} finally {
					if (socket != null)
						socket.close();
				}
			}
		} finally {
			if (server != null)
				server.close();
		}
	}
}
