package nextflow.tencentcloud.omics.config

import com.qcloud.cos.ClientConfig
import com.qcloud.cos.auth.BasicCOSCredentials
import com.qcloud.cos.auth.BasicSessionCredentials
import com.qcloud.cos.auth.COSCredentials
import com.qcloud.cos.http.HttpProtocol
import com.qcloud.cos.region.Region
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.Yaml
import nextflow.Global
import org.ini4j.Ini
import org.ini4j.Profile.Section

/**
 * Model Tencent cloud configuration settings
 *
 */
@Slf4j
@CompileStatic
class CosConfig {

    private String bucket

    private String secretId

    private String secretKey

    private String sessionToken

    private ClientConfig clientConfig

    private COSCredentials credentials

    private Region region

    CosConfig(Map config, String bucket) {
        this.bucket = bucket

        getConfig()

        if (!sessionToken) {
            this.credentials = new BasicCOSCredentials(secretId, secretKey);
        } else {
            this.credentials = new BasicSessionCredentials(secretId, secretKey, sessionToken);
        }
        this.clientConfig = new ClientConfig(region)
        clientConfig.setHttpProtocol(HttpProtocol.https);
    }

    private void getConfig() {
        // read rclone config file first
        if (!getConfigFromRClone()) {
            // read cos config file next
            if (!getConfigFromCosSecret()) {
                // read nextflow file finally
                getConfigFromNFConfig()
            }
        }

        checkConfig()
    }

    private boolean getConfigFromCosSecret() {
        def filePath = '/config/cos-secret/' + this.bucket
        def file = new File(filePath)

        if (!file.exists()) {
            return false
        }
        Yaml parser = new Yaml()

        def yaml = parser.load(file.text) as Map<String, Object>

        this.secretId = yaml['tmpSecretId']
        this.secretKey = yaml['tmpSecretKey']
        this.sessionToken = yaml['token']
        this.region = new Region(yaml['region'] as String)
        return true
    }

    private boolean getConfigFromRClone() {
        def filePath = '/config/rclone/rclone.conf'
        def file = new File(filePath)

        if (!file.exists()) {
            return false
        }

        Ini ini = new Ini(file);

        // 访问配置节
        Section bucket = ini.get(bucket);
        if (!bucket) {
            return false
        }

        this.secretId = bucket.get("secret_id")
        this.secretKey = bucket.get("secret_key")
        this.sessionToken = bucket.get("session_token")
        this.region = new Region(bucket.get("region") as String)
        return true
    }

    private void getConfigFromNFConfig() {
        def config = Global.config?.get('tencentcloud') as Map
        if (!config) {
            throw new RuntimeException("tencent cloud config not found in nextflow config file")
        }

        this.secretId = config.secretId
        this.secretKey = config.secretKey
        this.sessionToken = config.sessionToken
        this.region = new Region(config.region as String)

        checkConfig()
    }

    private void checkConfig() {
        if (!secretId) {
            throw new RuntimeException("tencent secretId config not found for bucket:${bucket}")
        }
        if (!secretKey) {
            throw new RuntimeException("tencent secretKey not found for bucket:${bucket}")
        }
        if (!region.getRegionName()) {
            throw new RuntimeException("tencent region not found for bucket:${bucket}")
        }
    }

    ClientConfig getClientConfig() { clientConfig }

    COSCredentials getCredentials() { credentials }

    String getBucket() { bucket }

    Region getRegion() { region }
}
