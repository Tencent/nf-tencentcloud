English | [简体中文](./README_ZH.md)

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="assets/logo-bg-dark.png">
  <source media="(prefers-color-scheme: light)" srcset="assets/logo-bg-light.png">
  <img alt="nf-tencentcloud Logo" src="assets/logo-bg-light.png">
</picture>

## nf-tencentcloud

[![Nextflow](https://img.shields.io/badge/nextflow%20DSL1-%E2%89%A522.10.7-23aa62.svg)](https://www.nextflow.io/)
[![Release](https://img.shields.io/badge/v1.0.0-v?label=realease)](https://github.com/Tencent/nf-tencentcloud/releases/tag/1.0.0)
[![TencentCos](https://img.shields.io/badge/TencentCos-s?logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyAgIHdpZHRoPSIxNnB4IiAgIGhlaWdodD0iMTZweCIgIHZpZXdCb3g9IjAgMCAxNiAxNiIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIj4KICAgIDx0aXRsZT7lr7nosaHlrZjlgqgtMTZweDwvdGl0bGU%2BCiAgICA8ZyBpZD0i5a%2B56LGh5a2Y5YKoLTE2cHgiIHN0cm9rZT0ibm9uZSIgc3Ryb2tlLXdpZHRoPSIxIiBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPgogICAgICAgIDxnIGlkPSLnvJbnu4QiPgogICAgICAgICAgICA8cmVjdCBpZD0iUmVjdGFuZ2xlLUNvcHkiIGZpbGw9IiM0NDQ0NDQiIG9wYWNpdHk9IjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiI%2BPC9yZWN0PgogICAgICAgICAgICA8cGF0aCBkPSJNOCwwIEwxLDQuMDAxIEwxLDEyLjAwMSBMOCwxNiBMMTUsMTIuMDAxIEwxNSw0LjAwMSBMOCwwIFogTTQuMDQ2LDQuNTYzIEw4LDIuMzA0IEwxMS45NTMsNC41NjMgTDgsNi44NDUgTDQuMDQ2LDQuNTYzIFogTTksOC41NzggTDEyLjk5OSw2LjI2OCBMMTIuOTk5LDEwLjg0IEw5LDEzLjEyNiBMOSw4LjU3OCBaIE0zLDEwLjg0IEwzLDYuMjY4IEw3LDguNTc4IEw3LDEzLjEyNiBMMywxMC44NCBaIiBpZD0iRmlsbCIgZmlsbD0iI0ZGRkZGRiI%2BPC9wYXRoPgogICAgICAgIDwvZz4KICAgIDwvZz4KPC9zdmc%2B&label=run%20with)](https://cloud.tencent.com/product/cos)
[![TencentOmics](https://img.shields.io/badge/TencentOmics-s?logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1%2BjfqAAAAIGNIUk0AAHomAACAhAAA%2BgAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAACYktHRAD%2Fh4%2FMvwAAAAlwSFlzAABYlQAAWJUB2W030wAAAAd0SU1FB%2BgIFgYnAkOH3zYAAAFRSURBVCjPRZExS9txFEWPv7Rqo6QIdZaSTRFFKHZorYagm0umxkKloPkSpVu3QrcOQhEHtS7VTSrZgihBdLJxUAimpYNKRYXUBDwd8o95b3nv3ru88wAQExb9Z0rESWvu2C0AgUZdU6SDt3TSwzwPKXITOTZ72FMvzbmqHjnQ1DFmPFqybnmslpyIlLgh8IwVMgSgnRGS%2FGCZBBAjywqD%2BEn9Ko575q0fHbNs2QGDa%2BqHwAsgD8zxhHUW%2BcMefcxyRx54iRVvfG7CA%2FXMigVnrbpr3JS3lppn1qkCDwD4S414NBP4TRdPqVIA9pniDWkSHHJFknZ%2BBQpAGliiTJJe5shxwQJtpIECjrphxiBO%2B95N9dx3YszXrjuEMR9FWF75Uz1xy5kWqBbqfkvqN3NeWnHkHvX9Pz6r3%2B2xw0X1i22RGQW63bbulIjjVt3zcSPwH3ir7avSxkx9AAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI0LTA4LTIyVDA2OjM4OjQ0KzAwOjAwUNWOEgAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNC0wOC0yMlQwNjozODo0NCswMDowMCGINq4AAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjQtMDgtMjJUMDY6Mzk6MDIrMDA6MDB%2BxUePAAAAAElFTkSuQmCC&label=run%20with)](https://cloud.tencent.com/product/omics)
[![nf-tencentcloud license](https://img.shields.io/github/license/Tencent/nf-tencentcloud.svg?colorB=58bd9f&style=popout)](https://github.com/Tencent/nf-tencentcloud/blob/master/LICENSE)

nf-tencentcloud is a nextflow plugin designed to add Tencent Cloud Object storage and Tencent Healthcare Omics Platform executor adaptation support to the nextflow workflow engine. Through this component, it can also implement some detailed function adaptations required by the platform, such as metadata file generation, to ensure the efficient operation and management of the workflow. Its design goal is to extend Tencent Cloud's native support for Nextflow workflows, allowing users to run Nextflow workflows using Tencent Cloud resources in a simple and easy-to-use manner.

we use [Task Execution Schema](https://github.com/ga4gh/task-execution-schemas) (TES) protocol as the protocol for nextflow to dock with the Tencent Healthcare Omics Platform. Compared to the official plugin, we use the  [TES v1.1](https://github.com/ga4gh/task-execution-schemas/releases/tag/v1.1) protocol and utilize some custom fields to adapt to the platform features.

## Feature

- Supports docking with Tencent Cloud COS object storage, allowing direct dependence on Tencent Cloud files in files and configurations.
- Supports docking with Tencent Healthcare Omics Platform for quick access to high-performance elastic computing capabilities (For more information about Tencent Health Genomics Platform, please refer to: [Tencent Healthcare Omics Platform](https://cloud.tencent.com/product/omics)).

## Installation

- Make sure that nextflow is already installed on your system.
- Run the following command to install nf-tencentcloud.
  ```bash
  nextflow plugin install nf-tencentcloud
  ```
  You can install the nf-tencentcloud plugin that meets your current environment.
- You can also specify the plugin in the configuration file for installation.
  ```groovy
  plugins {
      id 'nf-tencentcloud@1.0.0'
  }
  ```
- Or you can use the -plugins command line option.
  ```bash
  nextflow run <pipeline> -plugins nf-tencentcloud@1.0.0
  ```

## Using Tencent Cloud Object Storage

This plugin integrates COS object storage support, and you can conveniently integrate and use Tencent Cloud object storage in nextflow after completing the object storage-related service opening in Tencent Cloud.

Get the key on the [Tencent Cloud CAM console page](https://console.cloud.tencent.com/cam/capi) and follow the steps below:
- Configure the key in the nextflow configuration file.
  ```groovy
  tencentcloud {
      secretId = "your_secret_id"
      secretKey = "your_secret_key"
  }
  ```

- If you use temporary key authorization, you can configure accessToken to enable temporary key.
  ```groovy
  tencentcloud {
      secretId = "your_secret_id"
      secretKey = "your_secret_key"
      accessToken = "your_access_token"
  }
  ```

-After the configuration is complete, you can use COS storage anywhere in the process and configuration, such as:
  ```groovy
  workDir = "cos://test-bucket-1258888888/nextflow/workdir"
  ```

## Using Tencent Healthcare Omics Platform

This plugin integrates the Tencent Healthcare Omics Platform executor, and you can enable sandbox in the omics platform for debugging, or wait for the subsequent opening of the external network access terminal interface, and use the elastic computing resources provided by the Tencent Genomics Platform after making the corresponding configuration on your own computer.

In order to use the Tencent Health Genomics Platform executor, you need to activate and [use the Tencent Healthcare Omics Platform](https://cloud.tencent.com/document/product/1643/86477) on Tencent Cloud, and then follow the steps below:

- You need to obtain the request endpoint and accessToken in the omics platform for interface calling and authentication, and configure them in the nextflow configuration file as follows:
  ```groovy
  tencentcloud {
      omics {
          endpoint = "http://your_endpoint/api"
          accessToken = "your_access_token"
      }
  }
  ```
  > Please note: The accessToken here is used for Tencent Healthcare Omics Platform oauth authentication, which is different from the Tencent Cloud cam temporary authorization accessToken mentioned above, and cannot be mixed.
  
- After completing the relevant configuration, set the executor to `tencentcloud-omics` to use the elastic computing capabilities of the Tencent Health Genomics Platform.
  ```groovy
  process {
      executor = 'tencentcloud-omics'
  }
  ```

## Contributing

For more information about contributing issues or pull requests, see our [nf-tencentcloud Contributing Guide](./CONTRIBUTING.md)。

## License

nf-tencentcloud is under the Apache-2.0. See the [LICENSE](./LICENSE) file for details.

## Support

This plugin is developed by Tencent Healthcare Omics Platform team and can be used upon Tencent Healthcare Omics Platform. Tencent Healthcare Omics Platform, built on a PaaS architecture, provides genomic enterprises with an efficient deployment, flexible scheduling, and user-friendly bioinformatics cloud environment along with various computing resources

For inquiries about product testing and usage, please contact via email: omics@tencent.com