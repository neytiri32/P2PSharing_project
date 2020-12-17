package tracker;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

public class Tracker implements Runnable {

	// activeClients is hashMap that contains Clients in shape:
	// <uniqueID, address:port>
	Map<String, String> activeClients = new HashMap<String, String>();
	Map<String, Torrent> sharedTorrents = new HashMap<String, Torrent>();
	List<String> seeds = new ArrayList<String>();

	ServerSocket server = null;
	Socket socket = null;
	String uID; // id of currently connected peer

	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	
	boolean running = false;

	private static SecureRandom random = new SecureRandom();

	/**
	 * @return
	 */
	public static String getUniqueId() {
		return new BigInteger(130, random).toString(32).substring(0, 6);
	}

	/**
	 * 
	 * @return true if the peer wants to be seed
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	private boolean isSeed() throws IOException, ClassNotFoundException {

		while (true) {
			
			oos.writeObject("If you want to download files return 'D', \nelse if you want to act as seed return 'S'");
			
			String answer = (String) ois.readObject();

			if (answer.equals("S")) {
				System.out.println("ID(" + uID + "): peer wants to be seed");
				oos.writeObject("OK");
				oos.writeObject("You are now a seeder.");
				return true;
			} else if (answer.equals("D")) {
				System.out.println("ID(" + uID + "): peer wants to download files");
				seeds.add(uID);
				oos.writeObject("OK");
				String msg = "You are now a downloader. I am waiting for your next request.\n\n"
						+ "Instructions for making request:\n " + "Send \"SUMM\" if you want to get summary,\n"
						+ " send \"DATA\"if you want to get needed data for connecting with peers and getting the file\n";
				oos.writeObject(msg);
				return false;
			}
		}
	}

	/**
	 * invoked when peer shuts down
	 * 
	 * @throws IOException
	 */
	public void peerShuttingDown() throws IOException {

		if (socket != null) {
			oos.close();
			ois.close();
			socket.close();
		}
		System.out.println("Peer ID(" + uID + ") exited.");

		// delete peer form lists if its in them
		activeClients.remove(uID);
		seeds.remove(uID);

		// if there is no more seeds, remove torrent from the list
		if (seeds.isEmpty())
			sharedTorrents.remove(uID);

	}

	/**
	 * @param port
	 * @throws Exception
	 */
	public void runTracker(int port) throws Exception {

		Scanner in = new Scanner(System.in);

		// creating socket and waiting for client connection
		try {
			server = new ServerSocket(port);
			System.out.println("Started tracker: " + server);
			while (true) {
				System.out.println("Waiting for the peer request");
				try {
					socket = server.accept();
					System.out.println("Accepted connection : " + socket.getInetAddress().getHostAddress() + ':'
							+ socket.getPort());

					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					
					// unique id(port cannot be ID because if we use different IP address, the port
					// can be the same)
					uID = getUniqueId();
					activeClients.put(uID, socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

					// initial talk, checking is the peer seeder or downloader
					// if it is sender, save torrent, else wait for request
					// this is a blocking part(another peer has to wait until this peer finishes his talk
					// TODO: change this part into thread
					if (isSeed()) {
						// Receive torrent
						Torrent summary = null;
						summary = (Torrent) ois.readObject();
						sharedTorrents.put(uID, summary);
						//stay alive; TODO repair functionality
						new Thread(() -> {
							running = true;
					         while(true) {
					            try {
					               Thread.sleep(5000);
					            } catch(Exception e) {
					               e.printStackTrace();
					            }
					         }
					      }).start();
					} else {
						// Wait for request (summary or IP or exit)
						running = true;
						Thread th = new Thread(this);
						th.start();
						// while loop continues while thread is working
					}

				} catch (Exception e) {
				} finally {
					// if thread is working, do not close connection
					if (!running)
						peerShuttingDown();
				}
			}
		} finally {
			if (server != null)
				server.close();
			in.close();
		}
	}

	/**
	 * Wait for request (summary or IP:port or exit)
	 */
	@Override
	public void run() {
		while (running) {

			String request = null;
			// listen to the request "SUMM" or "DATA" or "EXIT"
			try {
				request = (String) ois.readObject();
				switch (request) {
				case "SUMM":

					if (sharedTorrents.isEmpty()) {
						oos.writeObject("There is no available summary");
					} else {
						System.out.println("Sending summary to " + uID);

						// this is always sending summary that is from the first Seeder in the array
						// TODO: communicate with a peer so it can tell you which one does he wants
						oos.writeObject(sharedTorrents.get(seeds.get(0)));						
					}
					break;
				case "DATA":
					System.out.println("DATA");

					if (seeds.isEmpty()) {
						oos.writeObject("There is no available data of the seeder");						

					} else {
						System.out.println("Sending data to " + uID);

						// this is always sending summary that is from the first Seeder in the array
						// TODO: check which summary peer asked and send him a data of all clients that
						// are seeding it
						oos.writeObject(activeClients.get(seeds.get(0)));						
					}
					break;
				case "EXIT":
					running = false;
					peerShuttingDown();
					// exit this thread because socket does not exist anymore
					break;
				default:
					break;
				}
			} catch (IOException e1) {
				running = false;
				try {
					// close if exc happens
					peerShuttingDown();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
