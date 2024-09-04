package nextflow.tencentcloud.omics.executor

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import nextflow.executor.Executor
import nextflow.extension.FilesEx
import nextflow.processor.TaskHandler
import nextflow.processor.TaskMonitor
import nextflow.processor.TaskPollingMonitor
import nextflow.processor.TaskRun
import nextflow.tencentcloud.omics.client.ApiClient
import nextflow.tencentcloud.omics.client.api.TaskServiceApi
import nextflow.tencentcloud.omics.client.auth.OAuth
import nextflow.util.Duration
import nextflow.util.ServiceName
import org.pf4j.ExtensionPoint

import java.nio.file.Path

/**
 * TencentCloud Omics executor
 *
 *
 */
@Slf4j
@CompileStatic
@ServiceName('tencentcloud-omics')
class TencentCloudOmicsExecutor extends Executor implements ExtensionPoint {

    private TaskServiceApi client

    /**
     * A path accessible to TencentCloud Omics where executable scripts need to be uploaded
     */
    private Path remoteBinDir = null

    @Override
    protected void register() {
        super.register()
        uploadBinDir()

        String t = getAccessToken()
        if (t) {
            ApiClient apiClient = new ApiClient(
                    basePath: getEndPoint(),
                    authentications: new HashMap() {
                        {
                            put("oauth", new OAuth(accessToken: t))
                        }
                    },
            )
            apiClient.setAccessToken(t)
            client = new TaskServiceApi(
                    apiClient: apiClient,
                    localVarAuthNames: new String[]{"oauth"},
            )
        } else {
            client = new TaskServiceApi(new ApiClient(basePath: getEndPoint()))
        }
    }

    protected String getDisplayName() {
        return "$name [${getEndPoint()}]"
    }

    TaskServiceApi getClient() {
        client
    }

    @PackageScope
    Path getRemoteBinDir() {
        remoteBinDir
    }

    protected void uploadBinDir() {
        /*
         * upload local binaries
         */
        if (session.binDir && !session.binDir.empty() && !session.disableRemoteBinDir) {
            def tempBin = getTempDir()
            log.info "Uploading local `bin` scripts folder ${session.binDir.toUriString()} to ${tempBin.toUriString()}/bin"
            remoteBinDir = FilesEx.copyTo(session.binDir, tempBin)
        }
    }

    protected String getEndPoint() {
        String result = session.getConfigAttribute('tencentcloud.omics.endpoint', null)
        if (!result) {
            throw new RuntimeException("tencentcloud.omics.endpoint must be specified!")
        }
        while (result.endsWith('/')) {
            result = result.substring(0, result.length() - 1)
        }
        if (!result.endsWith('/v1')) {
            result += '/v1'
        }
        log.debug "[TencentCloud] endpoint=$result"
        return result
    }

    protected String getAccessToken() {
        def result = session.getConfigAttribute('tencentcloud.omics.accessToken', null)
        if (!result) {
            throw new RuntimeException("tencentcloud.omics.accessToken must be specified!")
        }
        log.debug "[TencentCloud] accessToken=$result"
        return result
    }

    /**
     * @return {@code true} whenever the containerization is managed by the executor itself
     */
    boolean isContainerNative() {
        return true
    }

    /**
     * Create a a queue holder for this executor
     *
     * @return
     */
    TaskMonitor createTaskMonitor() {
        return TaskPollingMonitor.create(session, name, 100, Duration.of('1 sec'))
    }


    /*
     * Prepare and launch the task in the underlying execution platform
     */

    @Override
    TaskHandler createTaskHandler(TaskRun task) {
        assert task
        assert task.workDir
        log.debug "[TencentCloud] Launching process > ${task.name} -- work folder: ${task.workDir}"
        new TencentCloudOmicsTaskHandler(task, this)
    }
}



