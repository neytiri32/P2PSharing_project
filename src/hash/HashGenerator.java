package hash;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HashGenerator {

	public static ArrayList<byte[]> createSHA1(File file) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		InputStream fis = new FileInputStream(file);
		

		ArrayList<byte[]> list = new ArrayList<byte[]>();

		int n = 0;

		byte[] buffer = new byte[254];

		while (n != -1) {
			n = fis.read(buffer);
			if (n > 0) {
				byte[] var = md.digest(buffer); // len = 20
				list.add(var);
			}
		}
		return list;
	}
	
	public static boolean compareHash(ArrayList<byte[]> a1,  ArrayList<byte[]> a2) {
		
		if(a1.size() != a2.size()) return false;
		
		for(int i = 0; i < a1.size(); i++) {
			if(!Arrays.equals(a1.get(i), a2.get(i))) return false;
		}
		
		return true;
		
	}

}
