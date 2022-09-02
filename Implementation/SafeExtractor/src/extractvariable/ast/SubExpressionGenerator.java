package extractvariable.ast;

import org.eclipse.jdt.core.dom.*;
 
import java.io.IOException;
import java.util.*;

public class SubExpressionGenerator {
	private ASTNode commonExpression;
	private List<ASTNode> subExpressionList;
    
    public ASTNode getCommonExpression() {
		return commonExpression;
	}

	public void setCommonExpression(ASTNode commonExpression) {
		this.commonExpression = commonExpression;
	}
	
    public SubExpressionGenerator( ASTNode commonExpression ) throws IOException {
    	 
    	this.commonExpression=commonExpression;
         MyExpressionVisitor ev=new MyExpressionVisitor(commonExpression);
         commonExpression.accept(ev);
         this.subExpressionList= new ArrayList<ASTNode>(ev.subExpressionSet);
    } 
    

class MyExpressionVisitor extends ASTVisitor{
	ASTNode expression;
	Set<ASTNode> subExpressionSet;
	MyExpressionVisitor(ASTNode expression){
		this.expression=expression;
		subExpressionSet=new HashSet<ASTNode>();
	}
	
	
    @Override
	public void preVisit(ASTNode node) { 
		if(node instanceof MethodInvocation) {
			MethodInvocation mi=( MethodInvocation)node; 
				this.subExpressionSet.add(mi);
			for(int i=0;i<mi.arguments().size();++i) {
				Object object = mi.arguments().get(i);
				if(mi.arguments().get(i) instanceof ASTNode) {
					this.subExpressionSet.add((ASTNode)object);
				}
			}
		}else if(node instanceof FieldAccess) {
			FieldAccess fa=(FieldAccess)node; 
			this.subExpressionSet.add(fa);
		}else if(node instanceof QualifiedName) {
			QualifiedName qn=( QualifiedName)node;  
			this.subExpressionSet.add(qn);
		}
	}
}

}
