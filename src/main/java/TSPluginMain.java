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
				new File(tempFolder, "axios.git"),
				"https://github.com/refdiff-study/axios.git");

			CstDiff diffForCommit = refDiffJs.computeDiffForCommit(repo, "d23f9d5d4782e5849362895f8b648ed587999706");
			printRefactorings("Refactorings found in axios d23f9d5d4782e5849362895f8b648ed587999706", diffForCommit);
		}
    }

    private static void printRefactorings(String headLine, CstDiff diff) {
        System.out.println(headLine);
        for (Relationship rel : diff.getRefactoringRelationships()) {
            System.out.println(rel.getStandardDescription());
        }
    }
}
