package de.uni_postdam.hpi.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;


public class FileUtils {

	public static final long BYTE = 1;
	public static final long KB = 1024 * BYTE;
	public static final long MB = 1024 * KB;
	public static final long GB = 1024 * MB;
	
	
	public static String generatePartPath(String filePath, String partPrefix, int num){
		File f = new File(filePath);
		String path = f.getParentFile().getAbsolutePath();
		String name = generatePartName(f.getName(), partPrefix, num);
		
		return path + File.separator + name;
	}
	
	public static String generatePartName(String fileName, String partPrefix, int num){
		String name = getName(fileName);
		String ext = getExtension(fileName);
		String format = ext.length() == 0 ? "%s_%s%d%s" : "%s_%s%d.%s";
		return String.format(format, name, partPrefix, num, ext);
	}
	
	public static File[] collectFiles(String filePath, String partPrefix, int numFiles){
		File[] result = new File[numFiles];
		File f = new File(filePath);
		String path = f.getParentFile().getAbsolutePath();
		for(int i = 1; i <= numFiles; i++){
			String newFilePath = path + File.separator + generatePartName(f.getName(), partPrefix, i);
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
	
	
	public static void writeParts(byte[] dataAndCoding,
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
			FileUtils.write(i, k_parts[i], dataAndCoding, w, packetSize);
		}
	
		for (int i = 0; i < m; i++) {
			FileUtils.write(i + k, m_parts[i], dataAndCoding, w, packetSize);
		}
	}

	private static void write(int idx, FileOutputStream destenation,
			byte[] dataAndCoding, int w, int packetSize) throws IOException {
		int start = idx * w * packetSize;
		destenation.write(dataAndCoding, start, w * packetSize);
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
}
