import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.js.JsPlugin;
import refdiff.parsers.ts.TsPlugin;

import java.io.File;
public class TSPluginMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Started Main");
        runExamples();
    }

    private static void runExamples() throws Exception {
        // This is a temp folder to clone or checkout git repositories.
//        File tempFolder = new File("temp");
//
//        // Creates a RefDiff instance configured with the JavaScript plugin.
//        JsPlugin jsPlugin = new JsPlugin();
//        RefDiff refDiffJs = new RefDiff(jsPlugin);

//        // Clone the angular.js GitHub repo.
//        File angularJsRepo = refDiffJs.cloneGitRepository(
//                new File(tempFolder, "angular.js"),
//                "https://github.com/refdiff-study/angular.js.git");
//
//        // You can compute the relationships between the code elements in a commit with
//         // contains all relationships between CstNodes. Relationships whose type is different
//        // from RelationshipType.SAME are refactorings.
//        CstDiff diffForCommit = refDiffJs.computeDiffForCommit(angularJsRepo, "6c224a2a6059d4a089ef396c880c4a6369f0be59");
//        printRefactorings("Refactorings found in angular.js 2636105", diffForCommit);
//
//        // You can also mine refactoring from the commit history. In this example we navigate
//        // the commit graph backwards up to 5 commits. Merge commits are skipped.
//        refDiffJs.computeDiffForCommitHistory(angularJsRepo, 5, (commit, diff) -> {
//            printRefactorings("Refactorings found in angular.js " + commit.getId().name(), diff);
//        });

        // This is a temp folder to clone or checkout git repositories.
		File tempFolder = new File("temp");

		// Creates a RefDiff instance configured with the JavaScript plugin.
		try (TsPlugin jsPlugin = new TsPlugin()) {
			RefDiff refDiffJs = new RefDiff(jsPlugin);

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
