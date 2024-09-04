package nextflow.tencentcloud

import groovy.transform.CompileStatic
import nextflow.file.FileHelper
import nextflow.plugin.BasePlugin
import nextflow.tencentcloud.nio.CosFileSystemProvider
import org.pf4j.PluginWrapper

/**
 * Nextflow plugin for TencentCloud extensions
 *
 */
@CompileStatic
class TencentCloudPlugin extends BasePlugin {

    TencentCloudPlugin(PluginWrapper wrapper) {
        super(wrapper)
    }

    @Override
    void start() {
        super.start()
        FileHelper.getOrInstallProvider(CosFileSystemProvider)
    }

}
