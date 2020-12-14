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
import java.util.Scanner;

import hash.*;

public class Peer {

	char role; // S == seed; D == downloader

	private File allocateFileMemory(final String filename, final long sizeInBytes) throws IOException {
		File file = new File(filename);
		file.createNewFile();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(sizeInBytes);
		raf.close();

		return file;
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
	
	public void sendTorrent(Socket socket, String path) throws Exception {
		File file = new File(path);

		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = null;
		oos = new ObjectOutputStream(socket.getOutputStream());

		Torrent summary = new Torrent(file);
		oos.writeObject(summary); 
		oos.flush();
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

	private void initialTalk(DataOutputStream dOut, DataInputStream dIn) throws IOException {
		// initial talk so the tracker knows do peer want to download or be a seed
		String get;
		String s = "";
		Scanner in = new Scanner(System.in);

		while (true) {
			get = dIn.readUTF();

			System.out.println(get);
			if (get.equals("OK")) {
				role = s.charAt(0);
				break;
			}
			s = in.nextLine();
			dOut.writeUTF(s);
			dOut.flush();
		}
	}

	public void runPeer(int port) throws Exception {

		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		ObjectInputStream ois = null;

		try {

			// establish socket connection to server
			socket = new Socket(host.getHostName(), port);
			System.out.println("Connecting...");

			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			initialTalk(dOut, dIn);

			//sendFile
			String path3 = "/home/matea/Desktop/new.txt"; // isto je ok
			String path1 = "SharingFiles/article1.txt";

			sendTorrent(socket, path1);
			shareFile(socket, path1);


		} finally {
			if (socket != null)
				System.out.println("Shutting down peer" + socket.toString());
			socket.close();
		}

	}

}
