/*
 * Task Execution Service
 * ## Executive Summary The Task Execution Service (TES) API is a standardized schema and API for describing and executing batch execution tasks. A task defines a set of input files, a set of containers and commands to run, a set of output files and some other logging and metadata.  TES servers accept task documents and execute them asynchronously on available compute resources. A TES server could be built on top of a traditional HPC queuing system, such as Grid Engine, Slurm or cloud style compute systems such as AWS Batch or Kubernetes. ## Introduction This document describes the TES API and provides details on the specific endpoints, request formats, and responses. It is intended to provide key information for developers of TES-compatible services as well as clients that will call these TES services. Use cases include:    - Deploying existing workflow engines on new infrastructure. Workflow engines   such as CWL-Tes and Cromwell have extentions for using TES. This will allow   a system engineer to deploy them onto a new infrastructure using a job scheduling   system not previously supported by the engine.    - Developing a custom workflow management system. This API provides a common   interface to asynchronous batch processing capabilities. A developer can write   new tools against this interface and expect them to work using a variety of   backend solutions that all support the same specification.   ## Standards The TES API specification is written in OpenAPI and embodies a RESTful service philosophy. It uses JSON in requests and responses and standard HTTP/HTTPS for information transport. HTTPS should be used rather than plain HTTP except for testing or internal-only purposes. ### Authentication and Authorization Is is envisaged that most TES API instances will require users to authenticate to use the endpoints. However, the decision if authentication is required should be taken by TES API implementers.  If authentication is required, we recommend that TES implementations use an OAuth2  bearer token, although they can choose other mechanisms if appropriate.  Checking that a user is authorized to submit TES requests is a responsibility of TES implementations. ### CORS If TES API implementation is to be used by another website or domain it must implement Cross Origin Resource Sharing (CORS). Please refer to https://w3id.org/ga4gh/product-approval-support/cors for more information about GA4GH’s recommendations and how to implement CORS. 
 *
 * OpenAPI spec version: 1.1.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package nextflow.tencentcloud.omics.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nextflow.tencentcloud.omics.client.model.TesExecutorLog;
import nextflow.tencentcloud.omics.client.model.TesOutputFileLog;
/**
 * TaskLog describes logging information related to a Task.
 */
@Schema(description = "TaskLog describes logging information related to a Task.")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-07-29T15:12:19.210051+08:00[Asia/Shanghai]")

public class TesTaskLog {
  @SerializedName("logs")
  private List<TesExecutorLog> logs = new ArrayList<TesExecutorLog>();

  @SerializedName("metadata")
  private Map<String, String> metadata = null;

  @SerializedName("start_time")
  private String startTime = null;

  @SerializedName("end_time")
  private String endTime = null;

  @SerializedName("outputs")
  private List<TesOutputFileLog> outputs = new ArrayList<TesOutputFileLog>();

  @SerializedName("system_logs")
  private List<String> systemLogs = null;

  public TesTaskLog logs(List<TesExecutorLog> logs) {
    this.logs = logs;
    return this;
  }

  public TesTaskLog addLogsItem(TesExecutorLog logsItem) {
    this.logs.add(logsItem);
    return this;
  }

   /**
   * Logs for each executor
   * @return logs
  **/
  @Schema(required = true, description = "Logs for each executor")
  public List<TesExecutorLog> getLogs() {
    return logs;
  }

  public void setLogs(List<TesExecutorLog> logs) {
    this.logs = logs;
  }

  public TesTaskLog metadata(Map<String, String> metadata) {
    this.metadata = metadata;
    return this;
  }

  public TesTaskLog putMetadataItem(String key, String metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<String, String>();
    }
    this.metadata.put(key, metadataItem);
    return this;
  }

   /**
   * Arbitrary logging metadata included by the implementation.
   * @return metadata
  **/
  @Schema(example = "{\"host\":\"worker-001\",\"slurmm_id\":123456}", description = "Arbitrary logging metadata included by the implementation.")
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public TesTaskLog startTime(String startTime) {
    this.startTime = startTime;
    return this;
  }

   /**
   * When the task started, in RFC 3339 format.
   * @return startTime
  **/
  @Schema(example = "2020-10-02T10:00:00-05:00", description = "When the task started, in RFC 3339 format.")
  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public TesTaskLog endTime(String endTime) {
    this.endTime = endTime;
    return this;
  }

   /**
   * When the task ended, in RFC 3339 format.
   * @return endTime
  **/
  @Schema(example = "2020-10-02T11:00:00-05:00", description = "When the task ended, in RFC 3339 format.")
  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public TesTaskLog outputs(List<TesOutputFileLog> outputs) {
    this.outputs = outputs;
    return this;
  }

  public TesTaskLog addOutputsItem(TesOutputFileLog outputsItem) {
    this.outputs.add(outputsItem);
    return this;
  }

   /**
   * Information about all output files. Directory outputs are flattened into separate items.
   * @return outputs
  **/
  @Schema(required = true, description = "Information about all output files. Directory outputs are flattened into separate items.")
  public List<TesOutputFileLog> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TesOutputFileLog> outputs) {
    this.outputs = outputs;
  }

  public TesTaskLog systemLogs(List<String> systemLogs) {
    this.systemLogs = systemLogs;
    return this;
  }

  public TesTaskLog addSystemLogsItem(String systemLogsItem) {
    if (this.systemLogs == null) {
      this.systemLogs = new ArrayList<String>();
    }
    this.systemLogs.add(systemLogsItem);
    return this;
  }

   /**
   * System logs are any logs the system decides are relevant, which are not tied directly to an Executor process. Content is implementation specific: format, size, etc.  System logs may be collected here to provide convenient access.  For example, the system may include the name of the host where the task is executing, an error message that caused a SYSTEM_ERROR state (e.g. disk is full), etc.  System logs are only included in the FULL task view.
   * @return systemLogs
  **/
  @Schema(description = "System logs are any logs the system decides are relevant, which are not tied directly to an Executor process. Content is implementation specific: format, size, etc.  System logs may be collected here to provide convenient access.  For example, the system may include the name of the host where the task is executing, an error message that caused a SYSTEM_ERROR state (e.g. disk is full), etc.  System logs are only included in the FULL task view.")
  public List<String> getSystemLogs() {
    return systemLogs;
  }

  public void setSystemLogs(List<String> systemLogs) {
    this.systemLogs = systemLogs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TesTaskLog tesTaskLog = (TesTaskLog) o;
    return Objects.equals(this.logs, tesTaskLog.logs) &&
        Objects.equals(this.metadata, tesTaskLog.metadata) &&
        Objects.equals(this.startTime, tesTaskLog.startTime) &&
        Objects.equals(this.endTime, tesTaskLog.endTime) &&
        Objects.equals(this.outputs, tesTaskLog.outputs) &&
        Objects.equals(this.systemLogs, tesTaskLog.systemLogs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(logs, metadata, startTime, endTime, outputs, systemLogs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TesTaskLog {\n");
    
    sb.append("    logs: ").append(toIndentedString(logs)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
    sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
    sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
    sb.append("    systemLogs: ").append(toIndentedString(systemLogs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}