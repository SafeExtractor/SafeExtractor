package safeextractor.dataset;

import static java.util.Objects.hash;

public class ElemInfo {
    int startLine;
    int endLine;
    int startColumn;
    int endColumn;

    public ElemInfo(int startLine, int endLine, int startColumn, int endColumn) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    @Override
    public int hashCode() {
        return hash(startLine,startColumn,endLine,endColumn);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj!=null&& obj instanceof ElemInfo) {
            ElemInfo elemInfo = (ElemInfo)obj;
            if (elemInfo.startLine==this.startLine&&
            elemInfo.startColumn==this.startColumn&&
            elemInfo.endLine==this.endLine&&
            elemInfo.endColumn==this.endColumn) {
                return true;
            }
        }
        return false;
    }
}
