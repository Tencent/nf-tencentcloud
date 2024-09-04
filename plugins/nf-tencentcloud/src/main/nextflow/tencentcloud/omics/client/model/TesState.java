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
import io.swagger.v3.oas.annotations.media.Schema;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Task state as defined by the server.   - &#x60;UNKNOWN&#x60;: The state of the task is unknown. The cause for this status   message may be dependent on the underlying system. The &#x60;UNKNOWN&#x60; states   provides a safe default for messages where this field is missing so   that a missing field does not accidentally imply that   the state is QUEUED.  - &#x60;QUEUED&#x60;: The task is queued and awaiting resources to begin computing.  - &#x60;INITIALIZING&#x60;: The task has been assigned to a worker and is currently preparing to run. For example, the worker may be turning on, downloading input files, etc.  - &#x60;RUNNING&#x60;: The task is running. Input files are downloaded and the first Executor has been started.  - &#x60;PAUSED&#x60;: The task is paused. The reasons for this would be tied to   the specific system running the job. An implementation may have the ability   to pause a task, but this is not required.  - &#x60;COMPLETE&#x60;: The task has completed running. Executors have exited without error and output files have been successfully uploaded.  - &#x60;EXECUTOR_ERROR&#x60;: The task encountered an error in one of the Executor processes. Generally, this means that an Executor exited with a non-zero exit code.  - &#x60;SYSTEM_ERROR&#x60;: The task was stopped due to a system error, but not from an Executor, for example an upload failed due to network issues, the worker&#x27;s ran out of disk space, etc.  - &#x60;CANCELED&#x60;: The task was canceled by the user, and downstream resources have been deleted.  - &#x60;CANCELING&#x60;: The task was canceled by the user, but the downstream resources are still awaiting deletion.  - &#x60;PREEMPTED&#x60;: The task is stopped (preempted) by the system. The reasons for this would be tied to the specific system running the job. Generally, this means that the system reclaimed the compute capacity for reallocation.
 */
@JsonAdapter(TesState.Adapter.class)
public enum TesState {
  @SerializedName("UNKNOWN")
  UNKNOWN("UNKNOWN"),
  @SerializedName("QUEUED")
  QUEUED("QUEUED"),
  @SerializedName("INITIALIZING")
  INITIALIZING("INITIALIZING"),
  @SerializedName("RUNNING")
  RUNNING("RUNNING"),
  @SerializedName("PAUSED")
  PAUSED("PAUSED"),
  @SerializedName("COMPLETE")
  COMPLETE("COMPLETE"),
  @SerializedName("EXECUTOR_ERROR")
  EXECUTOR_ERROR("EXECUTOR_ERROR"),
  @SerializedName("SYSTEM_ERROR")
  SYSTEM_ERROR("SYSTEM_ERROR"),
  @SerializedName("CANCELED")
  CANCELED("CANCELED"),
  @SerializedName("PREEMPTED")
  PREEMPTED("PREEMPTED"),
  @SerializedName("CANCELING")
  CANCELING("CANCELING");

  private String value;

  TesState(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static TesState fromValue(String input) {
    for (TesState b : TesState.values()) {
      if (b.value.equals(input)) {
        return b;
      }
    }
    return null;
  }

  public static class Adapter extends TypeAdapter<TesState> {
    @Override
    public void write(final JsonWriter jsonWriter, final TesState enumeration) throws IOException {
      jsonWriter.value(String.valueOf(enumeration.getValue()));
    }

    @Override
    public TesState read(final JsonReader jsonReader) throws IOException {
      Object value = jsonReader.nextString();
      return TesState.fromValue((String)(value));
    }
  }
}
