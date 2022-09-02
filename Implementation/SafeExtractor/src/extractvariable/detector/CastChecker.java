package extractvariable.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeLiteral;

import safeextractor.log.MyLog;

import org.eclipse.jdt.core.dom.InstanceofExpression;
 

public class CastChecker {
	private ASTNode expression;
	private int startOffset;
	private int endOffset;
	private ASTNode commonNode;
	private Set<String> invocationSet;
	public CastChecker(ASTNode commonNode,ASTNode expression,int startOffset,int endOffset) {
		this.commonNode=commonNode;
		this.expression=expression;
		this.startOffset=startOffset;
		this.endOffset=endOffset;
		this.invocationSet=new HashSet<String>();
		if(this.endOffset-startOffset==this.expression.getLength()) {
			return;
		} 
		InvocationVisitor iv=new InvocationVisitor();
		this.expression.accept(iv);
		this.invocationSet=iv.invocationSet;
	}

	public boolean isExistInstanceAndCast() {
		CastMiddleCodeVisitor castMiddleCodeVisitor=new CastMiddleCodeVisitor(new ArrayList<String>(this.invocationSet),this.startOffset,this.endOffset);
        this.commonNode.accept(castMiddleCodeVisitor);
        
		return castMiddleCodeVisitor.isValid();
	}

	private class InvocationVisitor extends ASTVisitor{
		Set<String> invocationSet;
		InvocationVisitor(){
			this.invocationSet=new HashSet<String>();
		}
		@Override
		public void preVisit(ASTNode node) {
			if(node instanceof MethodInvocation) {
				MethodInvocation mi=( MethodInvocation)node;
				Expression temp = mi.getExpression();
				if(temp!=null)
				this.invocationSet.add(temp.toString());
			}else if(node instanceof FieldAccess) {
				FieldAccess fa=( FieldAccess)node;
				Expression temp = fa.getExpression();
				if(temp!=null)
				this.invocationSet.add(temp.toString());
			}else if(node instanceof QualifiedName) {
				QualifiedName qn=( QualifiedName)node;
				SimpleName temp = qn.getName();
				if(temp!=null)
				this.invocationSet.add(temp.toString());
			}else if(node instanceof SimpleName) {
				SimpleName temp=( SimpleName)node;
				if(temp!=null)
				this.invocationSet.add(temp.toString());
			}else if(node instanceof ArrayAccess) {
				ArrayAccess temp=( ArrayAccess)node;
				if(temp!=null)
				this.invocationSet.add(temp.toString());
			}
		}
	}
	
	private class CastMiddleCodeVisitor extends ASTVisitor{
		int startPosition;
	    int endPosition;
	    List<String> list;
	    HashMap<String,String> instanceOfSet;
	    HashMap<String,String> castSet;
	    public CastMiddleCodeVisitor( List<String> list,int startPosition, int endPosition) {
	        this.startPosition=startPosition;
	        this.endPosition=endPosition;
	        this.list=list;
	        this.instanceOfSet=new HashMap<>();
	        this.castSet=new HashMap<>();
	    }
	    
	    private boolean isInstanceof(int s,int e,ASTNode node) {
	    	if(s<startPosition||e>endPosition) {
	        	return false;
	        }  
	    	if(node instanceof InstanceofExpression) {
	    		InstanceofExpression exp=(InstanceofExpression)node;
	    		this.instanceOfSet.put(exp.getLeftOperand().toString(), exp.getRightOperand().toString()) ;
	    		return true;
	    	}
	    	if(node instanceof MethodInvocation) {
	    		IMethodBinding resolveMethodBinding = ((MethodInvocation) node).resolveMethodBinding();
				if(resolveMethodBinding==null) {
					return false;
				}
				MethodInvocation mi=(MethodInvocation)node;
	    		String str=resolveMethodBinding.getKey();
	    		 
	    		if(str.startsWith("Ljava/lang/Class<")
	    				&&str.endsWith(">;.isInstance(Ljava/lang/Object;)Z")&&mi.getExpression() instanceof TypeLiteral) {
	    			this.instanceOfSet.put(mi.arguments().get(0).toString(),
	    					((TypeLiteral)(mi.getExpression())).getType().toString()) ;
	    			return true;
	    		}
	    	}
			return false;
	    }
	    
	    private boolean isCast(int s,int e,ASTNode node) {
	    	if(s<startPosition||e>endPosition) { 
	        	return false;
	        } 
	    	if(node instanceof CastExpression) {
	    		CastExpression exp=(CastExpression)node;
	    		this.castSet.put(exp.getExpression().toString(),exp.getType().toString());
	    		return true;
	    	}
	    	if(node instanceof MethodInvocation) {
	    		IMethodBinding resolveMethodBinding = ((MethodInvocation) node).resolveMethodBinding();
				MethodInvocation mi=(MethodInvocation) node;
	    		if(resolveMethodBinding==null) {
					return false;
				} 
	    		String str=resolveMethodBinding.getKey();  
	    		if(str.startsWith("Ljava/lang/Class<")
	    				&& str.contains(">;.cast(Ljava/lang/Object;)")
	    				) { 
	    			this.castSet.put(mi.arguments().get(0).toString(),
	    					((TypeLiteral)(mi.getExpression())).getType().toString()) ; 
	    			return true;
	    		}
	    	}
			return false;
	    }
	    
	    @Override
	    public boolean preVisit2(ASTNode node) {
	        int sl= node.getStartPosition() ;
	        int el= node.getStartPosition()+node.getLength();
	        if(el<startPosition||sl>endPosition ) {
	        	return true;
	        } 
	        if(sl>=startPosition && el <=endPosition ){
	        	 isCast(sl,el,node);
	        	 isInstanceof(sl,el,node);
	        }
	        return super.preVisit2(node);
	    }
	    public boolean isValid() {
	    	boolean flag=false; 
	    	for(int i=0;i<this.list.size();++i) {
	    		String v=this.list.get(i); 
	    		if(this.castSet.get(v)!=null&&this.instanceOfSet.get(v)!=null
	    				&& this.castSet.get(v).equals(this.instanceOfSet.get(v))) {
	    			flag=true;
	    			break;
	    		}
	    	}
	    	return flag;
	    }
	} 
	
}
