package nextflow.tencentcloud.omics.executor

import groovy.transform.CompileStatic
import nextflow.executor.ScriptFileCopyStrategy
import nextflow.processor.TaskProcessor
import nextflow.util.Escape

import java.nio.file.Path

@CompileStatic
class TencentCloudOmicsFileCopyStrategy implements ScriptFileCopyStrategy {

    private Path remoteBinDir
    private Path scratch

    TencentCloudOmicsFileCopyStrategy(Path remoteBinDir, Path scratch) {
        this.remoteBinDir = remoteBinDir
        this.scratch = scratch
    }

    @Override
    String getBeforeStartScript() {
        return null
    }

    @Override
    String getStageInputFilesScript(Map<String, Path> inputFiles) {
        return null
    }

    @Override
    String getUnstageOutputFilesScript(List<String> outputFiles, Path targetDir) {
        return null
    }

    @Override
    String touchFile(Path file) {
        return ''
    }

    @Override
    String fileStr(Path file) {
        Escape.path(file.getFileName())
    }

    @Override
    String copyFile(String name, Path target) {
        return 'true'
    }

    @Override
    String exitFile(Path file) {
        "-> ${Escape.path(file.getName())}"
    }

    @Override
    String pipeInputFile(Path file) {
        " <- ${Escape.path(file.getName())}"
    }

    @Override
    String getEnvScript(Map env, boolean container) {
        if (container)
            throw new UnsupportedOperationException("Parameter `container` not supported by ${this.class.simpleName}")

        final result = new StringBuilder()
        final copy = env ? new HashMap<String, String>(env) : Collections.<String, String> emptyMap()
        final path = copy.containsKey('PATH')
        if (path)
            copy.remove('PATH')
        if (remoteBinDir) {
            result << "chmod +x \$PWD/bin/* || true\n"
            result << "export PATH=\$PWD/bin:\$PATH\n"
        }
        if (scratch) {
            result << "NXF_SCRATCH=$scratch\n"
        }
        // finally render the environment
        final envSnippet = TaskProcessor.bashEnvironmentScript(copy, false)
        if (envSnippet)
            result << envSnippet
        return result.toString()
    }
}
