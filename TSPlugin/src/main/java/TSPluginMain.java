import refdiff.core.RefDiff;
import refdiff.core.diff.CstDiff;
import refdiff.core.diff.Relationship;
import refdiff.parsers.js.JsPlugin;

import java.io.File;


public class TSPluginMain {
    public static void main() throws Exception {
        runExamples();
    }

    private static void runExamples() throws Exception {
        // This is a temp folder to clone or checkout git repositories.
        File tempFolder = new File("temp");

        // Creates a RefDiff instance configured with the JavaScript plugin.
        JsPlugin jsPlugin = new JsPlugin();
        RefDiff refDiffJs = new RefDiff(jsPlugin);

        // Clone the angular.js GitHub repo.
        File angularJsRepo = refDiffJs.cloneGitRepository(
                new File(tempFolder, "angular.js"),
                "https://github.com/refdiff-study/angular.js.git");

        // You can compute the relationships between the code elements in a commit with
        // its previous commit. The result of this operation is a CstDiff object, which
        // contains all relationships between CstNodes. Relationships whose type is different
        // from RelationshipType.SAME are refactorings.
        CstDiff diffForCommit = refDiffJs.computeDiffForCommit(angularJsRepo, "2636105");
        printRefactorings("Refactorings found in angular.js 2636105", diffForCommit);

        // You can also mine refactoring from the commit history. In this example we navigate
        // the commit graph backwards up to 5 commits. Merge commits are skipped.
        refDiffJs.computeDiffForCommitHistory(angularJsRepo, 5, (commit, diff) -> {
            printRefactorings("Refactorings found in angular.js " + commit.getId().name(), diff);
        });

        // The JsPlugin initializes JavaScript runtime to run the Babel parser. We should close it shut down.
        jsPlugin.close();
    }

    private static void printRefactorings(String headLine, CstDiff diff) {
        System.out.println(headLine);
        for (Relationship rel : diff.getRefactoringRelationships()) {
            System.out.println(rel.getStandardDescription());
        }
    }
}
