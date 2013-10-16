package de.uni_postdam.hpi.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import de.uni_postdam.hpi.jerasure.Buffer;


public class FileUtils {

	public static final long BYTE = 1;
	public static final long KB = 1024 * BYTE;
	public static final long MB = 1024 * KB;
	public static final long GB = 1024 * MB;
	
	
	public static String generatePartPath(String filePath, String partSuffix, int num){
		File f = new File(filePath);
		String path = f.getParentFile().getAbsolutePath();
		String name = generatePartName(f.getName(), partSuffix, num);
		
		return path + File.separator + name;
	}
	
	public static String generatePartName(String fileName, String partSuffix, int num){
		String name = getName(fileName);
		String ext = getExtension(fileName);
		String format = ext.length() == 0 ? "%s_%s%02d%s" : "%s_%s%02d.%s";
		return String.format(format, name, partSuffix, num, ext);
	}
	
	public static int extractNum(String fileName, String partSuffix){
		String name = getName(fileName);
		int len = name.length();
		return Integer.parseInt(name.substring(len - 2, len));
	}
	
 	public static File[] collectFiles(String filePath, String partSuffix, int numFiles){
		File[] result = new File[numFiles];
		File f = new File(filePath);
		String path = f.getParentFile().getAbsolutePath();
		for(int i = 1; i <= numFiles; i++){
			String newFilePath = path + File.separator + generatePartName(f.getName(), partSuffix, i);
			result[i-1] = new File(newFilePath);
		}
		return result;
	}
	
	public static void deleteFiles(File[] files) {
		if(files == null){
			System.err.println("Files was null!");
			return;
		}
		for(File f: files){
			if(f != null && !f.delete()){
				System.err.println("Could not delete: " + f.getAbsolutePath());
			}else if(f == null){
				System.err.println("File was null!");
			}
		}
		
	}
	
	public static String[] getNameAndExtension(String fileName){
		String name = fileName;
		String extension = "";

		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			name = fileName.substring(0, i);
		    extension = fileName.substring(i+1);
		}
		return new String[] {name, extension};

	}
	
	public static String getName(String fileName){
		return getNameAndExtension(fileName)[0];
	}
	
	public static String getExtension(String fileName){
		return getNameAndExtension(fileName)[1];
	}
	
	
	public static void writeRestored(byte[] restored, FileOutputStream[] missing, int k, int w, int packetSize) throws IOException{
		
		for (int i = 0; i < missing.length; i++) {
			FileUtils.write(i, missing[i], restored, w, packetSize);
		}
	}
	
	public static void writeParts(byte[] data, byte[] coding,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w,
			int packetSize) throws IOException {
		if (k_parts == null || m_parts == null) {
			throw new IllegalArgumentException(
					"one of the parts(or both) arrays was null: k=" + k_parts
							+ " m=" + m_parts);
		}
	
		int k = k_parts.length;
		int m = m_parts.length;
	
		for (int i = 0; i < k; i++) {
			FileUtils.write(i, k_parts[i], data, w, packetSize);
		}
	
		for (int i = 0; i < m; i++) {
			FileUtils.write(i, m_parts[i], coding, w, packetSize);
		}
	}


	public static void writeParts(Buffer data, Buffer coding,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w,
			int packetSize) throws IOException {
		if (k_parts == null || m_parts == null) {
			throw new IllegalArgumentException(
					"one of the parts(or both) arrays was null: k=" + k_parts
							+ " m=" + m_parts);
		}
	
		int k = k_parts.length;
		int m = m_parts.length;
	
		int blockSize = w * packetSize * k;
		int len = w * packetSize;
		
		for(int j = 0; j < data.size() / blockSize; j++){
			for (int i = 0; i < k; i++) {
				int start = (j * k + i) *  len;
				data.writeToStream(k_parts[i], start, len);
			}
		}
		
		blockSize = w * packetSize * m;
		for(int j = 0; j < coding.size() / blockSize ; j++){
					
			for (int i = 0; i < m; i++) {

				int start = (j * m + i) *  len;
				coding.writeToStream(m_parts[i], start, len);
			}
		}
	}

	public static void writeParts(Buffer data, byte[] coding,
			FileOutputStream[] k_parts, FileOutputStream[] m_parts, int w,
			int packetSize) throws IOException {
		if (k_parts == null || m_parts == null) {
			throw new IllegalArgumentException(
					"one of the parts(or both) arrays was null: k=" + k_parts
							+ " m=" + m_parts);
		}
	
		int k = k_parts.length;
		int m = m_parts.length;
	
		for (int i = 0; i < k; i++) {
			FileUtils.write(i, k_parts[i], data, w, packetSize);
		}
	
		for (int i = 0; i < m; i++) {
			FileUtils.write(i, m_parts[i], coding, w, packetSize);
		}		
	}
	
	private static void write(int idx, FileOutputStream destenation,
			byte[] data, int w, int packetSize) throws IOException {
		int start = idx * w * packetSize;
		destenation.write(data, start, w * packetSize);
	}

	private static void write(int idx, FileOutputStream destenation,
			Buffer data, int w, int packetSize) throws IOException {
		int start = idx * w * packetSize;
		data.writeToStream(destenation, start, w * packetSize);
	}

	
	public static FileOutputStream[] createParts(String filePath,
			String suffix, int numParts) {
		FileOutputStream[] result = new FileOutputStream[numParts];
		for (int i = 0; i < numParts; i++) {
			String partName = generatePartPath(filePath, suffix, i+1);
			File part = new File(partName);
			try {
				if (part.exists()) {
					part.delete();
				}
				if (part.createNewFile()) {
					result[i] = new FileOutputStream(part);
				} else {
					throw new RuntimeException("part " + part.getAbsolutePath()
							+ " could not be created!");
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("an error occured!");
			}
		}
		return result;
	}

	public static void close(FileOutputStream[] parts) {
		if (parts == null)
			return;
		for (FileOutputStream fos : parts) {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("output stream was null!");
			}
		}
	}

	public static boolean createRandomContentFile(File f, long size){
		return createRandomContentFile(f.getAbsolutePath(), size);
	}
	
	public static boolean createRandomContentFile(String filePath, long fileSize){
		RandomAccessFile f = null;
		File file = new File(filePath);
		
		int cacheSize = 4 * 1024 * 1024; // 4mb
		long bytesWritten = 0L;
		
		try {
			f = new RandomAccessFile(file, "rw");
			f.setLength(fileSize);
			while (bytesWritten < fileSize) {
				int bytesToWrite = (int) Math.min(cacheSize, fileSize - bytesWritten);
				byte[] cache = new byte[bytesToWrite];
				new Random().nextBytes(cache);
				f.write(cache);
				bytesWritten += bytesToWrite;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				f.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;

	}

	
	public static void cleanDir(File dir, boolean recursive){
		if(!dir.isDirectory()) return;
		
		for(File f: dir.listFiles()){
			if(f.isFile())
				f.delete();
			if(f.isDirectory() && recursive)
				cleanDir(f, recursive);
		}
		
	}
	
	public static void cleanDir(File dir){
		cleanDir(dir, false);
	}


	public static String getMD5Hash(File file) throws IOException, NoSuchAlgorithmException {

		HashCode hashCode = Files.hash(file, Hashing.md5());
		BigInteger bigInt = new BigInteger(1, hashCode.asBytes());
		String hashString = bigInt.toString(16);
		// an die Länge von 32bit anpassen
		while (hashString.length() < 32) {
			hashString = "0" + hashString;
		}

		return hashString;
	}

	public static String getHash(File file, HashFunction hashFunc) throws IOException {

		byte[] hash;
		try {
			hash = Files.hash(file, hashFunc).asBytes();
		} catch (IOException e) {

			return "";
		}
		int hashLength = hashFunc.bits() / 4;
		BigInteger bigInt = new BigInteger(1, hash);
		String hashString = bigInt.toString(16);
		while (hashString.length() < hashLength) {
			// führende Nullen vorne hinzufügen
			hashString = "0" + hashString;
		}
		return hashString;
	}

	
	public static File createFile(String fileName, byte[] content) throws IOException{
		return createFile(fileName, "", content);
	}
	
	public static File createFile(String fileName, String extension, byte[] content) throws IOException{
		File result = File.createTempFile(fileName, extension);
		result.deleteOnExit();
		FileOutputStream fos = null;
		fos = new FileOutputStream(result);
		fos.write(content);
		fos.close();
		return result;
		
	}
	

	
	public static boolean checkFileContent(File f, byte[] should) throws IOException {
		
		FileInputStream fis = new FileInputStream(f);
		byte[] content = new byte[should.length];
		fis.read(content);
		fis.close();
		for(int i = 0; i < should.length; i++){
			if(content[i] != should[i])
				return false;
		}
		return true;
	}


}
