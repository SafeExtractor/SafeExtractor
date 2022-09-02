package safeextractor.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import extractvariable.ast.LightASTParser;
import extractvariable.ast.PotentialTargetVisitor;
import safeextractor.dataset.EvaluationMetaData;
import safeextractor.json.Exp1Record;
import safeextractor.json.Exp2Record;
import safeextractor.json.HandleDataSetJsonFile;
import safeextractor.json.TimeRecord;
import safeextractor.log.MyLog;
import safeextractor.refactoring.MyExtractVariableRefactoring;
import safeextractor.utils.Constants;
import safeextractor.utils.GlobalClass;
import safeextractor.utils.Utils;

import org.eclipse.jface.dialogs.MessageDialog;

public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {    
			handleCommand(window);  
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	} 
	private void handleCommand(IWorkbenchWindow window) throws Exception { 
		System.out.println("If you are replicating the evaluation, please type 1. Otherwise, type 2 to replicate the case study");
		Scanner input = new Scanner(System.in);
		int modeNum = input.nextInt(); 
		System.out.println("mode type:" + modeNum); 
		if (modeNum == 2) {
			System.out.println("Case Study! please input project name in eclipse");
			String projectName = input.next();
			String p = Constants.CaseStudyHashMap.get(projectName);//input.next();
			int no = -1; //-1input.nextInt(); 
			System.out.println("input :" + projectName  );
			if(p==null) {
				MyLog.add("cannot find project!");
				return;
			}
			String path = Constants.DATASET_ROOT +"exp1/sample/"+p + ".json";
			List<Exp1Record> recordList = HandleDataSetJsonFile.JsonParserExp1(path);
			GlobalClass.JavaProject = findJavaProject(projectName);
			if (GlobalClass.JavaProject == null) {
				MyLog.add("cannot find project!");
				return;
			} else {
				System.out.println("get project " + GlobalClass.JavaProject.getElementName());
			}
			MyLog.add("name is:" + GlobalClass.JavaProject.getElementName());
			exp1(projectName, no, recordList);
		} else if (modeNum == 1) {
			System.out.println("Evaluation! input project name in eclipse");
			String projectName =  input.next(); 
			String dataPath = Constants.DATASET_ROOT + "/exp2/" + projectName + ".json";
			int no =  -1; //input.nextInt(); 
			GlobalClass.JavaProject = findJavaProject(projectName);
			if (GlobalClass.JavaProject == null) {
				MyLog.add("cannot find project!");
				return;
			} else {
				System.out.println("get project " + GlobalClass.JavaProject.getElementName());
			}
			exp2(dataPath, no);

		} 
//		else if (modeNum == 3) {
//			System.out.println("input project name in eclipse, src path ");
//			String projectName = input.next(); // input.next(); Time Closure Mockito Math
//			String srcPath = input.next(); // input.next(); src/main/java
//			String outputPath = Constants.DATASET_ROOT + "/exp2/" + projectName + ".json";
//			storeExp2Data(projectName, srcPath, outputPath);
//		} 
		else {
			MyLog.add("error Mode!");
			MyLog.show();
			MyLog.clear();
			return;
		}
		MyLog.show();
		MyLog.clear();
		MessageDialog.openInformation(window.getShell(), "Evaluate",
				GlobalClass.JavaProject.getPath().lastSegment() + " Finish!");

	}

	/**
	 * @param dataPath
	 * @param no
	 * @throws Exception
	 * @throws JavaModelException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void exp2(String dataPath, int no) throws Exception, JavaModelException, IOException, InterruptedException {
		List<Exp2Record> allRecordList = HandleDataSetJsonFile.JsonParserExp2(dataPath);
		System.out.println("total " + allRecordList.size()); 
		GlobalClass.buildRecorderHashMap();
 
		List<TimeRecord> timeList = new ArrayList<>(); 
		 
		for (int i = no > 0 ? no:1 ; i <= allRecordList.size(); ++i) {
			Exp2Record exp2Record = allRecordList.get(i - 1);
			MyLog.add("no:" + i);
			System.out.println("refactoring " + i + " " + exp2Record.getPath() + "," + exp2Record.getOldName() + ","
					+ exp2Record.getOffset());
			start = System.currentTimeMillis();
			MyExtractVariableRefactoring extractVariableRefactoring = new MyExtractVariableRefactoring(
					GlobalClass.JavaProject, exp2Record, "eclipse");
			end = System.currentTimeMillis();
			AfterWorkHandler afterHandler = new AfterWorkHandler(GlobalClass.JavaProject, exp2Record, "eclipse");
			if (extractVariableRefactoring.flag == true) {
				long eclipseTime=end - start ;
				start = System.currentTimeMillis();
				end = -1;
				MyExtractVariableRefactoring oursExtractVariableRefactoring = new MyExtractVariableRefactoring(
						GlobalClass.JavaProject, exp2Record, "ours");
				long oursTime= end == -1 ? System.currentTimeMillis() -start :end - start ;
				timeList.add(new TimeRecord(i,eclipseTime,oursTime));
				afterHandler = new AfterWorkHandler(GlobalClass.JavaProject, exp2Record, "ours");
			}
			Thread.sleep(10);
			try {
				IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(GlobalClass.JavaProject.getPath().append(exp2Record.getPath()));
				iFile.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(10); 
			if (no > 0 ) 
				break;
		}
		if(timeList.size()>1)
		createTimeFile(timeList,Constants.EXP2_RUNTIME_PATH+GlobalClass.JavaProject.getElementName()+".csv");
 
	}

	/**
	 * @param projectName
	 * @return
	 */
	public static IJavaProject findJavaProject(String projectName) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; ++i) {
			if (JavaCore.create(projects[i]).getPath().lastSegment().contains(projectName)) {
				return JavaCore.create(projects[i]);
			}
		}
		return null;
	}

	/**
	 * @param projectName
	 * @param no
	 * @param recordList
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JavaModelException
	 */
	public void exp1(String projectName, int no, List<Exp1Record> recordList)
			throws InterruptedException, JavaModelException, IOException {
		List<TimeRecord> timeList = new ArrayList<>(); 
		if (no <= 0) {
			for (int i = 1; i <= recordList.size(); ++i) {
				Exp1Record exp1Record = recordList.get(i - 1);
				MyLog.add("no:" + i);
				System.out.println("no:" + i);
				PreWorkHandler preWorkhandler = new PreWorkHandler(GlobalClass.JavaProject, exp1Record);
				GlobalClass.JavaProject = findJavaProject(projectName);
				Thread.sleep(1);
				try {
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.sleep(1);
				end=0;
				start=0;
				MyExtractVariableRefactoring myExtractVariableRefactoring = new MyExtractVariableRefactoring(
						GlobalClass.JavaProject, exp1Record, "eclipse");
				long eclipseTime =end-start;
				AfterWorkHandler afterHandler = new AfterWorkHandler(GlobalClass.JavaProject, exp1Record, "eclipse");
				if (myExtractVariableRefactoring.flag == true) { 
					preWorkhandler = new PreWorkHandler(GlobalClass.JavaProject, exp1Record);
					Thread.sleep(1);
					try {
						ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Thread.sleep(1);

					GlobalClass.JavaProject = findJavaProject(projectName);
					GlobalClass.buildRecorderHashMap();
					end=0;
					start=0;
					myExtractVariableRefactoring = new MyExtractVariableRefactoring(GlobalClass.JavaProject, exp1Record,
							"ours");
					long oursTime=end-start;
					timeList.add(new TimeRecord(i,eclipseTime,oursTime));
					afterHandler = new AfterWorkHandler(GlobalClass.JavaProject, exp1Record, "ours");
				}

			}
			MyLog.add("total: " + recordList.size() + "," + EvaluationMetaData.metadatalist.size());
			EvaluationMetaData.writeToCsv(Constants.EXP1_EVALUATE_PATH + projectName + ".csv");
			EvaluationMetaData.metadatalist.clear();
		} else {
			Exp1Record exp1Record = recordList.get(no - 1);
			MyLog.add("no:" + no); 
			System.out.println("no:" + no);
			PreWorkHandler preWorkhandler = new PreWorkHandler(GlobalClass.JavaProject, exp1Record);
			GlobalClass.buildRecorderHashMap();
			Thread.sleep(1);
			try {
				ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(1);
			end=0;
			start=0;
			MyExtractVariableRefactoring myExtractVariableRefactoring = new MyExtractVariableRefactoring(
					GlobalClass.JavaProject, exp1Record, "eclipse");
			long eclipseTime =end-start;
			AfterWorkHandler afterHandler = new AfterWorkHandler(GlobalClass.JavaProject, exp1Record, "eclipse");
			
			if (myExtractVariableRefactoring.flag == true) {
				preWorkhandler = new PreWorkHandler(GlobalClass.JavaProject, exp1Record);
				Thread.sleep(1);
				try {
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Thread.sleep(1);

				GlobalClass.JavaProject = findJavaProject(projectName);
				end=0;
				start=0;
				myExtractVariableRefactoring = new MyExtractVariableRefactoring(GlobalClass.JavaProject, exp1Record,
						"ours");
				afterHandler = new AfterWorkHandler(GlobalClass.JavaProject, exp1Record, "ours");
				long oursTime=end-start;
				timeList.add(new TimeRecord(no,eclipseTime,oursTime));
			}
			MyLog.add("total: " + recordList.size() + "," + EvaluationMetaData.metadatalist.size());
			for (String s : EvaluationMetaData.metadatalist) {
				MyLog.add(s);
			}
			EvaluationMetaData.metadatalist.clear();
		}

		if(timeList.size()>1)
		createTimeFile(timeList,Constants.EXP1_RUNTIME_PATH+GlobalClass.JavaProject.getElementName()+".csv");
		else if(timeList.size()==1)
			System.out.println(timeList.get(0).getId()+", "+timeList.get(0).getEclipseTime()+", "+timeList.get(0).getOursTime());

	}

	public boolean isValid(CompilationUnit cu, String srcPath) { 
		if (cu.getJavaElement() != null
				&& cu.getJavaElement().getPath().removeFirstSegments(1).toString().startsWith(srcPath)) {
			return true;
		}
		return false;
	}

	public void storeExp2Data(String projectName, String srcPath, String outputPath)
			throws JavaModelException, FileNotFoundException, UnsupportedEncodingException {
		GlobalClass.JavaProject = findJavaProject(projectName);
		if (GlobalClass.JavaProject == null) {
			System.out.println("cannot find project!");
			return;
		} else {
			System.out.println("get project " + GlobalClass.JavaProject.getElementName());
		}
		int totalMethod = 0;
		IPackageFragment[] packages = GlobalClass.JavaProject.getPackageFragments();
		List<Exp2Record> allRecordList = new ArrayList<Exp2Record>();
		 
		for (int m = 0; packages != null && m < packages.length; ++m) {
			IPackageFragment aPackage = packages[m];
			if (aPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				for (int k = 0; k < aPackage.getCompilationUnits().length; ++k) {
					LightASTParser parser = new LightASTParser(aPackage.getCompilationUnits()[k]);
					CompilationUnit compilationUnit = Utils.getCompilationUnit(aPackage.getCompilationUnits()[k]);
					if (compilationUnit == null || !isValid(compilationUnit, srcPath)) {
						continue;
					}
					PotentialTargetVisitor ptv = new PotentialTargetVisitor(compilationUnit,
							aPackage.getCompilationUnits()[k]);
					parser.getCompilationUnit().accept(ptv);
					for (int i = 1; i <= ptv.getPotentialTargetList().size(); ++i) {//
						PotentialTargetVisitor.Record v = ptv.getPotentialTargetList().get(i - 1);
						++totalMethod;
						int nextInt = random.nextInt(v.getExpLocList().size());
						int offset = v.getExpLocList().get(nextInt).getOffset();
						int length =  v.getExpLocList().get(nextInt).getLength();
						Exp2Record exp2Record = new Exp2Record(projectName, totalMethod, v.getName(),
								"var_" + totalMethod,
								compilationUnit.getJavaElement().getPath().removeFirstSegments(1).toString(),
								offset, length);
						allRecordList.add(exp2Record);
					}
				}
			}
		}
		allRecordList.forEach(v -> new HandleDataSetJsonFile(v));
		HandleDataSetJsonFile.createJsonFile(outputPath);
		HandleDataSetJsonFile.clear();
		return;
	} 
	
	public static boolean createTimeFile(List<TimeRecord> list,String outputPath)
			throws FileNotFoundException, UnsupportedEncodingException {
		String filePath = outputPath;
		StringBuffer sb = new StringBuffer("no,eclipse,ours\n");
		for(TimeRecord r:list) {
			sb.append(r.getId()+","+r.getEclipseTime()+","+r.getOursTime()+"\n");
		}
		boolean flag = true;
		try {
			File file = new File(filePath);
			if (!file.getParentFile().exists()) { 
				file.getParentFile().mkdirs();
			}
			if (file.exists()) { 
				file.delete();
			}
			file.createNewFile();
			Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			write.write(sb.toString());
			write.flush();
			write.close();
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}
	public static Random random = new Random();
	public static int r ;
	public static long start, end; 
}
