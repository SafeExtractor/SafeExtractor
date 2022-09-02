package safeextractor.json;

public class Point {

    int lineNum;
    int offset;
    int length;
    int frequency;
    boolean legal;
    public Point(int lineNum, int offset,Boolean legal,int frequency,int length) {
        this.lineNum = lineNum;
        this.offset = offset;
        this.legal=legal;
        this.frequency= frequency;
        this.length=length;
    }

    
    public Point(int lineNum, int offset,int frequency,int length) {
        this.lineNum = lineNum; 
        this.offset = offset;
        this.legal=false;
        this.frequency= frequency;
        this.length=length;
    }

    
    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getFrequency() {
        return  frequency;
    }

    public void setColumn(int column) {
        this. frequency = column;
    }

    public int getOffset() {
        return offset;
    }

    public void setFrequency(int  frequency) {
        this.frequency = frequency;
    }
    
    public boolean isLegal() {
        return legal;
    }

    public void setLegal(boolean legal) {
        this.legal = legal;
    }


	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}


	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}


	/**
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

}
