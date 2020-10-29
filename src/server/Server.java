package server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import hash.*;

public class Server {

	public void shareFile(Socket socket, String path) throws Exception {
		File file = new File(path);

		OutputStream os = socket.getOutputStream();
		ObjectOutputStream oos = null;

		ArrayList<byte[]> summary = HashGenerator.createSHA1(file);

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

	public void runServer(int port) throws Exception {
		ServerSocket server = new ServerSocket(port);

		System.out.println("Started server with ServerSocket: " + server);

		System.out.println("Waiting for the client request");
		// creating socket and waiting for client connection
		Socket socket = server.accept();

		String path = "SharingFiles/article1.txt";
		
		ArrayList<String> sharingFiles = new ArrayList<String>();
		sharingFiles.add(path);

		shareFile(socket, path);

		socket.close();
		server.close();

	}

}
