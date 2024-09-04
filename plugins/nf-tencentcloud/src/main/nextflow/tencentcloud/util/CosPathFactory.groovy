package nextflow.tencentcloud.util

import groovy.transform.CompileStatic
import nextflow.Global
import nextflow.file.FileHelper
import nextflow.file.FileSystemPathFactory
import nextflow.tencentcloud.nio.CosPath

import java.nio.file.Path

/**
 * Implements the a factory strategy to parse and build Cos path URIs
 *
 */
@CompileStatic
class CosPathFactory extends FileSystemPathFactory {

    @Override
    protected Path parseUri(String str) {
        // normalise 'cos' path
        if (str.startsWith('cos://') && str[5] != '/') {
            final path = "cos:///${str.substring(5)}"
            return create(path)
        }
        return null
    }

    static private Map config() {
        final result = Global.config?.get('tencent') as Map
        return result != null ? result : Collections.emptyMap()
    }

    @Override
    protected String toUriString(Path path) {
        return path instanceof CosPath ? "cos:/$path".toString() : null
    }

    @Override
    protected String getBashLib(Path target) {
        return ""
    }

    @Override
    protected String getUploadCmd(String source, Path target) {
        return null
    }

    /**
     * Creates a {@link CosPath} from a Cos formatted URI.
     *
     * @param path
     *      A Cos URI path e.g. cos:///BUCKET_NAME/some/data.
     *      NOTE it expect the cos prefix provided with triple `/` .
     *      This is required by the underlying implementation expecting the host name in the URI to be empty
     *      and the bucket name to be the first path element
     * @return
     *      The corresponding {@link CosPath}
     */
    static CosPath create(String path) {
        if (!path) throw new IllegalArgumentException("Missing Cos path argument")
        if (!path.startsWith('cos:///')) throw new IllegalArgumentException("Cos path must start with cos:/// prefix -- offending value '$path'")
        // note: this URI constructor parse the path parameter and extract the `scheme` and `authority` components
        final uri = new URI(null, null, path, null, null)
        return (CosPath) FileHelper.getOrCreateFileSystemFor(uri, config()).provider().getPath(uri)
    }
}
