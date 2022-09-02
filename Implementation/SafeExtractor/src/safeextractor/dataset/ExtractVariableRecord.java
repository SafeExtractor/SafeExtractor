package safeextractor.dataset;

import java.util.Set;

public class ExtractVariableRecord {
    String beforeCommit;
    String afterCommit;
    String oldName;
    String newName;
    String type;
    String filePath;
    ElemInfo variablePlace;
    Set<ElemInfo> beforeSet;
    Set<ElemInfo> afterSet;

    public ExtractVariableRecord(String beforeCommit, String afterCommit, String oldName, String newName, String type, String filePath, ElemInfo variablePlace, Set<ElemInfo> beforeSet, Set<ElemInfo>afterSet) {
        this.beforeCommit = beforeCommit;
        this.afterCommit = afterCommit;
        this.oldName = oldName;
        this.newName = newName;
        this.type = type;
        this.filePath = filePath;
        this.variablePlace = variablePlace;
        this.beforeSet = beforeSet;
        this.afterSet = afterSet;
    }

    public String getBeforeCommit() {
        return beforeCommit;
    }

    public void setBeforeCommit(String beforeCommit) {
        this.beforeCommit = beforeCommit;
    }

    public String getAfterCommit() {
        return afterCommit;
    }

    public void setAfterCommit(String afterCommit) {
        this.afterCommit = afterCommit;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Set<ElemInfo> getBeforeSet() {
        return beforeSet;
    }

    public void setBeforeSet(Set<ElemInfo> beforeSet) {
        this.beforeSet = beforeSet;
    }

    public Set<ElemInfo> getAfterSet() {
        return afterSet;
    }

    public void setAfterSet(Set<ElemInfo> afterSet) {
        this.afterSet = afterSet;
    }

    public ElemInfo getVariablePlace() {
        return variablePlace;
    }

    public void setVariablePlace(ElemInfo variablePlace) {
        this.variablePlace = variablePlace;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
