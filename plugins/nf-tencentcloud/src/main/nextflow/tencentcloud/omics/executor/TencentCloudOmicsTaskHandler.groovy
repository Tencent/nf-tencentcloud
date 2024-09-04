package nextflow.tencentcloud.omics.executor

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.executor.BashWrapperBuilder
import nextflow.processor.TaskConfig
import nextflow.processor.TaskHandler
import nextflow.processor.TaskRun
import nextflow.processor.TaskStatus
import nextflow.tencentcloud.nio.CosPath
import nextflow.tencentcloud.omics.client.api.TaskServiceApi
import nextflow.tencentcloud.omics.client.model.*
import nextflow.tencentcloud.omics.client.model.TesExecutor as TesExecutorModel
import nextflow.trace.TraceRecord
import nextflow.util.MemoryUnit

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths

import static nextflow.processor.TaskStatus.COMPLETED
import static nextflow.processor.TaskStatus.RUNNING

/**
 * Handle execution phases for a task executed by a TencentCloud Omics executor
 *
 */
@Slf4j
@CompileStatic
class TencentCloudOmicsTaskHandler extends TaskHandler {

    private final String WORK_DIR

    final List<TesState> COMPLETE_STATUSES = [TesState.COMPLETE, TesState.EXECUTOR_ERROR, TesState.SYSTEM_ERROR, TesState.CANCELED]

    final List<TesState> STARTED_STATUSES = [TesState.INITIALIZING, TesState.RUNNING, TesState.PAUSED] + COMPLETE_STATUSES

    final List<TesState> CLEAN_SCRATCH_STATUSES = [TesState.COMPLETE, TesState.CANCELED]

    private TencentCloudOmicsExecutor executor

    private final Path exitFile

    private final Path wrapperFile

    private final Path outputFile

    private final Path errorFile

    private final Path inputFile

    private TaskServiceApi client

    private Path scratch

    private Boolean cleanScratch

    private String requestId

    private Path[] infileList
    private String[] outFileList

    private Boolean failed = false

    /** only for testing purpose -- do not use */
    protected TencentCloudOmicsTaskHandler() {}

    TencentCloudOmicsTaskHandler(TaskRun task, TencentCloudOmicsExecutor executor) {
        super(task)
        this.executor = executor
        this.client = executor.getClient()
        this.infileList = [
                task.workDir.resolve(TaskRun.CMD_SCRIPT),
                task.workDir.resolve(TaskRun.CMD_RUN),
        ]
        this.outFileList = [
                TaskRun.CMD_LOG,
                TaskRun.CMD_INFILE,
                TaskRun.CMD_OUTFILE,
                TaskRun.CMD_ERRFILE,
                TaskRun.CMD_START,
                TaskRun.CMD_EXIT,
                TaskRun.CMD_STAGE,
                TaskRun.CMD_TRACE,
                TaskRun.CMD_ENV,
        ]

        this.inputFile = task.workDir.resolve(TaskRun.CMD_INFILE)
        this.outputFile = task.workDir.resolve(TaskRun.CMD_OUTFILE)
        this.errorFile = task.workDir.resolve(TaskRun.CMD_ERRFILE)
        this.exitFile = task.workDir.resolve(TaskRun.CMD_EXIT)
        this.wrapperFile = task.workDir.resolve(TaskRun.CMD_RUN)

        this.cleanScratch = executor.session.getConfigAttribute("tencent.cleanScratch", true) as Boolean
        this.WORK_DIR = getWorkdir()
    }

    // calculate workdir
    private String getWorkdir() {

        String scratch = task.getScratch()?.toString()

        // workdir remote, need scratch support
        if (task.workDir.getFileSystem() != FileSystems.getDefault()) {
            // must set scratch not false
            if (scratch == 'false') {
                throw new RuntimeException("[TencentCloud] remote workDir not support scratch=false, please don't specific scratch, or set scratch=true, or set to local cfs absolute path")
            }

            // scratch set specific path
            if (scratch != null && !scratch.isEmpty() && scratch != "true") {
                this.scratch = getScratchDir()
                return this.scratch
            }

            // no scratch set, calculate a logic scratch dir
            this.scratch = getScratchDirByWorkDir()
            return this.scratch
        }

        // scratch is true, calculate a logic scratch dir
        // make scratch=true equal to not set scratch
//        if( scratch == 'true' ) {
//            this.scratch = getScratchDirByWorkDir()
//            return this.scratch
//        }

        // no scratch, use workdir as tes workdir
        if (scratch == 'false') {
            return task.workDir.toString()
        }

        // specific dir
        if (scratch != null && !scratch.isEmpty() && scratch != "true") {
            this.scratch = getScratchDir()
            return this.scratch
        }

        // scratch is true, same as scratch set to nothing
        if (scratch == 'true') {
            this.scratch = Paths.get("/work")
            return this.scratch
        }

        // default logic
        return "/work"
    }

    protected Path getScratchDir() {
        Path startPath = Paths.get(System.getProperty("user.dir") as String)
//        String scratchNamePath = "scratch-" + executor.session.getUniqueId()
        String taskWorkdir = executor.workDir.relativize(task.workDir).toString()
        String scratch = task.getScratch()?.toString()

        Path scratchPath = Paths.get(scratch)
        if (scratchPath instanceof CosPath) {
            throw new RuntimeException("[TencentCloud] scratch must specify to a local path: $scratch")
        }
        if (!scratchPath.isAbsolute()) {
            Path newScratchPath = startPath.resolve(scratchPath)
//                .resolve(scratchNamePath)
                    .resolve(taskWorkdir)
            checkInVolume(newScratchPath, "[TencentCloud] scratch path not absolute, you must run nextflow in a volume path, scratch:$scratch, startPath:$startPath, calculated scratch dir:$newScratchPath")
            return newScratchPath
        }
//        checkInVolume(scratchPath, "[TencentCloud] not support set scratch path out of volumes:$scratch")
        Path newScratchPath = scratchPath
//            .resolve(scratchNamePath)
                .resolve(taskWorkdir)
        return newScratchPath
    }

    protected Path getScratchDirByWorkDir() {
        String startPath = System.getProperty("user.dir") as String
        Path newScratchPath = Paths.get(startPath)
                .resolve("scratch-" + executor.session.getUniqueId())
                .resolve(executor.workDir.relativize(task.workDir).toString())
        checkInVolume(newScratchPath, "[TencentCloud] scratch dir not set, you must run nextflow in a volume path, scratch:$scratch, startPath:$startPath")
        return newScratchPath
    }

    protected String getRequestId() { requestId }

    protected static void checkInVolume(Path p, String message) {
        if (!p.toString().startsWith("/vol-")) {
            throw new RuntimeException(message)
        }
    }

    @Override
    boolean checkIfRunning() {

        if (requestId && isSubmitted()) {
            final response = client.getTask(requestId, null)
            final started = response.state in STARTED_STATUSES
            if (started) {
                log.trace "[TencentCloud] Task started > $task.name|$requestId"
                status = RUNNING
                return true
            }
        }

        return false
    }

    @Override
    boolean checkIfCompleted() {
//        log.info "[TencentCloud] Task checkIfCompleted > $task.name|$requestId|$status"
        if (isCompleted()) {
            return true
        }
        if (!isRunning()) {
            return false
        }

        final response = client.getTask(requestId, null)
        if (response.state in COMPLETE_STATUSES) {
            // finalize the task
            log.info "[TencentCloud] Task completed > $task.name|$requestId"
            task.exitStatus = readExitFile()
            task.stdout = outputFile
            task.stderr = errorFile
            status = COMPLETED

            // 清理 scratch 目录
            if (response.state in CLEAN_SCRATCH_STATUSES) {
//                checkDeleteScratch()
            } else {
                failed = true
            }
            return true
//        } else {
//            log.info "[TencentCloud] Task status not completed from tes > $task.name|$requestId|$response.state"
        }

        return false
    }

    private void checkDeleteScratch() {
        if (!cleanScratch) {
            return
        }
        if (!scratch) {
            return
        }
        if (!scratch.toString().startsWith("/vol-")) {
            return
        }
        if (!scratch.exists() || !scratch.isDirectory()) {
            return
        }
        // delete scratch dir
        scratch.deleteDir()
        // check if parent empty, delete
        Path parent = scratch.parent
        if (!parent.listFiles()) {
            parent.deleteDir()
        }
        // check if parent's parent empty, delete
        parent = parent.parent
        if (!parent.listFiles()) {
            parent.deleteDir()
        }
    }

    private int readExitFile() {
        try {
            exitFile.text as Integer
        }
        catch (Exception e) {
            log.trace "[TencentCloud] Cannot read exitstatus for task: `$task.name|$requestId` | ${e.message}"
            return Integer.MAX_VALUE
        }
    }

    @Override
    void kill() {
        if (requestId)
            client.cancelTask(requestId)
        else
            log.trace "[TencentCloud] Invalid kill request -- missing requestId"
    }

    @Override
    void submit() {

        // create task wrapper
        Path remoteBinDir = executor.getRemoteBinDir()
        final bash = newTesBashBuilder(task, remoteBinDir)
        bash.build()

        final body = newTesTask()
//        log.info("[TencentCloud] Task body generated  > $task.name|$task.workDir|$body")

        // submit the task
        final t = client.createTask(body)
        requestId = t.id
        log.info("[TencentCloud] Task submitted > $task.name|$task.workDir|$requestId")
        status = TaskStatus.SUBMITTED
    }

    @Override
    TraceRecord getTraceRecord() {
        def result = super.getTraceRecord()
        if (requestId) {
            result.put('native_id', requestId)
        }
        if (scratch) {
            result.put('scratch', scratch)
        }

        return result
    }

    protected TencentCloudOmicsBashBuilder newTesBashBuilder(TaskRun task, Path remoteBinDir) {
        return new TencentCloudOmicsBashBuilder(task, remoteBinDir, scratch)
    }

    protected TesTask newTesTask() {
        // the cmd list to launch it
        def job = new ArrayList(BashWrapperBuilder.BASH) << wrapperFile.getName()
        List cmd = ['/bin/bash', '-c', job.join(' ') + " &> $TaskRun.CMD_LOG"]

        def exec = new TesExecutorModel()
        exec.command = cmd
        exec.image = task.container
        exec.workdir = WORK_DIR

        def body = new TesTask()

        // add task control files
        infileList.each { Path p ->
            body.addInputsItem(inItem(p))
        }

        // add bin for remote bin dir
        if (executor.getRemoteBinDir()) {
            body.addInputsItem(inItem(executor.getRemoteBinDir(), "bin"))
        }

        // add task input files
        if (inputFile.exists()) body.addInputsItem(inItem(inputFile))

        task.getInputFilesMap()?.each { String name, Path path ->
            body.addInputsItem(inItem(path, name))
        }

        // add the task output files
        outFileList.each { String p ->
            body.addOutputsItem(outItem(p))
        }

        // set requested resources
        body.setResources(getResources(task.config))

        task.outputFilesNames?.each { fileName ->
            body.addOutputsItem(outItem(fileName))
        }

        body.setName(task.getName())

        // add the executor
        body.executors = [exec]

        // add preemptible
        def preemptible = task.config.getResourceLabels()?["preemptible"]
        if (preemptible == "true") {
            body.getResources().setPreemptible(true)
        }

        // add gpu
        body.getResources().putBackendParametersItem("gpuType", task.config.getResourceLabels()?["gpuType"])
        body.getResources().putBackendParametersItem("gpuCount", task.config.getResourceLabels()?["gpuCount"])

        // add run_uuid
        body.putTagsItem("runUuid", System.getenv("RUN_ID"))
        // add WorkingDir，DestDir
        body.putTagsItem("WorkingDir", WORK_DIR)
        body.putTagsItem("DestDir", task.workDir.toUriString())

        return body
    }

    private TesResources getResources(TaskConfig cfg) {
        def res = new TesResources()
        res.cpuCores(cfg.getCpus())
                .ramGb(toGiga(cfg.getMemory()))
                .diskGb(cfg.getDisk()?.toGiga())
        log.trace("[TencentCloud] Adding resource request: $res")
        // @TODO preemptible
        // @TODO zones
        return res
    }

    private Double toGiga(MemoryUnit size) {
        // 1073741824 = 1GB
        return size != null ? ((double) size.bytes) / 1073741824 : null
    }

    private TesInput inPathItem(Path realPath, String fileName = null) {
        def result = new TesInput()
        result.url = realPath.toUriString()
        if (!result.url.endsWith("/")) {
            result.url = result.url + "/"
        }
        result.path = fileName ? "$WORK_DIR/$fileName" : "$WORK_DIR/${realPath.getName()}"
        if (!result.path.endsWith("/")) {
            result.path = result.path + "/"
        }
        result.type = TesFileType.FILE
        log.trace "[TencentCloud] Adding INPUT file: $result"
        return result
    }

    private TesInput inItem(Path realPath, String fileName = null) {
        def result = new TesInput()
        result.url = realPath.toUriString()
        result.path = fileName ? "$WORK_DIR/$fileName" : "$WORK_DIR/${realPath.getName()}"
        result.type = TesFileType.FILE
        log.trace "[TencentCloud] Adding INPUT file: $result"
        return result
    }

    private TesOutput outItem(String fileName) {
        def result = new TesOutput()
        result.path = "$WORK_DIR/$fileName"
        result.url = task.workDir.resolve(fileName).toUriString()
        result.type = TesFileType.FILE
        log.trace "[TencentCloud] Adding OUTPUT file: $result"
        return result
    }

}
