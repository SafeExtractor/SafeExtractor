package extractvariable.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExceptionMethod {
	public List<String> targetReaderList; 
	public List<List<String>> middleCodeReaderList; 
	public ExceptionMethod() {
		super();
		this.targetReaderList=new ArrayList<>();
		this.middleCodeReaderList=new ArrayList<>();
	}
	public boolean isConflict() {
		if(this.targetReaderList.size()==0||middleCodeReaderList.size()==0) {
			return false;
		}
		Set<String> set = new HashSet<String>();  
		for(int i=0;i<this.middleCodeReaderList.size();++i) {
			set.addAll(this.middleCodeReaderList.get(i));
		}
		for(int i=0;i<this.targetReaderList.size();++i) {
			if(set.contains(this.targetReaderList.get(i))) {
				return true;
			}
		}
		return false;
	}
	
}
