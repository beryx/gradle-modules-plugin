package org.javamodularity.moduleplugin.tasks;

import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.List;

class CompileJavaTaskMutator {

    static void mutateJavaCompileTask(Project project, JavaCompile compileJava) {
        ModuleOptions moduleOptions = compileJava.getExtensions().getByType(ModuleOptions.class);
        PatchModuleExtension patchModuleExtension = project.getExtensions().getByType(PatchModuleExtension.class);

        var compilerArgs = new ArrayList<>(compileJava.getOptions().getCompilerArgs());

        compilerArgs.addAll(List.of("--module-path", compileJava.getClasspath()
                .filter(patchModuleExtension::isUnpatched)
                .getAsPath()));

        if (!moduleOptions.getAddModules().isEmpty()) {
            String addModules = String.join(",", moduleOptions.getAddModules());
            compilerArgs.add("--add-modules");
            compilerArgs.add(addModules);
        }

        compilerArgs.addAll(patchModuleExtension.configure(compileJava.getClasspath()));
        compileJava.getOptions().setCompilerArgs(compilerArgs);
        compileJava.setClasspath(project.files());

        AbstractCompile compileKotlin = (AbstractCompile) project.getTasks().findByName("compileKotlin");
        if (compileKotlin != null) {
            compileJava.setDestinationDir(compileKotlin.getDestinationDir());
        }

        GroovyCompile compileGroovy = (GroovyCompile)project.getTasks().findByName("compileGroovy");
        if (compileGroovy != null) {
            JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
            for(String sourceSetName: List.of(SourceSet.MAIN_SOURCE_SET_NAME, SourceSet.TEST_SOURCE_SET_NAME)) {
                SourceSet sourceSet = javaConvention.getSourceSets().getByName(sourceSetName);
                sourceSet.getJava().include("module-info.java");
                compileJava.setDestinationDir(compileGroovy.getDestinationDir());

                GroovySourceSet groovySourceSet = (GroovySourceSet)new DslObject(sourceSet).getConvention().getPlugins().get("groovy");
                groovySourceSet.getGroovy().setSrcDirs(List.of("src/" + sourceSetName + "/java", "src/" + sourceSetName + "/groovy"));
                groovySourceSet.getGroovy().exclude("**/module-info.java");
            }

            compileGroovy.getOptions().setCompilerArgs(compilerArgs);

            compileGroovy.getDependsOn().remove(JavaPlugin.COMPILE_JAVA_TASK_NAME);
            compileJava.mustRunAfter(compileGroovy);
        }
    }

}
