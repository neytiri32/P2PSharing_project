package peers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import hash.*;

/**
 * @author matea
 *
 */
public class Peer {

	Socket socket = null;
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	Scanner in = new Scanner(System.in);

	// list of other participants
	Map<String, String> neighbors = new HashMap<String, String>();
	// ID of neighbors and indexes of containing blocks
	Map<String, BitSet> blocksOfNeighbors = new HashMap<String, BitSet>();

	BitSet ownedBlocks = null;
	ArrayList<byte[]> blocksOfFile = null;

	// for the blockSelection algorithm
	ArrayList<Integer> rarityOfBlocks = null;

	Torrent myTorrent = null;

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

	// TODO send one bucket to peer
	public void shareFile(Socket socket, String path) throws Exception {
		File file = new File(path);

		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = null;

		byte[] bytearray = new byte[(int) file.length()];
		System.out.println("Sending Size...");
		oos = new ObjectOutputStream(socket.getOutputStream());
		oos.writeObject(bytearray.length);

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
	private File allocateFileMemory(final String filename) throws IOException {
		File file = new File(filename);
		file.createNewFile();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(myTorrent.getFileSize());
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

	// TODO ovo je za download, ali radi samo s jednim blokom
	public void getFile(Socket socket, String path, int blockSize) throws Exception {

		int bytesRead;
		int currentTot = 0;

		byte[] bytearray = new byte[blockSize];
		InputStream is = socket.getInputStream();

		// allocate memory
		File fo = allocateFileMemory(path);
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

		if (!(mySummary.equals(myTorrent))) {
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
		getFile(socket, path, filesize);

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

		// saving torrent into myTorrent variable
		myTorrent = new Torrent(file, path.substring(path.lastIndexOf("/") + 1).trim());
		oos.writeObject(myTorrent);
		oos.flush();

	}

	/**
	 * @param peerID
	 * @return index of chosen block; if index is null, there is no block to choose
	 */
	public Integer blockSelection(String peerID) {

		BitSet peerBitSet = blocksOfNeighbors.get(peerID);

		int numOfShared = Integer.MAX_VALUE;
		Integer index = null;
		// iterate over the true bits in a BitSet
		for (int i = peerBitSet.nextSetBit(0); i >= 0 && i < peerBitSet.length(); i = peerBitSet.nextSetBit(i + 1)) {
			if (rarityOfBlocks.get(i) != 0 && rarityOfBlocks.get(i) < numOfShared) {
				numOfShared = rarityOfBlocks.get(i);
				index = i;
			}
		}
		return index;
	}

	/**
	 * @return maximum four IDs of chosen peers in TreeSet(without duplicates)
	 */
	public TreeSet<String> peerSelection() {
		TreeSet<String> chosenPeers = new TreeSet<String>();
		Object[] keys = neighbors.keySet().toArray();

		int cnt = 0;

		// choosing 4 peers
		if (keys.length <= 4) {
			for (int i = 0; i < keys.length; i++)
				if (!blocksOfNeighbors.get(keys[i]).isEmpty()) // check does peer have something to share
					chosenPeers.add((String) keys[i]);
		} else {
			do {
				String randomID = (String) keys[new Random().nextInt(keys.length)];
				if (!blocksOfNeighbors.get(randomID).isEmpty()) // check does peer have something to share
					chosenPeers.add(randomID);
				cnt++;
			} while (chosenPeers.size() < 4 || cnt > 10); // max 10 iteration to avoid infinitive loop
		}
		return chosenPeers;
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

				// choose file
				File choosenFile = null;
				String choosenPath = "";
				do {
					System.out.println("Please enter full path to the file.");
					choosenPath = in.nextLine();
					choosenFile = new File(choosenPath);
				} while (!choosenFile.isFile());

				sendTorrent(socket, choosenPath);
				System.out.println("Torrent sent");

				System.out.println("Tracker says: " + (String) ois.readObject());

				// setting size of arrays
				int torrentSize = myTorrent.getSize();
				ownedBlocks = new BitSet(torrentSize);
				blocksOfFile = new ArrayList<byte[]>(torrentSize);
				// at the beginning, all zeros in the list
				rarityOfBlocks = new ArrayList<Integer>(Collections.nCopies(torrentSize, 0));

				// TODO thread listening to other peers connection

			} else if (obj instanceof Torrent) {
				// this peer is downloader
				myTorrent = (Torrent) obj;
				System.out.println("Torrent recived");

				oos.writeObject("OK. \nPlease send me list of seeds");

				neighbors = (HashMap<String, String>) ois.readObject(); // stops here and waits
				System.out.println("List of seeds recived");

				// setting size of arrays
				int torrentSize = myTorrent.getSize();
				ownedBlocks = new BitSet(torrentSize);
				blocksOfFile = new ArrayList<byte[]>(torrentSize);

				// at the beginning, all zeros in the list
				rarityOfBlocks = new ArrayList<Integer>(Collections.nCopies(torrentSize, 0));

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
