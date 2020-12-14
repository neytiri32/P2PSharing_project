package peers;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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

			int filesize;
			Torrent summary = null;

			String path = "SharingFiles/recivedFile.txt";

			ArrayList<String> sharingFiles = new ArrayList<String>();
			sharingFiles.add(path);

			ois = new ObjectInputStream(socket.getInputStream());
			filesize = (int) ois.readObject();
			summary = (Torrent) ois.readObject();

			getFile(socket, path, filesize, summary);

		} finally {
			if (socket != null)
				System.out.println("Shutting down peer" + socket.toString());
			socket.close();
		}

	}

}
