package client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


import hash.*;

public class Client {

	private File allocateFileMemory(final String filename, final long sizeInBytes) throws IOException {
		File file = new File(filename);
		file.createNewFile();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(sizeInBytes);
		raf.close();

		return file;
	}

	public void getFile(Socket socket, String path, int filesize, ArrayList<byte[]> summary) throws Exception {

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

//		make hash from a file -> compare
		File file = new File(path);

		ArrayList<byte[]> mySummary = HashGenerator.createSHA1(file);

		if (!HashGenerator.compareHash(mySummary, summary)) {
			System.err.println("File is corrupted!");
		}else {
			System.out.println("File is not corrupted.");
		}


	}

	public void runClient(int port) throws Exception {

		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		ObjectInputStream ois = null;

		// establish socket connection to server
		socket = new Socket(host.getHostName(), port);

		int filesize;
		ArrayList<byte[]> summary = null;

		String path = "SharingFiles/recivedFile.txt";

		ArrayList<String> sharingFiles = new ArrayList<String>();
		sharingFiles.add(path);

		ois = new ObjectInputStream(socket.getInputStream());
		filesize = (int) ois.readObject();
		summary = (ArrayList<byte[]>) ois.readObject();

		getFile(socket, path, filesize, summary);

		System.out.println("Shutting down client");
		socket.close();

	}

}
