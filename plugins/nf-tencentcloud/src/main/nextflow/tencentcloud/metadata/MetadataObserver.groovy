package nextflow.tencentcloud.metadata

import groovy.json.JsonGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.gpars.agent.Agent
import nextflow.Const
import nextflow.NextflowMeta
import nextflow.Session
import nextflow.processor.TaskHandler
import nextflow.script.ScriptBinding.ParamsMap
import nextflow.script.WorkflowMetadata
import nextflow.trace.TraceObserver
import nextflow.trace.TraceRecord
import nextflow.util.Duration

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * Generate and save metadata json file into specific path
 *
 */
@Slf4j
@CompileStatic
class MetadataObserver extends TimerTask implements TraceObserver {

    private Session session

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC")

    /**
     * Workflow identifier, will be taken from the Session() object later
     */
    private String runName

    /**
     * Store the sessions unique ID for downstream reference purposes
     */
    private String runId

    /**
     * The file path of the metadata
     */
    private Path filePath

    /**
     * An agent for the metadata generator in an own thread
     */
    private Agent<MetadataObserver> metadataAgent

    /**
     * Json generator for weblog payloads
     */
    private JsonGenerator generator

    /**
     * local metadata saver
     */
    private Metadata metadata = new Metadata()

    /**
     * check if new Metadata exist
     */
    private boolean newMeta = false

    /**
     * timer for saving the metadata
     */
    private Timer metaTimer = new Timer()

    /**
     * Constructor that consumes a metadata file path
     * @param metadata filePath
     */
    MetadataObserver(String filePath) {
        this.filePath = checkFilePath(filePath)
        this.metadataAgent = new Agent<>(this)
        this.generator = createJsonGeneratorForPayloads()
        metaTimer.schedule(this, 0, 10000)
    }

    /**
     * only for testing purpose -- do not use
     */
    protected MetadataObserver() {
        this.metadataAgent = new Agent<>(this)
        this.generator = createJsonGeneratorForPayloads()
    }

    @Override
    void run() {
        metadataAgent.send {
            try {
                writeMeta()
            }
            catch (Exception e) {
                log.warn1 e.message
            }
        }
    }

    protected static Path checkFilePath(String filePath) {
        if (!filePath) {
            return Paths.get(System.getProperty("user.dir") as String).resolve("metadata.json")
        }
        Path p = Paths.get(filePath)
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir") as String).resolve(p)
        }
        Path parentDir = p.getParent()
        try {
            Files.createDirectories(parentDir);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to create parent directories for metadata file: ${p}, message: ${e.getMessage()}")
        }
        return p
    }

    /**
     * On workflow start, submit a message with some basic
     * information, like Id, activity and an ISO 8601 formatted
     * timestamp.
     * @param session The current Nextflow session object
     */
    @Override
    void onFlowCreate(Session session) {
        this.session = session
        runName = session.getRunName()
        runId = session.getUniqueId()

        asyncMetaMessage("started", createFlowPayloadFromSession(session))
    }

    /**
     * Save a metadata when the workflow is completed.
     */
    @Override
    void onFlowComplete() {
        asyncMetaMessage("completed", createFlowPayloadFromSession(this.session))
        run()
        metaTimer.cancel()
        metadataAgent.await()
    }

    /**
     * Save a metadata when a process has been submitted
     *
     * @param handler A {@link TaskHandler} object representing the task submitted
     * @param trace A {@link TraceRecord} object holding the task metadata and runtime info
     */
    @Override
    void onProcessSubmit(TaskHandler handler, TraceRecord trace) {
        asyncMetaMessage("process_submitted", trace)
    }

    /**
     * Save a metadata, when a process has started
     *
     * @param handler A {@link TaskHandler} object representing the task started
     * @param trace A {@link TraceRecord} object holding the task metadata and runtime info
     */
    @Override
    void onProcessStart(TaskHandler handler, TraceRecord trace) {
        asyncMetaMessage("process_started", trace)
    }

    /**
     * Save a metadata, when a process completed
     *
     * @param handler A {@link TaskHandler} object representing the task completed
     * @param trace A {@link TraceRecord} object holding the task metadata and runtime info
     */
    @Override
    void onProcessComplete(TaskHandler handler, TraceRecord trace) {
        asyncMetaMessage("process_completed", trace)
    }

    /**
     * Save a metadata, when a workflow has failed
     *
     * @param handler A {@link TaskHandler} object representing the task that caused the workflow execution to fail (it may be null)
     * @param trace A {@link TraceRecord} object holding the task metadata and runtime info (it may be null)
     */
    @Override
    void onFlowError(TaskHandler handler, TraceRecord trace) {
        asyncMetaMessage("error", trace)
    }

    /**
     * Little helper method that save a metadata message as JSON with
     * the current run status, ISO 8601 UTC timestamp, run name and the TraceRecord
     * object, if present.
     * @param event The current run status. One of {'started', 'process_submit', 'process_start',
     * 'process_complete', 'error', 'completed'}
     * @param payload An additional object to send. Must be of type TraceRecord or Manifest
     */
    protected void sendMetaMessage(String event, Object payload = null) {
        log.trace "Sending weblog event=$event; payload=$payload"
//        // Set the message info
        final time = new Date().format(Const.ISO_8601_DATETIME_FORMAT, UTC)

        metadata.runName = runName
        metadata.runId = runId
        metadata.event = event
        metadata.utcTime = time

        if (payload instanceof TraceRecord) {
            final message = new HashMap(5)
            message.runName = runName
            message.runId = runId
            message.event = event
            message.utcTime = time
            message.trace = payload.getStore()

            if (metadata.trace == null) {
                metadata.trace = new ArrayList()
            }
            metadata.trace.add(message)
        } else if (payload instanceof FlowPayload) {
            metadata.metadata = payload
        } else if (payload != null)
            throw new IllegalArgumentException("Only TraceRecord and Manifest class types are supported: [${payload.getClass().getName()}] $payload")
        newMeta = true
    }

    protected void writeMeta() {
        if (!newMeta) {
            return
        }
        log.trace "write metadata"
        filePath.write(generator.toJson(metadata), 'UTF-8')
        newMeta = false
    }

    protected static FlowPayload createFlowPayloadFromSession(Session session) {
        def params = session.binding.getProperty('params') as ParamsMap
        def workflow = session.getWorkflowMetadata()
        new FlowPayload(params, workflow)
    }

    /**
     * Asynchronous metadata generate wrapper.
     * @param event The workflow run status
     * @param payload An additional object to send. Must be of type TraceRecord or Manifest
     */
    protected void asyncMetaMessage(String event, Object payload = null) {
        metadataAgent.send {
            try {
                sendMetaMessage(event, payload)
            }
            catch (Exception e) {
                log.warn1 e.message
            }
        }
    }

    private static JsonGenerator createJsonGeneratorForPayloads() {
        new JsonGenerator.Options()
                .addConverter(Path) { Path p, String key -> p.toUriString() }
                .addConverter(Duration) { Duration d, String key -> d.durationInMillis }
                .addConverter(NextflowMeta) { meta, key -> meta.toJsonMap() }
                .dateFormat(Const.ISO_8601_DATETIME_FORMAT).timezone("UTC")
                .build()
    }

    private static class FlowPayload {

        final ParamsMap parameters

        final WorkflowMetadata workflow

        FlowPayload(ParamsMap params, WorkflowMetadata workflow) {
            this.parameters = params
            this.workflow = workflow
        }
    }

    class Metadata {
        String runName
        String runId
        String event
        String utcTime
        ArrayList trace = new ArrayList()
        FlowPayload metadata
    }
}