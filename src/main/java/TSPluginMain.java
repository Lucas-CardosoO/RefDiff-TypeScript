import com.sun.org.apache.bcel.internal.generic.ExceptionThrower;
import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.ts.TsPlugin;

import java.io.File;
public class TSPluginMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Started Main");
        runExamples();
    }

    private static void runExamples() throws Exception {

        // This is a temp folder to clone or checkout git repositories.
		File tempFolder = new File("temp");

		// Creates a RefDiff instance configured with the JavaScript plugin.
		try (TsPlugin tsPlugin = new TsPlugin()) {
			RefDiff refDiffJs = new RefDiff(tsPlugin);

			// Clone the angular.js GitHub repo.
            File repo = refDiffJs.cloneGitRepository(
                    new File(tempFolder, "ta.git"),
                    "https://github.com/pauloborba/teachingassistant.git");
            refDiffJs.computeDiffForCommitHistory(repo, 15, (commit, diff) -> {
                printRefactorings("Refactorings found in teachingAssistant  " + commit.getId().name(), diff);
            });
			//CstDiff diffForCommit = refDiffJs.computeDiffForCommit(repo, "b8340969981ac00067033fe514776a079a6df14b");
			//printRefactorings("Refactorings found in axios 87bf57b52907db3eb2cd88d36f5dad9175000069", diffForCommit);
		}
		tempFolder.delete();
    }

    private static void printRefactorings(String headLine, CstDiff diff) {
        System.out.println(headLine);
        for (Relationship rel : diff.getRefactoringRelationships()) {
            System.out.println(rel.getStandardDescription());
        }
    }
}
