package org.javamodularity.moduleplugin.internal;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.javamodularity.moduleplugin.extensions.PatchModuleExtension;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PatchModuleResolver {

    private static final Logger LOGGER = Logging.getLogger(PatchModuleResolver.class);

    private final PatchModuleExtension patchModuleExtension;
    /**
     * Takes a JAR name and resolves it to a full JAR path. If returned JAR patch is empty, the JAR is skipped.
     */
    private final UnaryOperator<String> jarNameResolver;

    public PatchModuleResolver(PatchModuleExtension patchModuleExtension, UnaryOperator<String> jarNameResolver) {
        this.patchModuleExtension = patchModuleExtension;
        this.jarNameResolver = jarNameResolver;
    }

    public void mutateArgs(List<String> args) {
        buildOptionStream().forEach(option -> option.mutateArgs(args));
    }

    public Stream<TaskOption> buildOptionStream() {
        return patchModuleExtension.getConfig().stream()
                .map(patch -> patch.split("="))
                .map(this::resolvePatchModuleValue)
                .filter(Objects::nonNull)
                .map(value -> new TaskOption("--patch-module", value));
    }

    private String resolvePatchModuleValue(String[] parts) {
        String moduleName = parts[0];
        String[] jarNames = parts[1].split("[,;:]");

        LOGGER.info("Attempting to patch {} into {}", Arrays.asList(jarNames), moduleName);
        String jarPaths = Arrays.stream(jarNames)
                .map(jarName -> {
                    String jarPath = jarNameResolver.apply(jarName);
                    if (jarPath.isEmpty()) {
                        LOGGER.warn("Skipped patching {} into {}", jarName, moduleName);
                        return null;
                    }
                    return jarPath;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(File.pathSeparator));

        if(jarPaths.isEmpty()) return null;
        String patchModuleOption = moduleName + "=" + jarPaths;
        LOGGER.info("Adding compiler option: --patch-module={}", jarPaths);
        return patchModuleOption;
    }
}
