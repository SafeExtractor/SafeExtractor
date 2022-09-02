package extractvariable.ast.rw;

import org.eclipse.jdt.core.dom.ASTNode;
import java.util.List;

public abstract class RWBase {
    protected ASTNode expression; 
    protected int middleCodeFlag;
    protected String key;
    protected List<String> resList;
    protected ASTNode extractVariable; 

    public RWBase(ASTNode expression, int middleCodeFlag, ASTNode extractVariable) {
        this.expression = expression; 
        this.middleCodeFlag=middleCodeFlag;
        this.extractVariable=extractVariable;
        this.resList = null;
        RWVisitor.start_time=System.currentTimeMillis();
		
    }

    public abstract void print();

    public List<String> getResList() {
        return resList;
    }
}
