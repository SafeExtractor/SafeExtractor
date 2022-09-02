package extractvariable.api;

import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class APIRecorder {
    AbstractTypeDeclaration abstractTypeDeclaration;
    HashSet<APIFieldRecord> apiFieldRecordHashSet;
    public boolean buildFlag;
    public APIRecorder(AbstractTypeDeclaration abstractTypeDeclaration,
    		HashMap<IBinding, AbstractTypeDeclaration> typeDeclarationHashMap ) {
        this.abstractTypeDeclaration=abstractTypeDeclaration; 
        this.apiFieldRecordHashSet=new HashSet<>(); 
        this.buildFlag=false;  
        build();
    }
    
    public void build() {
    	ITypeBinding tempITypeBinding=abstractTypeDeclaration.resolveBinding(); 
        this.apiFieldRecordHashSet=new HashSet<>(); 
        do{
            IVariableBinding[] declaredFields = tempITypeBinding.getDeclaredFields();
            for(int j=0;j<declaredFields.length;++j){
                apiFieldRecordHashSet.add(new APIFieldRecord(declaredFields[j]));
            } 
            tempITypeBinding= tempITypeBinding.getSuperclass();
        }while(tempITypeBinding!=null);
        ASTNode tempASTNode=abstractTypeDeclaration ;
        while(tempASTNode.getParent()!=null){
            tempASTNode=tempASTNode.getParent();
            if(tempASTNode instanceof TypeDeclaration){
                ITypeBinding iTypeBinding= ((TypeDeclaration) tempASTNode).resolveBinding();
                IVariableBinding[] declaredFields = iTypeBinding.getDeclaredFields();
                for(int j=0;j<declaredFields.length;++j){
                    apiFieldRecordHashSet.add(new APIFieldRecord(declaredFields[j]));
                }
            }else if(tempASTNode instanceof EnumDeclaration){
                ITypeBinding iTypeBinding= ((EnumDeclaration) tempASTNode).resolveBinding();
                IVariableBinding[] declaredFields = iTypeBinding.getDeclaredFields();
                for(int j=0;j<declaredFields.length;++j){
                    apiFieldRecordHashSet.add(new APIFieldRecord(declaredFields[j]));
                }
            }
        }
        this.buildFlag=true;
    }
    
    public HashSet<APIFieldRecord> getApiFieldRecordHashSet() {
        return apiFieldRecordHashSet;
    }
}
