package extractvariable.ast.rw;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.List;

public class AllVisitor extends  ASTVisitor{
    List<ASTNode> astNodes;
    public AllVisitor() {
        astNodes=new ArrayList<>();
    }

    public List<ASTNode> getAstNodes() {
        return astNodes;
    }

    @Override
    public boolean preVisit2(ASTNode node) {
        if(node instanceof SimpleName){
            astNodes.add(node);
            return  false;
        }
        return super.preVisit2(node);
    }

}
