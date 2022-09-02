package safeextractor.json;

import java.util.ArrayList;
import java.util.List;

import safeextractor.dataset.ElemInfo;


public class Exp1Record {
	private int no;
    private String commitId;
    private String oldName;
    private String newName;
    private String path;
    private String projectName;
    private int line;
    private int column;
    private List<ElemInfo> elemList; 

    public Exp1Record(String projectName,int no,String commitId, String oldName, String newName, String path, int line, int column) {
        this.projectName=projectName;
        this.no=no;
    	this.commitId = commitId;
        this.oldName = oldName;
        this.newName = newName;
        this.path = path;
        this.line = line;
        this.column = column;
        elemList=new ArrayList<>();
    } 

    public List<ElemInfo> getElemList() {
        return elemList;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    
    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
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
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
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
    
    
}

