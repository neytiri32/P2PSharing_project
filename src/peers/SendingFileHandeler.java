package peers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SendingFileHandeler extends Thread {
	int myPort;
	ArrayList<byte[]> blocksOfFile = null;	

	public SendingFileHandeler(int myPort, ArrayList<byte[]> blocksOfFile) {
		this.myPort = myPort;
		this.blocksOfFile = blocksOfFile;
	}

	@Override
	public void run() {

		try {
			System.out.println("Thread Running");
			ServerSocket server = new ServerSocket();

			while (true) {
				System.out.println("Waiting for the peer request");
				Socket socket = server.accept();

				ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());

				int wantedBlockIndex = (int) objectInput.readObject();
				objectOutput.writeObject(blocksOfFile.get(wantedBlockIndex));

				socket.close();
				objectInput.close();
				objectOutput.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
