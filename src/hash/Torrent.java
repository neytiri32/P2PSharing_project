package hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents object containing hash and his size
 * Hash is arraylist of bytes
 * 
 * 
 *
 */
public class Torrent implements Serializable {

	private ArrayList<byte[]> summary;
	int size; 
	String fileName = "";
	int fileSize;

	public Torrent(File file, String fileName) {
		try {
			setSummary(file);
			this.fileName = fileName;
			this.fileSize = (int) file.length();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getFileSize() {
		return fileSize;
	}

	public ArrayList<byte[]> getSummary() {
		return summary;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Making arraylist of bytes for recived file
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void setSummary(File file) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		InputStream fis = new FileInputStream(file);
		

		ArrayList<byte[]> list = new ArrayList<byte[]>();

		int n = 0;

		byte[] buffer = new byte[512];

		while (n != -1) {
			n = fis.read(buffer);
			if (n > 0) {
				byte[] var = md.digest(buffer); // len = 20
				list.add(var);
			}
		}
		
		this.summary = list;
		
		this.size = summary.size();
	}

	public int getSize() {
		return size;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Torrent)) { 
            return false; 
        } 
		
		Torrent torr = (Torrent) obj;
		
		if (this.getSize() != torr.getSize())
			return false;

		for (int i = 0; i < this.getSize(); i++) {
			if (!Arrays.equals(this.getSummary().get(i), this.getSummary().get(i)))
				return false;
		}

		return true;
	}

}
