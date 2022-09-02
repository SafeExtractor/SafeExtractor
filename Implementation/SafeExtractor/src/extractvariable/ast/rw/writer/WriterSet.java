package extractvariable.ast.rw.writer;

import extractvariable.ast.rw.RWBase;
import extractvariable.detector.Comparator;
import safeextractor.log.MyLog;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WriterSet extends RWBase { 
    Boolean staticFlag;

    public WriterSet(ASTNode expression ,int middleCodeFlag,String key,ASTNode extractNode){
        super(expression ,middleCodeFlag,extractNode);
        this.staticFlag=false; 
        this.key=key;
        try {
            this.resList = generateWriterList(expression );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    List<String> generateWriterList(ASTNode expr ) throws IOException {
        WriterVisitor writerVisitor = new WriterVisitor(new HashSet<ITypeBinding>(), key,key,"",
        		this.middleCodeFlag,this.extractVariable,0,false,new ArrayList<String>());
        expr.accept(writerVisitor);
        this.staticFlag=writerVisitor.isStaticFlag();
        List<String>res =writerVisitor.getAPIList(); 
        return res;
    }

    public void print() {
    	StringBuffer sb=new StringBuffer();
    	sb.append("The Result of Writer is:\t");
        if(this.resList!=null){
            Set<String> set=this.resList.stream().collect(Collectors.toSet()); 
            set.forEach(k->sb.append(k+", "));
            MyLog.add(sb.toString());
        }
    }

    public Boolean getStaticFlag() {
        return staticFlag;
    }
}
