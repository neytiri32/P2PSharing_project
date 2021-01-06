package peers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import hash.*;

public class Peer {

	Socket socket = null;
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	Scanner in = new Scanner(System.in);

	// list of other participants
	Map<String, String> neighbours = new HashMap<String, String>();

	Torrent recivedTorrent = null;

	// my access informations
	String myIP = "";
	int myPort;
	String myID = "";

	public String getMyIP() {
		return myIP;
	}

	public int getMyPort() {
		return myPort;
	}

	/**
	 * !! Currently not used !! Sending file to other peer
	 * 
	 * @param socket
	 * @param path
	 * @throws Exception
	 */
	public void shareFile(Socket socket, String path) throws Exception {
		File file = new File(path);

		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = null;

		Torrent summary = new Torrent(file, path.substring(path.lastIndexOf("/") + 1).trim());

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

		bin.close();

	}

	/**
	 * !! Currently not used !! For allocating memory when receiving file from other
	 * peer
	 * 
	 * @param filename
	 * @param sizeInBytes
	 * @return
	 * @throws IOException
	 */
	private File allocateFileMemory(final String filename, final long sizeInBytes) throws IOException {
		File file = new File(filename);
		file.createNewFile();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(sizeInBytes);
		raf.close();

		return file;
	}

	/**
	 * !! Currently not used !!
	 * 
	 * @param socket
	 * @param path
	 * @param filesize
	 * @param summary
	 * @throws Exception
	 */
	public void getFile(Socket socket, String path, int filesize, Torrent summary) throws Exception {

		int bytesRead;
		int currentTot = 0;

		byte[] bytearray = new byte[filesize];
		InputStream is = socket.getInputStream();

		// allocate memory
		File fo = allocateFileMemory(path, filesize);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fo));

		bytesRead = is.read(bytearray, 0, bytearray.length);

		currentTot = bytesRead;
		do {
			bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
			if (bytesRead >= 0) {
				currentTot += bytesRead;
			}
		} while (bytesRead > 0);
		bos.write(bytearray, 0, currentTot);

		bos.flush();
		bos.close();

		// make hash from a file -> compare
		File file = new File(path);

		Torrent mySummary = new Torrent(file, path.substring(path.lastIndexOf("/") + 1).trim());

		if (!(mySummary.equals(summary))) {
			System.err.println("File is corrupted!");
		} else {
			System.out.println("File is not corrupted.");
		}

	}

	/**
	 * !! Currently not used !!
	 * 
	 * TODO For receiving file from other shocker
	 * reciveFile()->getFile()->allocateMemmory()
	 * 
	 * @param socket
	 * @throws Exception
	 */
	public void reciveFile(Socket socket) throws Exception {
		// Receive file
		int filesize;
		Torrent summary = null;
		String path = "SharingFiles/recivedFile_" + myID + ".txt";
		ArrayList<String> sharingFiles = new ArrayList<String>();
		sharingFiles.add(path);
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		filesize = (int) ois.readObject();
		summary = (Torrent) ois.readObject();
		getFile(socket, path, filesize, summary);

	}

	// active code

	/**
	 * Sending summary to the tracker
	 * 
	 * @param socket
	 * @param path
	 * @throws Exception
	 */
	public void sendTorrent(Socket socket, String path) throws Exception {
		File file = new File(path);

		Torrent summary = new Torrent(file, path.substring(path.lastIndexOf("/") + 1).trim());
		oos.writeObject(summary);
		oos.flush();

	}

	/**
	 * @param port
	 * @throws Exception
	 */
	public void runPeer(int port) throws Exception {

		InetAddress host = InetAddress.getLocalHost();

		try {
			// establish socket connection to server
			socket = new Socket(host.getHostName(), port);
			System.out.println("Connecting..." + socket);

			// setting my access informations
			myIP = socket.getInetAddress().getHostAddress();
			myPort = socket.getLocalPort();

			oos = new ObjectOutputStream(socket.getOutputStream());
			ois = new ObjectInputStream(socket.getInputStream());

			myID = (String) ois.readObject();
			System.out.println("My ID is: " + myID);

			Object obj = ois.readObject();
			if (obj instanceof String) {
				// this peer is initial peer
				String recivedMsg = (String) obj;
				System.out.println(recivedMsg);

				// TODO choose file
				sendTorrent(socket, "SharingFiles/article1.txt");
				System.out.println("Torrent sent");

				System.out.println("Tracker says: " + (String) ois.readObject());

				// TODO thread listening to other peers connection

			} else if (obj instanceof Torrent) {
				// this peer is downloader
				recivedTorrent = (Torrent) obj;
				System.out.println("Torrent recived");

				oos.writeObject("OK. \nPlease send me list of seeds");

				neighbours = (HashMap<String, String>) ois.readObject(); // stops here and waits
				System.out.println("List of seeds recived");

				// TODO thread connect to wanted peers
				// TODO thread listening to other peers connection

			}

			while (true) {
				Thread.sleep(1234678);
			}
		} finally {
			in.close();
			if (socket != null) {
				// Inform the tracker when they leave the swarm.
				oos.writeObject("EXIT");
				oos.close();
				ois.close();
				socket.close();
			}
		}

	}
}
