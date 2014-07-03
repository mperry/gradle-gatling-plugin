package io.buhe

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Pattern

class GatlingPlugin implements Plugin<Project> {
    private static Pattern createPattern(array) {
        if (!array) return null
        def list = Arrays.asList(array)
        if (list == null || list.size() == 0)
            return null;
        StringBuilder sb = new StringBuilder(500);
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1)
                sb.append("|");
        }
        return Pattern.compile(sb.toString());
    }

    private static boolean check(String clazz, Pattern blackListPattern, Pattern whiteListPattern) {
        if (inList(clazz, whiteListPattern)) {
            println "---- $clazz in white list. ----"
            return true
        } else {
            if (inList(clazz, blackListPattern)) {
                println "---- $clazz in black list. ----"
                return false
            } else {
                println "---- $clazz not in black and white list. ----"
                return true
            }
        }
    }

    private static boolean inList(String path, Pattern listPattern) {
        listPattern && listPattern.matcher(path).matches()
    }


    void apply(Project project) {
        project.extensions.create("gatling", GatlingPluginExtension);

        project.task('gatling').dependsOn("compileTestScala") << {
            println 'Include ' + project.gatling.include
            println 'Exclude ' + project.gatling.exclude

            def whiteListPattern = createPattern(project?.gatling?.include);
            def blackListPattern = createPattern(project?.gatling?.exclude)

            println 'Include ' + whiteListPattern
            println 'Exclude ' + blackListPattern

            def testDirectory = project.sourceSets.test.output.classesDir
	    if (testDirectory.exists() && testDirectory.isDirectory()) {
                logger.lifecycle(" ---- Executing all Gatling scenarios from: ${project.sourceSets.test.output.classesDir} ----")
                testDirectory.eachFileRecurse { file ->
                    if (file.exists() && file.isFile()) {
                        //Remove the full path, .class and replace / with a .
                        logger.debug("Tranformed file ${file} into")
                        def gatlingScenarioClass = (file.getPath() - (project.sourceSets.test.output.classesDir.getPath() + File.separator) - '.class')
                                .replace(File.separator, '.')
                        if(check(gatlingScenarioClass,blackListPattern,whiteListPattern)){
                            logger.debug("Tranformed file ${file} into scenario class ${gatlingScenarioClass}")
                            project.javaexec {
                                // I do not use this so
                                main = 'com.excilys.ebi.gatling.app.Gatling'
                                classpath = project.sourceSets.test.output + project.sourceSets.test.runtimeClasspath
                                report = project.buildDir.getAbsolutePath()+'/reports/gatling';
                                args  '-sbf',
                                        project.sourceSets.test.output.classesDir,
                                        '-s',
                                        gatlingScenarioClass,
                                        '-rf',
                                        report
                            }
                        }
                    }
                }
                logger.lifecycle(" ---- Done executing all Gatling scenarios ----")
            } else {
                logger.lifecycle(" ---- Gatling test directory not found: ${testDirectory} ----")
            }
        }
    }
}

