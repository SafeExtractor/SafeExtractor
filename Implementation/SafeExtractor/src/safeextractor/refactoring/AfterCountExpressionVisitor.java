package safeextractor.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import safeextractor.json.Exp1Record;
import safeextractor.json.Point;
import safeextractor.log.MyLog;

public class AfterCountExpressionVisitor extends ASTVisitor{
	private CompilationUnit unit;
    private String expression; 
    private int key; 
    private List<Point> pointList; 
    public AfterCountExpressionVisitor(CompilationUnit unit, Exp1Record exp1Record,int key) {
    	this.unit=unit;
    	this.expression=exp1Record.getNewName();
    	this.pointList=new ArrayList<>(); 
    	this.key=key; 
		MyLog.add("key is "+key);
    } 

    
    public CompilationUnit getUnit() {
        return unit;
    }

    public void setUnit(CompilationUnit unit) {
        this.unit = unit;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public List<Point> getPointList() {
        return this.pointList;
    } 
    class FrequencyVisitor extends ASTVisitor{
		String expression;
		int frequency;
		ASTNode root; 
		FrequencyVisitor(String expression,ASTNode root){
			this.expression=expression;
			this.root=root;
			frequency=0; 
		}
		@Override
		public boolean preVisit2(ASTNode node) { 
			if(root.equals(node)) {
				return true;
			}else if(node instanceof Statement) {
				FrequencyVisitor myVisitor=new FrequencyVisitor(expression,node);
				node.accept(myVisitor);	
				if(myVisitor.frequency>0) {
					this.frequency-=myVisitor.frequency; 
				}
			}
			if(node.toString().equals(this.expression)) {
				frequency++;
				return false;
			}
			return true;
		}
	}
    
    
}
