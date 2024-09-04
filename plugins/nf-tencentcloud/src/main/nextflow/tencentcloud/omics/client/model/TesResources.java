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
/**
 * Resources describes the resources requested by a task.
 */
@Schema(description = "Resources describes the resources requested by a task.")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-07-29T15:12:19.210051+08:00[Asia/Shanghai]")

public class TesResources {
  @SerializedName("cpu_cores")
  private Integer cpuCores = null;

  @SerializedName("preemptible")
  private Boolean preemptible = null;

  @SerializedName("ram_gb")
  private Double ramGb = null;

  @SerializedName("disk_gb")
  private Double diskGb = null;

  @SerializedName("zones")
  private List<String> zones = null;

  @SerializedName("backend_parameters")
  private Map<String, String> backendParameters = null;

  @SerializedName("backend_parameters_strict")
  private Boolean backendParametersStrict = false;

  public TesResources cpuCores(Integer cpuCores) {
    this.cpuCores = cpuCores;
    return this;
  }

   /**
   * Requested number of CPUs
   * @return cpuCores
  **/
  @Schema(example = "4", description = "Requested number of CPUs")
  public Integer getCpuCores() {
    return cpuCores;
  }

  public void setCpuCores(Integer cpuCores) {
    this.cpuCores = cpuCores;
  }

  public TesResources preemptible(Boolean preemptible) {
    this.preemptible = preemptible;
    return this;
  }

   /**
   * Define if the task is allowed to run on preemptible compute instances, for example, AWS Spot. This option may have no effect when utilized on some backends that don&#x27;t have the concept of preemptible jobs.
   * @return preemptible
  **/
  @Schema(example = "false", description = "Define if the task is allowed to run on preemptible compute instances, for example, AWS Spot. This option may have no effect when utilized on some backends that don't have the concept of preemptible jobs.")
  public Boolean isPreemptible() {
    return preemptible;
  }

  public void setPreemptible(Boolean preemptible) {
    this.preemptible = preemptible;
  }

  public TesResources ramGb(Double ramGb) {
    this.ramGb = ramGb;
    return this;
  }

   /**
   * Requested RAM required in gigabytes (GB)
   * @return ramGb
  **/
  @Schema(example = "8", description = "Requested RAM required in gigabytes (GB)")
  public Double getRamGb() {
    return ramGb;
  }

  public void setRamGb(Double ramGb) {
    this.ramGb = ramGb;
  }

  public TesResources diskGb(Double diskGb) {
    this.diskGb = diskGb;
    return this;
  }

   /**
   * Requested disk size in gigabytes (GB)
   * @return diskGb
  **/
  @Schema(example = "40", description = "Requested disk size in gigabytes (GB)")
  public Double getDiskGb() {
    return diskGb;
  }

  public void setDiskGb(Double diskGb) {
    this.diskGb = diskGb;
  }

  public TesResources zones(List<String> zones) {
    this.zones = zones;
    return this;
  }

  public TesResources addZonesItem(String zonesItem) {
    if (this.zones == null) {
      this.zones = new ArrayList<String>();
    }
    this.zones.add(zonesItem);
    return this;
  }

   /**
   * Request that the task be run in these compute zones. How this string is utilized will be dependent on the backend system. For example, a system based on a cluster queueing system may use this string to define priorty queue to which the job is assigned.
   * @return zones
  **/
  @Schema(example = "us-west-1", description = "Request that the task be run in these compute zones. How this string is utilized will be dependent on the backend system. For example, a system based on a cluster queueing system may use this string to define priorty queue to which the job is assigned.")
  public List<String> getZones() {
    return zones;
  }

  public void setZones(List<String> zones) {
    this.zones = zones;
  }

  public TesResources backendParameters(Map<String, String> backendParameters) {
    this.backendParameters = backendParameters;
    return this;
  }

  public TesResources putBackendParametersItem(String key, String backendParametersItem) {
    if (this.backendParameters == null) {
      this.backendParameters = new HashMap<String, String>();
    }
    this.backendParameters.put(key, backendParametersItem);
    return this;
  }

   /**
   * Key/value pairs for backend configuration. ServiceInfo shall return a list of keys that a backend supports. Keys are case insensitive. It is expected that clients pass all runtime or hardware requirement key/values that are not mapped to existing tesResources properties to backend_parameters. Backends shall log system warnings if a key is passed that is unsupported. Backends shall not store or return unsupported keys if included in a task. If backend_parameters_strict equals true, backends should fail the task if any key/values are unsupported, otherwise, backends should attempt to run the task Intended uses include VM size selection, coprocessor configuration, etc. Example: &#x60;&#x60;&#x60; {   \&quot;backend_parameters\&quot; : {     \&quot;VmSize\&quot; : \&quot;Standard_D64_v3\&quot;   } } &#x60;&#x60;&#x60;
   * @return backendParameters
  **/
  @Schema(example = "{\"VmSize\":\"Standard_D64_v3\"}", description = "Key/value pairs for backend configuration. ServiceInfo shall return a list of keys that a backend supports. Keys are case insensitive. It is expected that clients pass all runtime or hardware requirement key/values that are not mapped to existing tesResources properties to backend_parameters. Backends shall log system warnings if a key is passed that is unsupported. Backends shall not store or return unsupported keys if included in a task. If backend_parameters_strict equals true, backends should fail the task if any key/values are unsupported, otherwise, backends should attempt to run the task Intended uses include VM size selection, coprocessor configuration, etc. Example: ``` {   \"backend_parameters\" : {     \"VmSize\" : \"Standard_D64_v3\"   } } ```")
  public Map<String, String> getBackendParameters() {
    return backendParameters;
  }

  public void setBackendParameters(Map<String, String> backendParameters) {
    this.backendParameters = backendParameters;
  }

  public TesResources backendParametersStrict(Boolean backendParametersStrict) {
    this.backendParametersStrict = backendParametersStrict;
    return this;
  }

   /**
   * If set to true, backends should fail the task if any backend_parameters key/values are unsupported, otherwise, backends should attempt to run the task
   * @return backendParametersStrict
  **/
  @Schema(example = "false", description = "If set to true, backends should fail the task if any backend_parameters key/values are unsupported, otherwise, backends should attempt to run the task")
  public Boolean isBackendParametersStrict() {
    return backendParametersStrict;
  }

  public void setBackendParametersStrict(Boolean backendParametersStrict) {
    this.backendParametersStrict = backendParametersStrict;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TesResources tesResources = (TesResources) o;
    return Objects.equals(this.cpuCores, tesResources.cpuCores) &&
        Objects.equals(this.preemptible, tesResources.preemptible) &&
        Objects.equals(this.ramGb, tesResources.ramGb) &&
        Objects.equals(this.diskGb, tesResources.diskGb) &&
        Objects.equals(this.zones, tesResources.zones) &&
        Objects.equals(this.backendParameters, tesResources.backendParameters) &&
        Objects.equals(this.backendParametersStrict, tesResources.backendParametersStrict);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpuCores, preemptible, ramGb, diskGb, zones, backendParameters, backendParametersStrict);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TesResources {\n");
    
    sb.append("    cpuCores: ").append(toIndentedString(cpuCores)).append("\n");
    sb.append("    preemptible: ").append(toIndentedString(preemptible)).append("\n");
    sb.append("    ramGb: ").append(toIndentedString(ramGb)).append("\n");
    sb.append("    diskGb: ").append(toIndentedString(diskGb)).append("\n");
    sb.append("    zones: ").append(toIndentedString(zones)).append("\n");
    sb.append("    backendParameters: ").append(toIndentedString(backendParameters)).append("\n");
    sb.append("    backendParametersStrict: ").append(toIndentedString(backendParametersStrict)).append("\n");
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