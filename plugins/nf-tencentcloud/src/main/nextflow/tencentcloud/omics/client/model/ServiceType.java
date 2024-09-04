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
/**
 * Type of a GA4GH service
 */
@Schema(description = "Type of a GA4GH service")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-07-29T15:12:19.210051+08:00[Asia/Shanghai]")

public class ServiceType {
  @SerializedName("group")
  private String group = null;

  @SerializedName("artifact")
  private String artifact = null;

  @SerializedName("version")
  private String version = null;

  public ServiceType group(String group) {
    this.group = group;
    return this;
  }

   /**
   * Namespace in reverse domain name format. Use &#x60;org.ga4gh&#x60; for implementations compliant with official GA4GH specifications. For services with custom APIs not standardized by GA4GH, or implementations diverging from official GA4GH specifications, use a different namespace (e.g. your organization&#x27;s reverse domain name).
   * @return group
  **/
  @Schema(example = "org.ga4gh", required = true, description = "Namespace in reverse domain name format. Use `org.ga4gh` for implementations compliant with official GA4GH specifications. For services with custom APIs not standardized by GA4GH, or implementations diverging from official GA4GH specifications, use a different namespace (e.g. your organization's reverse domain name).")
  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public ServiceType artifact(String artifact) {
    this.artifact = artifact;
    return this;
  }

   /**
   * Name of the API or GA4GH specification implemented. Official GA4GH types should be assigned as part of standards approval process. Custom artifacts are supported.
   * @return artifact
  **/
  @Schema(example = "beacon", required = true, description = "Name of the API or GA4GH specification implemented. Official GA4GH types should be assigned as part of standards approval process. Custom artifacts are supported.")
  public String getArtifact() {
    return artifact;
  }

  public void setArtifact(String artifact) {
    this.artifact = artifact;
  }

  public ServiceType version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Version of the API or specification. GA4GH specifications use semantic versioning.
   * @return version
  **/
  @Schema(example = "1.0.0", required = true, description = "Version of the API or specification. GA4GH specifications use semantic versioning.")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceType serviceType = (ServiceType) o;
    return Objects.equals(this.group, serviceType.group) &&
        Objects.equals(this.artifact, serviceType.artifact) &&
        Objects.equals(this.version, serviceType.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(group, artifact, version);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceType {\n");
    
    sb.append("    group: ").append(toIndentedString(group)).append("\n");
    sb.append("    artifact: ").append(toIndentedString(artifact)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
