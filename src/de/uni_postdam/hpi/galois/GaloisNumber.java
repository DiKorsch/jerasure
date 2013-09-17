package de.uni_postdam.hpi.galois;
import java.lang.Comparable;


public class GaloisNumber implements Comparable<GaloisNumber>{

	private GaloisField field = null;
	private int value = -1;
	
	public GaloisNumber(GaloisField field, int value) {
		this.field = field;
		this.setValue(value);
	}

	private void setValue(int value) {
		if(this.field == null){
			throw new RuntimeException("The galois field is not set!");
		}
		this.value = value % this.field.elements();
	}
	
	public int value(){
		return this.value;
	}

	@Override
	public int compareTo(GaloisNumber other) {
		if(!this.field.equals(other.field)){
			throw new RuntimeException("Fields of the numbers are not equal!");
		}
		return this.value - other.value;
	}
	
	@Override
	public boolean equals(Object other) {
		GaloisNumber otherNumber = null;
		if (other instanceof GaloisNumber) {
			otherNumber = (GaloisNumber) other;
		}else{
			return false;
		}
		if(!this.field.equals(otherNumber.field) || otherNumber == null){
			return false;
		}
		return this.value == otherNumber.value;
	}
	
}
