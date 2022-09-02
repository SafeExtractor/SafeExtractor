package extractvariable.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;

public class MethodDeclarationVisitor extends ASTVisitor {
    public MethodDeclarationVisitor() {
        super();
    }
    @Override
    public boolean visit(InfixExpression infixExpression)  {
        InfixExpression.Operator op=infixExpression.getOperator();
        if(op.equals(InfixExpression.Operator.EQUALS) ){
            System.out.println(infixExpression.getLeftOperand().toString());
        }
        return true;
    }

}
