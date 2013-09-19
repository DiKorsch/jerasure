package de.uni_postdam.hpi.matrix;

public class Schedule {
	/** FALSE for copy and TRUE for XOR*/
	boolean operation;
	
	int sourceId;
	int sourceBit;
	
	int destinationId;
	int destinationBit;
	
	public Schedule(boolean operation, int srcId, int srcBit, int destId, int destBit) {
		this.operation = operation;
		this.sourceId = srcId;
		this.sourceBit = srcBit;
		this.destinationId = destId;
		this.destinationBit = destBit;
	}
	
	@Override
	public String toString() {
		
		return String.format("<%d, %d, %d, %d, %d>",
				operation ? 1 : 0, 
				sourceId, sourceBit,
				destinationId, destinationBit);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Schedule) {
			Schedule other = (Schedule) obj;
			return 
				this.operation == other.operation 
				&& this.sourceId == other.sourceId
				&& this.sourceBit == other.sourceBit
				&& this.destinationId == other.destinationId
				&& this.destinationBit == other.destinationBit;
		}
		return false;
	}
}
