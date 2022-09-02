package safeextractor.refactoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractTempRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringExecutionHelper;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import extractvariable.ast.LightASTParser;
import safeextractor.dataset.EvaluationMetaData;
import safeextractor.handlers.AfterWorkHandler;
import safeextractor.handlers.SampleHandler;
import safeextractor.json.Exp1Record;
import safeextractor.json.Exp2Record;
import safeextractor.json.Point;
import safeextractor.log.MyLog;
import safeextractor.utils.GlobalClass;
import safeextractor.utils.Utils;

public class MyExtractVariableRefactoring {

	public boolean flag;
	public ArrayList<Integer> extractList;
	public boolean evaluateRes;
	public MyExtractVariableRefactoring(IJavaProject javaProject, Exp1Record extractVariableRecord,String approach) {
		this.evaluateRes=false;
		try {
			flag = extractExp1(javaProject, extractVariableRecord,approach);
		} catch (Exception e) {
			System.out.println(extractVariableRecord.getOldName() + ": extract refactoring failed!");
			e.printStackTrace();
		}
	}

	public MyExtractVariableRefactoring(IJavaProject javaProject, Exp2Record extractVariableRecord,String approach) {
		evaluateRes=false;
		try {
			flag = extractExp2(javaProject, extractVariableRecord,approach);
		} catch (Exception e) {
			System.out.println(extractVariableRecord.getOldName() + ": extract refactoring failed!");
			e.printStackTrace();
		}
	}

	protected boolean extractExp1(IJavaProject javaProject, Exp1Record extractVariableRecord,String approach) throws Exception {
		if (javaProject == null || !javaProject.exists()) {
			return false;
		}
		IPackageFragment[] fragments = javaProject.getPackageFragments();
		boolean status = false;
		if (fragments.length == 0) {
			MyLog.add("null fragments!");
		}
		for (int j = 0; j < fragments.length; j++) {
			IPackageFragment fragment = fragments[j];
			if (!fragment.exists()) {
				continue;
			}
			ICompilationUnit[] iCompilationUnits = fragment.getCompilationUnits();
			for (int k = 0; k < iCompilationUnits.length; k++) {
				ICompilationUnit iCompilationUnit = iCompilationUnits[k];
				if (iCompilationUnit.getPath().toString().endsWith(extractVariableRecord.getPath())) {
					MyLog.add("-----------start------------------");
					CountExpressionVisitor exCountExpressionVisitor = preEvaluate(iCompilationUnit,
							extractVariableRecord); 
					MyLog.add("ecv init ");
					StringBuffer str = new StringBuffer(); 
					for (int i = 0; i < exCountExpressionVisitor.getPointList().size(); ++i) { 
						Point p =exCountExpressionVisitor.getPointList().get(i);
						str.append("(" + p.getLineNum() + "," + p.getFrequency() + "), ");
					} 
					MyLog.add(str.toString());

					GlobalClass.iCompilationUnit = iCompilationUnit; 
					if(exCountExpressionVisitor.getPointList().size()<=0) {
						continue;
					}
					for(int m=0;m<exCountExpressionVisitor.getPointList().size();++m) { 
						CompilationUnit compilationUnit = Utils.getCompilationUnit(iCompilationUnit);
						ExpressionVisitor ev = getExpressionVisitor(iCompilationUnit, extractVariableRecord.getOldName(),
								exCountExpressionVisitor.getPointList().get(m).getLineNum()); 
						int offset = ev.getOffset();
						if (offset == -1) {
							MyLog.add("404 in offset!");
							continue;
						}
						MyLog.add("offset:" + offset + ", OldName: " + extractVariableRecord.getOldName() + ", length: "
								+ extractVariableRecord.getOldName().length());
 
						boolean stat = true;
						SampleHandler.start= System.currentTimeMillis();
						SampleHandler.end= -1;
						if(approach.equals("ours")) {
							stat = extractedOurApproach(compilationUnit, offset,ev.getLength(),extractVariableRecord.getNewName());
						}else if(approach.equals("eclipse")) {
							stat = extractedEclipseApproach(compilationUnit, offset,ev.getLength(),extractVariableRecord.getNewName());
						}  
						SampleHandler.end= SampleHandler.end==-1? System.currentTimeMillis():SampleHandler.end;
						
						evaluateRes = evaluate(GlobalClass.iCompilationUnit, extractVariableRecord, exCountExpressionVisitor );
						if(evaluateRes ==true) {
							break;
						}else{ 
							 new AfterWorkHandler(javaProject);
							 try {
									ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
								} catch (Exception e) {
									e.printStackTrace();
								}
							 GlobalClass.JavaProject=SampleHandler.findJavaProject(javaProject.getElementName().toString());
						} 
					} 
					if(evaluateRes==false) {
						CompilationUnit compilationUnit = Utils.getCompilationUnit(iCompilationUnit);
						 
						SampleHandler.r=0;// SampleHandler.random.nextInt(exCountExpressionVisitor.getPointList().size());
						ExpressionVisitor ev = getExpressionVisitor(iCompilationUnit, extractVariableRecord.getOldName(),
								exCountExpressionVisitor.getPointList().get(SampleHandler.r).getLineNum());
						int offset = ev.getOffset();
						if (offset == -1) {
							MyLog.add("404 in offset!");
							continue;
						}
						MyLog.add("offset:" + offset + " ,OldName: " + extractVariableRecord.getOldName() + ", length: "
								+ extractVariableRecord.getOldName().length());

						GlobalClass.iCompilationUnit = iCompilationUnit; 
						boolean stat = true;
						SampleHandler.start= System.currentTimeMillis();
						SampleHandler.end=-1;
						if(approach.equals("ours")) {
							stat = extractedOurApproach(compilationUnit, offset,ev.getLength(),extractVariableRecord.getNewName());
						}else if(approach.equals("eclipse")) {
							stat = extractedEclipseApproach(compilationUnit, offset,ev.getLength(),extractVariableRecord.getNewName());
						}  
						SampleHandler.end= SampleHandler.end==-1? System.currentTimeMillis():SampleHandler.end;
					}
					  
					MyLog.add("handle: " + extractVariableRecord.getOldName() + ", " +evaluateRes + ", "
							+ extractVariableRecord.getCommitId() + ",line number: " + extractVariableRecord.getLine());
					int num = extractVariableRecord.getNo();
					if (evaluateRes == true) {
						EvaluationMetaData.metadatalist.add(num + "," + extractVariableRecord.getProjectName() + ","
								+ extractVariableRecord.getCommitId() + "," + extractVariableRecord.getNewName()
								+ ",1,"+approach);
					} else {
						EvaluationMetaData.metadatalist.add(num + "," + extractVariableRecord.getProjectName() + ","
								+ extractVariableRecord.getCommitId() + "," + extractVariableRecord.getNewName()
								+ ",0,"+approach);
					}
					MyLog.add("-----------end------------------");
					status = true;
				}
			}
		}
		if (status == false) {
			MyLog.add("not find " + extractVariableRecord.getPath());
		}
		return status;
	}

	protected boolean extractExp2(IJavaProject javaProject, Exp2Record extractVariableRecord,String approach)
			throws Exception {
		if (javaProject == null || !javaProject.exists()) {
			return false;
		}
		IPackageFragment[] fragments = javaProject.getPackageFragments();
		boolean status = false;
		if (fragments.length == 0) {
			MyLog.add("null fragments!");
		}
		for (int j = 0; j < fragments.length; j++) {
			IPackageFragment fragment = fragments[j];
			if (!fragment.exists()) {
				continue;
			}
			ICompilationUnit[] iCompilationUnits = fragment.getCompilationUnits();
			for (int k = 0; k < iCompilationUnits.length; k++) {
				ICompilationUnit iCompilationUnit = iCompilationUnits[k];
				if (iCompilationUnit.getPath().toString().endsWith(extractVariableRecord.getPath())) {
					MyLog.add("-----------start------------------"); 
					int offset = extractVariableRecord.getOffset();
					CompilationUnit compilationUnit = Utils.getCompilationUnit(iCompilationUnit);  
					MyLog.add("offset:" + offset + " ,OldName: " + extractVariableRecord.getOldName() + ", length: "
							+ extractVariableRecord.getOldName().length());
					GlobalClass.iCompilationUnit = iCompilationUnit;
					if(approach.toLowerCase().contains("eclipse")) {
						status =extractedEclipseApproach(compilationUnit, offset,extractVariableRecord.getLength(),
								extractVariableRecord.getNewName());
					}else if(approach.toLowerCase().contains("ours")) {
						status =extractedOurApproach(compilationUnit,offset,extractVariableRecord.getLength(),
								extractVariableRecord.getNewName());
					}
					MyLog.add("-----------end------------------"); 
				}
			}
		}
		if (status == false) {
			MyLog.add("not find " + extractVariableRecord.getPath());
		}
		return status;
	}

	public boolean extractedOurApproach(CompilationUnit compilationUnit, int offset, int length, String newName) {
		
		MyExtractTempRefactoring extractTempRefactoring = new MyExtractTempRefactoring(compilationUnit, offset,
				length); 
		extractTempRefactoring.setTempName(newName); 
		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(); 
			RefactoringExecutionHelper helper = new RefactoringExecutionHelper(extractTempRefactoring,
					RefactoringCore.getConditionCheckingFailedSeverity(),
//			RefactirubgCore.CheckConditionsOperation()
					RefactoringSaveHelper.SAVE_NOTHING, shell, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			helper.perform(true, true);   
		} catch (Exception e) {
			e.printStackTrace();
			extractList=extractTempRefactoring.replaceList;
			return extractTempRefactoring.checkCondition;
		}
		extractList=extractTempRefactoring.replaceList;
		return extractTempRefactoring.checkCondition;
	}
	
	

	public boolean extractedEclipseApproach(CompilationUnit compilationUnit, int offset, int length, String newName) {
		EclipseExtractTempRefactoring extractTempRefactoring = new EclipseExtractTempRefactoring(compilationUnit,
				offset, length);
		extractTempRefactoring.setTempName(newName);

		try {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

			RefactoringExecutionHelper helper = new RefactoringExecutionHelper(extractTempRefactoring,
					RefactoringCore.getConditionCheckingFailedSeverity(),
					RefactoringSaveHelper.SAVE_NOTHING, 
					shell, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			helper.perform(true, true);
		} catch (Exception e) {
			e.printStackTrace();
			extractList=extractTempRefactoring.replaceList;
			return extractTempRefactoring.checkCondition;
		}
		extractList=extractTempRefactoring.replaceList;
		return extractTempRefactoring.checkCondition;
	}

	private ExpressionVisitor getExpressionVisitor(ICompilationUnit iCompilationUnit, String originalExpression,
			int lineNum) {
		LightASTParser lightASTParser = new LightASTParser(iCompilationUnit);
		ExpressionVisitor expressionVisitor = new ExpressionVisitor(lightASTParser.getCompilationUnit(),
				originalExpression, lineNum);
		lightASTParser.getCompilationUnit().accept(expressionVisitor);
		return expressionVisitor;
	}

	private CountExpressionVisitor preEvaluate(ICompilationUnit iCompilationUnit, Exp1Record extractVariableRecord) {
		LightASTParser lightASTParser = new LightASTParser(iCompilationUnit);
		CountExpressionVisitor countExpressionVisitor = new CountExpressionVisitor(lightASTParser.getCompilationUnit(),
				extractVariableRecord);
		lightASTParser.getCompilationUnit().accept(countExpressionVisitor);
		return countExpressionVisitor;
	}
	private boolean evaluate(ICompilationUnit iCompilationUnit, Exp1Record extractVariableRecord,
			CountExpressionVisitor exCountExpressionVisitor) { 
		StringBuffer str = new StringBuffer("exCountExpressionVisitor.getPointList: "); 
		List<Point> pointList = new ArrayList<>();
		for(Point p:exCountExpressionVisitor.getPointList() )
		pointList.add(new Point(p.getLineNum(),p.getOffset(),p.isLegal(),p.getFrequency()
				,p.getLength()));
		Collections.sort(pointList, new Comparator<Point>() {
			@Override
			public int compare(Point p1, Point p2) {
				if(p1.getOffset()<=p2.getOffset() && 
						p1.getOffset() + p1.getLength()  >=  p2.getOffset() + p2.getLength()) {
					 return  1;
				}else if (p1.getOffset()==p2.getOffset() && 
						p1.getOffset() + p1.getLength()  ==  p2.getOffset() + p2.getLength()) {
					return 0;
				}else {
					return -1;
				}  
			}
		}); 
		LightASTParser lightASTParser = new LightASTParser(iCompilationUnit);
		CompilationUnit unit = lightASTParser.getCompilationUnit();
		for (int i = 0; i < pointList.size(); ++i) { 
			Point p = pointList.get(i);
			str.append("(" + p.getLineNum() + ", "+p.getOffset()+", "+p.getLength()+", "+unit.getLineNumber(p.getOffset()+p.getLength())
			+", " + p.getFrequency() + "), ");
		} 
		str.append("\nExtractList: ");
		Collections.sort(this.extractList,Collections.reverseOrder()); 
		 
		for (int i = 0; i <this.extractList.size(); ++i) {
			int offset=this.extractList.get(i);
			str.append(unit.getLineNumber(offset)+", "+this.extractList.get(i));
		}
		MyLog.add(str.toString());
		for (int i = 0; i <this.extractList.size(); ++i) {
			int offset=this.extractList.get(i); 
			boolean isFound=false;
			for(int j=0;j< pointList.size();++j) {
				Point point =  pointList.get(j);
				 
				if(point.getLineNum() == unit.getLineNumber(offset) 
						 || point.getOffset()<=offset && point.getOffset()+point.getLength()>offset) {
					int f =  point.getFrequency();
					if(f==0) { 
						continue;
					}
					pointList.get(j).setFrequency(f-1);
					isFound=true;
					break;
				} 
			}  
			if(isFound==false) {
				return false;
			}
		}
		for(int j=0;j< pointList.size();++j) { 
			int f= pointList.get(j).getFrequency();
			if(f!=0) {
				return false;
			} 
		} 
		return true;
	}
}
