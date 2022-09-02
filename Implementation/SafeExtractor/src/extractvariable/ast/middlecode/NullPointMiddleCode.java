package extractvariable.ast.middlecode;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import safeextractor.log.MyLog;

public class NullPointMiddleCode {				
	Boolean flag;
    public NullPointMiddleCode(CompilationUnit compilationUnit,ASTNode commonNode,Set<String> invocationSet,int startPosition,int endPostion) {

    	NullMiddleCodeVisitor nullMiddleCodeVisitor=new NullMiddleCodeVisitor(commonNode,startPosition,endPostion);
        commonNode.accept(nullMiddleCodeVisitor);
        this.flag=true;
		for (ASTNode node:nullMiddleCodeVisitor.getAllStatement()){ 
			NullASTVisitor nullASTVisitor=new NullASTVisitor( invocationSet,node);
			node.accept(nullASTVisitor);
			this.flag=nullASTVisitor.isValid();
			if(this.flag==false) {
				break;
			} 
        }

    }
    
    public Boolean isValid() {
    	return this.flag;
    }
    class NullMiddleCodeVisitor extends ASTVisitor{
		int startPosition;
	    int endPosition;
	    CompilationUnit compilationUnit; 
	    HashSet<ASTNode> allStatement; 
	    ASTNode commonNode;
	    public NullMiddleCodeVisitor(ASTNode commonNode ,int startPosition, int endPosition) {
	        this.startPosition=startPosition;
	        this.endPosition=endPosition;  
	        this.commonNode=commonNode; 
	        this.allStatement=new HashSet<ASTNode>();
	    }

	    @Override
	    public boolean preVisit2(ASTNode node) {
	        ASTNode temp=node;
	        int sl= node.getStartPosition() ;
	        int el= node.getStartPosition()+node.getLength() ;
	        if(el<startPosition||sl>endPosition) {
	        	return false;
	        }
	        if(sl>=startPosition && el <=endPosition ){
	            this.allStatement.add(temp);
	            return false;
	        }
	        return super.preVisit2(node);
	    }

	    public HashSet<ASTNode> getAllStatement() {
	        return allStatement;
	    }

	    public void setAllStatement(HashSet<ASTNode> allStatement) {
	        this.allStatement = allStatement;
	    }
	}
    
    class NullASTVisitor extends ASTVisitor{
    	Set<String> invocationSet; 
	    ASTNode astNode;
	    Boolean flag;
	    public NullASTVisitor(Set<String> invocationSet,ASTNode astNode){
	    	this.invocationSet=invocationSet;
	    	this.astNode=astNode; 
	    	this.flag=true;
	    }
	    
	    @Override
		public boolean visit(InfixExpression infixExpression) {
			if(this.flag==false) {
				return false;
			}
	    	Operator op=infixExpression.getOperator(); 
	    	if(Operator.toOperator(op.toString())==Operator.EQUALS||Operator.toOperator(op.toString())==Operator.NOT_EQUALS) {
	    		Expression leftExpression = infixExpression.getLeftOperand();
	    		Expression rightExpression = infixExpression.getRightOperand();
	    		Expression target=null;
	    		if( rightExpression.getNodeType()==Expression.NULL_LITERAL) {
	    			target=leftExpression;
	    		}else if(leftExpression.getNodeType()==Expression.NULL_LITERAL) {
	    			target=rightExpression;
	    		}
	    		if(target!=null&&this.invocationSet.contains(target.toString())) {
	    			this.flag=false; 
	    			return false;
	    		}
	    	} 
			return true;
		}
	    public Boolean isValid() {
	    	return this.flag;
	    }
    }
}
