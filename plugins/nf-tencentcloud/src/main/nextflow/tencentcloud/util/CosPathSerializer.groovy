package nextflow.tencentcloud.util

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.tencentcloud.nio.CosPath
import nextflow.util.SerializerRegistrant
import org.pf4j.Extension

/**
 * Register the CosPath serializer
 *
 */
@Slf4j
@Extension
@CompileStatic
class CosPathSerializer extends Serializer<CosPath> implements SerializerRegistrant {

    @Override
    void register(Map<Class, Object> serializers) {
        serializers.put(CosPath, CosPathSerializer)
    }

    @Override
    void write(Kryo kryo, Output output, CosPath target) {
        final scheme = target.getFileSystem().provider().getScheme()
        final path = target.toString()
        log.trace "CosPath serialization > scheme: $scheme; path: $path"
        output.writeString(scheme)
        output.writeString(path)
    }

    @Override
    CosPath read(Kryo kryo, Input input, Class<CosPath> type) {
        final scheme = input.readString()
        final path = input.readString()
        if (scheme != 'cos') throw new IllegalStateException("Unexpected scheme for Cos path -- offending value '$scheme'")
        log.trace "CosPath de-serialization > scheme: $scheme; path: $path"
        return (CosPath) CosPathFactory.create("cos://${path}")
    }

}
