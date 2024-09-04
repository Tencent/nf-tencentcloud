package nextflow.tencentcloud.omics.executor

import groovy.transform.CompileStatic
import nextflow.executor.BashWrapperBuilder
import nextflow.processor.TaskBean
import nextflow.processor.TaskRun

import java.nio.file.Path

/**
 * Bash builder adapter to manage specific tasks
 *
 */
@CompileStatic
class TencentCloudOmicsBashBuilder extends BashWrapperBuilder {

    TencentCloudOmicsBashBuilder(TaskRun task, Path remoteBinDir, Path scratch) {
        super(new TaskBean(task), new TencentCloudOmicsFileCopyStrategy(remoteBinDir, scratch))
        cleanup = false
    }

}
