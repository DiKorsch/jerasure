package de.uni_postdam.hpi.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtils {
	
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
}
