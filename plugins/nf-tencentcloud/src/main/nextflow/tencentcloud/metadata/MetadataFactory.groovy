package nextflow.tencentcloud.metadata

import groovy.transform.CompileStatic
import nextflow.Session
import nextflow.trace.TraceObserver
import nextflow.trace.TraceObserverFactory

/**
 * Factory logic for Metadata json observer
 *
 */
@CompileStatic
class MetadataFactory implements TraceObserverFactory {

    @Override
    Collection<TraceObserver> create(Session session) {
        def isEnabled = session.config.navigate('tencentcloud.metadata.enabled') as Boolean
        def file = session.config.navigate('tencentcloud.metadata.file') as String
        def result = new ArrayList()
        if (isEnabled || file) {
            def observer = new MetadataObserver(file)
            result << observer
        }
        return result
    }

}
