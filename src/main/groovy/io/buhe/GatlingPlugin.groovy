package io.buhe

import com.excilys.ebi.gatling.app.Gatling
import com.github.mperry.fg.ListFJExtension
import com.github.mperry.fg.ListJavaExtension
import fj.F
import fj.P2
import fj.Unit
import fj.data.IO
import fj.data.IOFunctions
import fj.data.Option
import fj.data.Validation
import fj.data.vector.V
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.slf4j.Logger

//import org.gradle.api.logging.Logger

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

@TypeChecked
class GatlingPlugin implements Plugin<Project> {

    static enum ListStatus { WHITE, BLACK, NEITHER, BOTH }
    static String DEFAULT_PATTERN = ".*"

    static Pattern createPattern(Option<String> o) {
        Pattern.compile(o.orSome(DEFAULT_PATTERN))
    }

    static ListStatus status(String clazz, Pattern blackListPattern, Pattern whiteListPattern) {
        def w = inList(clazz, whiteListPattern)
        def b = inList(clazz, blackListPattern)
        (w && b) ? ListStatus.BOTH : w ? ListStatus.WHITE : b ? ListStatus.BLACK : ListStatus.NEITHER
    }

    static boolean check(String clazz, Pattern blackListPattern, Pattern whiteListPattern) {
        def s = status(clazz, blackListPattern, whiteListPattern)

        doTest(s)
    }

    static boolean doTest(ListStatus status) {
        status != ListStatus.BLACK
    }

    static boolean inList(String path, Pattern listPattern) {
        listPattern && listPattern.matcher(path).matches()
    }

    static Logger logger(Project project) {
        project.logger
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static File testClassesDir(Project project) {
        project.sourceSets.test.output.classesDir
    }

    static IO<List<Path>> listRecursively(Path dir){
        def f = { ->
            def stream = null
            try {
                def result = []
                stream = Files.newDirectoryStream(dir)
                for (Path path : stream) {
                    if (path.toFile().isDirectory()) {
                        result.addAll(listRecursively(path).run())
                    } else {
                        result.add(path.toAbsolutePath())
                    }
                }
                result
            } finally {
                stream?.close()
            }
        }
        f as IO
    }

    static String manglePath(File file, File root) {
        (file.getPath() - (root.getPath() + File.separator) - '.class').replace(File.separator, '.')
    }

    static <A> IO<List<A>> sequence(List<IO<A>> list) {
        IOFunctions.map(IOFunctions.sequence(ListJavaExtension.toFJList(list)), { fj.data.List<A> list2 -> ListFJExtension.toJavaList(list2) })
    }

    static void apply(GatlingPluginExtension g, Logger logger, String testClassesDir, F<String, IO<Unit>> perform) {
//            logger.info("Gatling configuration: $g, testClassesDir: $testClassesDir, report: $reportPath")
            logger.info("Gatling configuration: $g, testClassesDir: $testClassesDir")

//            logger.info("Include ${g.include}")
//            logger.info("Exclude " + g.exclude)

            def v = V.v(g.exclude, g.include).map { String s -> createPattern(Option.fromNull(s)) }
            def cDir = new File(testClassesDir)
            def found = cDir.exists() && cDir.isDirectory()
            if (!found) {
                logger.info("Gatling test directory not found: ${cDir}")
            } else {
//                logger.info("Executing all Gatling scenarios from: $cDir")

                def io1 = IOFunctions.map(listRecursively(Paths.get(cDir.absolutePath)), { List<Path> list ->
                    def list2 = list.collect { Path p ->
                        manglePath(p.toFile(), cDir)
                    }
                    def list3 = list2.collect { String s ->
                        status(s, v._1(), v._2())
                    }
                    def zip = ListJavaExtension.zip(list2, list3)
//                    def zip = list2.zip(list3)
                    def listIo = zip.collect { P2<String, ListStatus> p ->
                        def io = { ->
                            if (g.list) {
                                logger.info("Class ${p._1()} status: ${p._2()} doTest: ${doTest(p._2())}")
                            }
                            if (doTest(p._2()) && !g.dryRun) {
                                perform.f(p._1()).run()
//                                run(p._1(), reportPath, testClassesDir)
//                                runChild()
                            }
                            Unit.unit()
                        } as IO<Unit>
                        io
                    }
                    sequence(listIo)
                })
                def io2 = IOFunctions.join(io1)
                io2.run()

                logger.info("Finished Gatling scenarios.")
            }


    }

    void apply(Project project) {
        def g = (GatlingPluginExtension) project.extensions.create("gatling", GatlingPluginExtension);
        def logger = logger(project)

        project.task('gatlingRun').dependsOn("compileTestScala") << {
            apply(g, logger, testClassesDir(project).absolutePath, { String clazz ->
                { ->
                    runChild(project, clazz, classpath(project), testClassesDir(project).absolutePath, reportPath(project))
                } as IO<Unit>

            })

        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static FileCollection classpath(Project project) {
        project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static void runChild(Project project, String gatlingScenarioClass, FileCollection javaClasspath, String testClassesDirectory, String reportPath) {
        project.javaexec {
            // I do not use this so
            main = 'com.excilys.ebi.gatling.app.Gatling'
            classpath = javaClasspath
            args '-sbf', testClassesDirectory, '-s', gatlingScenarioClass, '-rf', reportPath
//            args '-sbf', testClassesDir(project), '-s', gatlingScenarioClass, '-rf', reportPath
        }
    }

    static String reportPath(Project project) {
        project.buildDir.getAbsolutePath() + "/reports/gatling"
    }

    static void run(Project p, String clazz) {
        run(clazz, reportPath(p), testClassesDir(p).absolutePath)
    }

    static void run(String gatlingScenarioClass, String reportPath, String testClassesDir) {

        Gatling.main(['-sbf', testClassesDir, '-s', gatlingScenarioClass, '-rf', reportPath] as String[])
//        Gatling.main(['-s', gatlingScenarioClass, '-rf', reportPath] as String[])
    }

}

