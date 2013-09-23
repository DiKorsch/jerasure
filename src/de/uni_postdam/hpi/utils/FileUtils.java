package de.uni_postdam.hpi.utils;

import java.io.File;

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
}
