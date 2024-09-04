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
import java.util.List;
import nextflow.tencentcloud.omics.client.model.Service;
import nextflow.tencentcloud.omics.client.model.ServiceOrganization;
import nextflow.tencentcloud.omics.client.model.TesServiceType;
import org.threeten.bp.OffsetDateTime;
/**
 * TesServiceInfo
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-07-29T15:12:19.210051+08:00[Asia/Shanghai]")

public class TesServiceInfo extends Service {
  @SerializedName("storage")
  private List<String> storage = null;

  @SerializedName("tesResources_backend_parameters")
  private List<String> tesResourcesBackendParameters = null;

  @SerializedName("type")
  private TesServiceType tesServiceInfoType = null;

  public TesServiceInfo storage(List<String> storage) {
    this.storage = storage;
    return this;
  }

  public TesServiceInfo addStorageItem(String storageItem) {
    if (this.storage == null) {
      this.storage = new ArrayList<String>();
    }
    this.storage.add(storageItem);
    return this;
  }

   /**
   * Lists some, but not necessarily all, storage locations supported by the service.
   * @return storage
  **/
  @Schema(example = "[\"file:///path/to/local/funnel-storage\",\"s3://ohsu-compbio-funnel/storage\"]", description = "Lists some, but not necessarily all, storage locations supported by the service.")
  public List<String> getStorage() {
    return storage;
  }

  public void setStorage(List<String> storage) {
    this.storage = storage;
  }

  public TesServiceInfo tesResourcesBackendParameters(List<String> tesResourcesBackendParameters) {
    this.tesResourcesBackendParameters = tesResourcesBackendParameters;
    return this;
  }

  public TesServiceInfo addTesResourcesBackendParametersItem(String tesResourcesBackendParametersItem) {
    if (this.tesResourcesBackendParameters == null) {
      this.tesResourcesBackendParameters = new ArrayList<String>();
    }
    this.tesResourcesBackendParameters.add(tesResourcesBackendParametersItem);
    return this;
  }

   /**
   * Lists all tesResources.backend_parameters keys supported by the service
   * @return tesResourcesBackendParameters
  **/
  @Schema(example = "[\"VmSize\"]", description = "Lists all tesResources.backend_parameters keys supported by the service")
  public List<String> getTesResourcesBackendParameters() {
    return tesResourcesBackendParameters;
  }

  public void setTesResourcesBackendParameters(List<String> tesResourcesBackendParameters) {
    this.tesResourcesBackendParameters = tesResourcesBackendParameters;
  }

  public TesServiceInfo tesServiceInfoType(TesServiceType tesServiceInfoType) {
    this.tesServiceInfoType = tesServiceInfoType;
    return this;
  }

   /**
   * Get tesServiceInfoType
   * @return tesServiceInfoType
  **/
  @Schema(description = "")
  public TesServiceType getTesServiceInfoType() {
    return tesServiceInfoType;
  }

  public void setTesServiceInfoType(TesServiceType tesServiceInfoType) {
    this.tesServiceInfoType = tesServiceInfoType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TesServiceInfo tesServiceInfo = (TesServiceInfo) o;
    return Objects.equals(this.storage, tesServiceInfo.storage) &&
        Objects.equals(this.tesResourcesBackendParameters, tesServiceInfo.tesResourcesBackendParameters) &&
        Objects.equals(this.tesServiceInfoType, tesServiceInfo.tesServiceInfoType) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(storage, tesResourcesBackendParameters, tesServiceInfoType, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TesServiceInfo {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    storage: ").append(toIndentedString(storage)).append("\n");
    sb.append("    tesResourcesBackendParameters: ").append(toIndentedString(tesResourcesBackendParameters)).append("\n");
    sb.append("    tesServiceInfoType: ").append(toIndentedString(tesServiceInfoType)).append("\n");
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