package safeextractor.json;

public class Exp2Record {
	private int no;
	private String oldName;
	private String newName;
	private String path;
	private String projectName;
	private int offset;
	private int length; 

	public Exp2Record(String projectName, int no, String oldName, String newName, String path, int offset,int length) {
		this.projectName = projectName;
		this.no = no;
		this.oldName = oldName;
		this.newName = newName; 
		this.path = path;
		this.offset = offset; 
		this.length=length;
	} 

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getLine() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * @return the no
	 */
	public int getNo() {
		return no;
	}

	/**
	 * @param no the no to set
	 */
	public void setNo(int no) {
		this.no = no;
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

}
