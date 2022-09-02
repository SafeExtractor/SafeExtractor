package safeextractor.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import safeextractor.dataset.ElemInfo;
import safeextractor.json.Exp1Record;
import safeextractor.json.Point;
import safeextractor.refactoring.AfterCountExpressionVisitor.FrequencyVisitor;

public class CountExpressionVisitor  extends ASTVisitor{

	 	private CompilationUnit unit;
	    private String expression; 
	    private int key; 
	    private List<Point> pointList;
	    private Exp1Record exp1Record;
	    public CountExpressionVisitor(CompilationUnit unit, Exp1Record exp1Record) {
	    	this.unit=unit;
	    	this.expression=exp1Record.getOldName();
	    	this.pointList=new ArrayList<>();
	    	this.exp1Record=exp1Record; 
	    	this.key=-1;
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
	
	    public int getKey() {
	        return key;
	    }

	     
	    @Override
		public void preVisit(ASTNode node) {
			if(node instanceof Statement) {
				int lineNum=unit.getLineNumber(node.getStartPosition());
				for(ElemInfo e:this.exp1Record.getElemList()) {
					if(e.getStartLine()==lineNum) { 
						FrequencyVisitor myVisitor=new FrequencyVisitor(expression,node);
						node.accept(myVisitor);	
						if(myVisitor.frequency>0) { 
							ASTNode temp=node;
							while(temp!=null&&key==-1) {
								temp = temp.getParent();
								if(temp instanceof MethodDeclaration) {
									this.key=unit.getLineNumber(temp.getStartPosition());
								}
							}
							Point point =new Point(lineNum,node.getStartPosition(),myVisitor.frequency,node.getLength());
							pointList.add(point); 
							break;
						}
					}
				}
			}
			super.preVisit(node);
		}
	    
	    class FrequencyVisitor extends ASTVisitor{
			String expression;
			int frequency;
			ASTNode root;
			FrequencyVisitor(String expression,ASTNode astnode){
				this.expression=expression;
				frequency=0;
				this.root=astnode;
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
