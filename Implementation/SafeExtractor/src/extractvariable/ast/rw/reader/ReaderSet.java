package extractvariable.ast.rw.reader;

import extractvariable.ast.rw.RWBase;
import extractvariable.detector.Comparator;
import safeextractor.log.MyLog;

import org.eclipse.jdt.core.dom.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReaderSet  extends RWBase {

    public ReaderSet( ASTNode expression,int middleCodeFlag,String key,ASTNode extractNode ){
        super( expression,middleCodeFlag,extractNode ); 
        this.key=key;
        try {
            this.resList = generateReaderList(expression, new HashSet<ASTNode>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    List<String> generateReaderList(ASTNode exp, HashSet<ASTNode> set) throws IOException {
        List<String> res = new ArrayList<>();
        ReaderVisitor readerVisitor = new ReaderVisitor(new HashSet<ITypeBinding>(),key,key,"",
        		this.middleCodeFlag,this.extractVariable,0,false,new ArrayList<String>());
        exp.accept(readerVisitor);
        res = readerVisitor.getAPIList();
		if(this.middleCodeFlag==0) {
			Comparator.exceptionMethod.targetReaderList.addAll(res);
		}  
        return res;
    }


    public void print() {
        StringBuffer sb=new StringBuffer();
    	sb.append("The Result of Reader is:\t");
        if(this.resList!=null){
            Set<String> set=this.resList.stream().collect(Collectors.toSet()); 
            set.forEach(k->sb.append(k+", "));
            MyLog.add(sb.toString());
        }
    }
}
