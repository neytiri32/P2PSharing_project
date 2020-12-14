package tracker;

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
	Map<String, Torrent> sharedTorrents = new HashMap<String, Torrent>();
	List<String> seeds = new ArrayList<String>();

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

	private File allocateFileMemory(final String filename, final long sizeInBytes) throws IOException {
		File file = new File(filename);
		file.createNewFile();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(sizeInBytes);
		raf.close();

		return file;
	}
	
	public void getFile(Socket socket, String path, int filesize, Torrent summary) throws Exception {

		int bytesRead;
		int currentTot = 0;

		byte[] bytearray = new byte[filesize];
		InputStream is = socket.getInputStream();

		// allocate memory
		File fo = allocateFileMemory(path, filesize);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fo));

		bytesRead = is.read(bytearray, 0, bytearray.length);

		currentTot = bytesRead; // infinitive
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

		Torrent mySummary = new Torrent(file);

		if (!(mySummary.equals(summary))) {
			System.err.println("File is corrupted!");
		} else {
			System.out.println("File is not corrupted.");
		}

	}

	private boolean isSeed(DataOutputStream dOut, DataInputStream dIn, String uID) throws IOException {
		// initial talk so the tracker knows do peer want to download or be a seed and add it to right list
		while (true) {
			dOut.writeUTF(
					"If you want to download files return 'D', else if you want to act as seed return 'S'");
			dOut.flush();

			String answer = dIn.readUTF();
			if (answer.equals("S")) {
				System.out.println("he wants to be seed");
				dOut.writeUTF("OK");
				dOut.flush();
				return true;
			} else if (answer.equals("D")) {
				System.out.println("he wants to download files");
				seeds.add(uID);
				dOut.writeUTF("OK");
				dOut.flush();
				return false;
			}
		}
	}
	
	public void peerShutingDown(Socket socket) {
		//TODO
		//delete from activClients
		//delete maybe from seeds and sharedTorrents (if it is in it)
		//...
	}
	
	public void reciveFile(Socket socket) throws Exception {
		//recive file
		int filesize;
		Torrent summary = null;
		String path = "SharingFiles/recivedFile.txt";
		ArrayList<String> sharingFiles = new ArrayList<String>();
		sharingFiles.add(path);
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		filesize = (int) ois.readObject();
		summary = (Torrent) ois.readObject();
		getFile(socket, path, filesize, summary);
		
	}
	
	public void runTracker(int port) throws Exception {
		ServerSocket server = null;
		Socket socket = null;
		Scanner in = new Scanner(System.in);

		ObjectInputStream ois = null;

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

					DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
					DataInputStream dIn = new DataInputStream(socket.getInputStream());
					
					//initial talk, checking is the peer seeder or downloader
					//if it is sender, save torrent
					if(isSeed(dOut, dIn, uID)) {
						//Receive torrent
						Torrent summary = null;
						ois = new ObjectInputStream(socket.getInputStream());
						summary = (Torrent) ois.readObject();
						sharedTorrents.put(uID, summary);
					}

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
