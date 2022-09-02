package safeextractor.handlers;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import safeextractor.json.Exp1Record;
import safeextractor.log.MyLog;
import safeextractor.utils.Constants; 

public class PreWorkHandler {
 
	public PreWorkHandler(IJavaProject javaProject,Exp1Record exp1Record) {
        String projectPath=Constants.EXP1_PROJECT_ROOT+javaProject.getPath().toString();
         
        Git git=null;
        try {
        	git=Git.open(new File(projectPath));
		} catch (IOException e) {
			e.printStackTrace();
		} 
        Repository repository = git.getRepository();
        MyLog.add("commit id:"+exp1Record.getCommitId());
        try {
        	git.checkout().setForced(true).setName(exp1Record.getCommitId()).call();
		} catch (CheckoutConflictException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
        try {
        	Iterable<RevCommit> iterable = git.log().call(); 
			Iterator<RevCommit> iterator = iterable.iterator();
			MyLog.add("switch to "+iterator.next().getName()+" !");
		} catch (NoWorkTreeException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		} 
        repository.close();
        git.close();
	}
	
}
