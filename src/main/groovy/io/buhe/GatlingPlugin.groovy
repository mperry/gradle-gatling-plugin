package io.buhe

import com.excilys.ebi.gatling.app.Gatling
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger

import java.util.regex.Pattern

@TypeChecked
class GatlingPlugin implements Plugin<Project> {

    static enum ListStatus { WHITE, BLACK, NEITHER, BOTH }

    static String DEFAULT_PATTERN = ".*"

    private static Pattern createPattern(String array) {
        Pattern.compile(array ?: DEFAULT_PATTERN)
    }

    static ListStatus status(String clazz, Pattern blackListPattern, Pattern whiteListPattern) {
        def w = inList(clazz, whiteListPattern)
        def b = inList(clazz, blackListPattern)
        (w && b) ? ListStatus.BOTH : w ? ListStatus.WHITE : b ? ListStatus.BLACK : ListStatus.NEITHER
    }

    static boolean check(String clazz, Pattern blackListPattern, Pattern whiteListPattern) {
        def s = status(clazz, blackListPattern, whiteListPattern)
        println("$clazz list status: $s")
        s != ListStatus.BLACK
    }

    static boolean inList(String path, Pattern listPattern) {
        listPattern && listPattern.matcher(path).matches()
    }

    Logger logger(Project project) {
        project.logger
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    File testClassesDir(Project project) {
        project.sourceSets.test.output.classesDir
    }

//    @TypeChecked(TypeCheckingMode.SKIP)
    void apply(Project project) {
        def g = (GatlingPluginExtension) project.extensions.create("gatling", GatlingPluginExtension);
//        def g = project.extensions.create("gatling", GatlingPluginExtension);
        def logger = logger(project)
        logger.info("GatlingPlugin hello world 1")

        project.task('gatling').dependsOn("compileTestScala") << {
            logger.info("Include ${g.include}")
            logger.info("Exclude " + g.exclude)

            def whiteListPattern = createPattern(g?.include);
            def blackListPattern = createPattern(g?.exclude)

//            println 'Include ' + whiteListPattern
//            println 'Exclude ' + blackListPattern

//            def ss = project.sourceSets
//            def t = project.sourceSets.test
//            def o = project.sourceSets.test.output
//            def testCdir = o.testClassesDir
//            logger.info("sourceSets: ${ss.class} $ss...${t.class} $t...${o.class} $o...${testCdir.class} $testCdir ")

//            def testDirectory = project.sourceSets.test.output.testClassesDir
            def cDir = testClassesDir(project)
            if (cDir.exists() && cDir.isDirectory()) {
                logger.lifecycle("Executing all Gatling scenarios from: $cDir")
                cDir.eachFileRecurse { File file ->
                    if (file.exists() && file.isFile()) {
                        //Remove the full path, .class and replace / with a .
                        def gatlingScenarioClass = (file.getPath() - (cDir.getPath() + File.separator) - '.class')
                                .replace(File.separator, '.')
                        if (check(gatlingScenarioClass, blackListPattern, whiteListPattern)) {
                            logger.info("Tranformed file ${file} into scenario class ${gatlingScenarioClass}")
                            if (!g.dryRun) {
                                runChild(project, gatlingScenarioClass, classpath(project), project.buildDir.getAbsolutePath() + '/reports/gatling')
//                                run(project, gatlingScenarioClass, classpath(project), project.buildDir.getAbsolutePath() + '/reports/gatling')


                            }
                        }
                    }
                }
                logger.lifecycle("Done executing all Gatling scenarios")
            } else {
                logger.lifecycle("Gatling test directory not found: ${cDir}")
            }
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    FileCollection classpath(Project project) {
        project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
    }


    @TypeChecked(TypeCheckingMode.SKIP)
    void runChild(Project project, String gatlingScenarioClass, FileCollection javaClasspath, String reportPath) {
        project.javaexec {
            // I do not use this so
            main = 'com.excilys.ebi.gatling.app.Gatling'
            classpath = javaClasspath
            args '-sbf', testClassesDir(project), '-s', gatlingScenarioClass, '-rf', reportPath
//                    project.sourceSets.test.output.testClassesDir,

        }

    }


    void run(Project project, String gatlingScenarioClass, FileCollection javaClasspath, String reportPath) {

        Gatling.main(['-sbf', testClassesDir(project), '-s', gatlingScenarioClass, '-rf', reportPath] as String[])
    }

}

