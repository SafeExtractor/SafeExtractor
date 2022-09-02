package safeextractor.refactoring;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import safeextractor.log.MyLog;


public class ExpressionVisitor extends ASTVisitor{
	private int offset;
	private CompilationUnit unit;
	private String originalExpression;
	private int lineNum;
	private int realLength;
	
	
	public ExpressionVisitor(CompilationUnit unit, String originalExpression, int lineNum) {
		super();
		this.unit = unit;
		this.originalExpression = originalExpression;
		this.lineNum = lineNum;
		this.offset = -1;
		this.realLength=0;
	}
	@Override
	public void preVisit(ASTNode node) {
		String nodeString = node.toString(); 
		if (nodeString.equals(this.originalExpression) && unit.getLineNumber(unit.getExtendedStartPosition(node)) == lineNum) {
			this.offset = node.getStartPosition();//unit.getExtendedStartPosition(node) node.getStartPosition()
			this.realLength=unit.getExtendedLength(node);
			MyLog.add("got it "+unit.getLineNumber(this.offset)+","+unit.getColumnNumber(this.offset)
			+","+this.offset+" len: "+this.realLength);
		} else if(this.offset==-1&&nodeString.equals(this.originalExpression)) { 
			this.offset = unit.getExtendedStartPosition(node);
			this.realLength=unit.getExtendedLength(node);
			MyLog.add("badly got it "+unit.getLineNumber(this.offset)+","+unit.getColumnNumber(this.offset)
			+","+this.offset+" len: "+this.realLength);
		}
		super.preVisit(node);
	}
	
	public int getOffset() {
		return offset;
	}
	public int getLength() {
		return this.realLength;
	}
	
}