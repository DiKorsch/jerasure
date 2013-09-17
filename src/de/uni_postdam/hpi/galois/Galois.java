package de.uni_postdam.hpi.galois;

import java.util.HashMap;
import java.util.Map;

public class Galois {
	static Map<Integer, GaloisField> fields = new HashMap<>();
	
	public static GaloisField field(int degree){
		int key = GaloisField.calcElements(degree);
		if(!fields.containsKey(key)){
			GaloisField newField = new GaloisField(degree);
			fields.put(key, newField);
			return newField;
		}
		return fields.get(key);
	}
		
	public static int add(int x, int y, int w){
		return Galois.field(w).add(x, y).value();
	}
	
	public static int subtract(int x, int y, int w){
		return Galois.field(w).subtract(x, y).value();
	}
	
	public static int multiply(int x, int y, int w){
		return Galois.field(w).multiply(x, y).value();
	}
	
	public static int divide(int x, int y, int w){
		return Galois.field(w).divide(x, y).value();
	}
}
