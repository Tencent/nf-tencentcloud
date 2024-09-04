[English](./README.md) | 简体中文

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="assets/logo-bg-dark.png">
  <source media="(prefers-color-scheme: light)" srcset="assets/logo-bg-light.png">
  <img alt="nf-tencentcloud Logo" src="assets/logo-bg-light.png">
</picture>

## nf-tencentcloud

[![Nextflow](https://img.shields.io/badge/nextflow%20DSL2-%E2%89%A523.04.0-23aa62.svg)](https://www.nextflow.io/)
[![Release](https://img.shields.io/badge/v2.0.0-v?label=realease)](https://github.com/Tencent/nf-tencentcloud/releases/tag/2.0.0)
[![TencentCos](https://img.shields.io/badge/TencentCos-s?logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyAgIHdpZHRoPSIxNnB4IiAgIGhlaWdodD0iMTZweCIgIHZpZXdCb3g9IjAgMCAxNiAxNiIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIj4KICAgIDx0aXRsZT7lr7nosaHlrZjlgqgtMTZweDwvdGl0bGU%2BCiAgICA8ZyBpZD0i5a%2B56LGh5a2Y5YKoLTE2cHgiIHN0cm9rZT0ibm9uZSIgc3Ryb2tlLXdpZHRoPSIxIiBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPgogICAgICAgIDxnIGlkPSLnvJbnu4QiPgogICAgICAgICAgICA8cmVjdCBpZD0iUmVjdGFuZ2xlLUNvcHkiIGZpbGw9IiM0NDQ0NDQiIG9wYWNpdHk9IjAiIHg9IjAiIHk9IjAiIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiI%2BPC9yZWN0PgogICAgICAgICAgICA8cGF0aCBkPSJNOCwwIEwxLDQuMDAxIEwxLDEyLjAwMSBMOCwxNiBMMTUsMTIuMDAxIEwxNSw0LjAwMSBMOCwwIFogTTQuMDQ2LDQuNTYzIEw4LDIuMzA0IEwxMS45NTMsNC41NjMgTDgsNi44NDUgTDQuMDQ2LDQuNTYzIFogTTksOC41NzggTDEyLjk5OSw2LjI2OCBMMTIuOTk5LDEwLjg0IEw5LDEzLjEyNiBMOSw4LjU3OCBaIE0zLDEwLjg0IEwzLDYuMjY4IEw3LDguNTc4IEw3LDEzLjEyNiBMMywxMC44NCBaIiBpZD0iRmlsbCIgZmlsbD0iI0ZGRkZGRiI%2BPC9wYXRoPgogICAgICAgIDwvZz4KICAgIDwvZz4KPC9zdmc%2B&label=run%20with)](https://cloud.tencent.com/product/cos)
[![TencentOmics](https://img.shields.io/badge/TencentOmics-s?logo=data%3Aimage%2Fpng%3Bbase64%2CiVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1%2BjfqAAAAIGNIUk0AAHomAACAhAAA%2BgAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAACYktHRAD%2Fh4%2FMvwAAAAlwSFlzAABYlQAAWJUB2W030wAAAAd0SU1FB%2BgIFgYnAkOH3zYAAAFRSURBVCjPRZExS9txFEWPv7Rqo6QIdZaSTRFFKHZorYagm0umxkKloPkSpVu3QrcOQhEHtS7VTSrZgihBdLJxUAimpYNKRYXUBDwd8o95b3nv3ru88wAQExb9Z0rESWvu2C0AgUZdU6SDt3TSwzwPKXITOTZ72FMvzbmqHjnQ1DFmPFqybnmslpyIlLgh8IwVMgSgnRGS%2FGCZBBAjywqD%2BEn9Ko575q0fHbNs2QGDa%2BqHwAsgD8zxhHUW%2BcMefcxyRx54iRVvfG7CA%2FXMigVnrbpr3JS3lppn1qkCDwD4S414NBP4TRdPqVIA9pniDWkSHHJFknZ%2BBQpAGliiTJJe5shxwQJtpIECjrphxiBO%2B95N9dx3YszXrjuEMR9FWF75Uz1xy5kWqBbqfkvqN3NeWnHkHvX9Pz6r3%2B2xw0X1i22RGQW63bbulIjjVt3zcSPwH3ir7avSxkx9AAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI0LTA4LTIyVDA2OjM4OjQ0KzAwOjAwUNWOEgAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNC0wOC0yMlQwNjozODo0NCswMDowMCGINq4AAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjQtMDgtMjJUMDY6Mzk6MDIrMDA6MDB%2BxUePAAAAAElFTkSuQmCC&label=run%20with)](https://cloud.tencent.com/product/omics)
[![nf-tencentcloud license](https://img.shields.io/github/license/Tencent/nf-tencentcloud.svg?colorB=58bd9f&style=popout)](https://github.com/Tencent/nf-tencentcloud/blob/master/LICENSE)

nf-tencentcloud是一个 nextflow 插件，旨在为 nextflow 工作流引擎添加腾讯云 COS 对象存储和腾讯健康组学平台执行器适配支持，通过该组件，还能实现平台所需的一些细节功能适配，例如 metadata 文件生成等，确保工作流的高效运行与管理。它的设计目标是扩展腾讯云对Nextflow工作流的原生支持，使得用户能通过简单易用的方式调用腾讯云资源运行Nextflow工作流。

我们使用 [Task Execution Schema](https://github.com/ga4gh/task-execution-schemas) (TES) 协议作为nextflow对接腾讯健康组学平台的协议，相比官方插件，我们使用了 [TES v1.1](https://github.com/ga4gh/task-execution-schemas/releases/tag/v1.1) 协议，并且对一部分自定义字段进行了利用以适配平台功能。
 
## Feature

- 支持对接腾讯云 COS 对象存储，可以直接在文件和配置中依赖腾讯云文件。
- 支持腾讯健康组学平台对接,快速获得高性能弹性计算能力。(了解腾讯健康组学平台的更多内容，可参考：[腾讯健康组学平台](https://cloud.tencent.com/product/omics)）

## 安装

- 确保您的系统中已经安装好了 nextflow。
- 执行以下命令安装nf-tencentcloud。
  ```bash
  nextflow plugin install nf-tencentcloud
  ```
  可以安装到符合您当前环境的nf-tencentcloud插件。
- 您也可以在配置文件中指定插件从而进行安装。
  ```groovy
  plugins {
      id 'nf-tencentcloud@2.0.0'
  }
  ```
- 也可以使用 -plugins 命令行参数来指定工具。
  ```bash
  nextflow run <pipeline> -plugins nf-tencentcloud@2.0.0
  ```

## 使用腾讯云对象存储

本插件集成了 COS 对象存储支持，您可以在腾讯云完成对象存储相关服务开通后，方便地在 nextflow 中集成使用腾讯云对象存储。

在[腾讯云 CAM 控制台](https://console.cloud.tencent.com/cam/capi)页面中获取密钥，然后按照以下步骤进行操作：
- 在 nextflow 配置文件中对密钥进行配置。
  ```groovy
  tencentcloud {
      secretId = "your_secret_id"
      secretKey = "your_secret_key"
  }
  ```

- 您如果使用临时密钥授权，可以配置 accessToken 以启用临时密钥。
  ```groovy
  tencentcloud {
      secretId = "your_secret_id"
      secretKey = "your_secret_key"
      accessToken = "your_access_token"
  }
  ```

- 配置完成后，您可以在流程，配置中任意使用 COS 存储，例如：
  ```groovy
  workDir = "cos://test-bucket-1258888888/nextflow/workdir"
  ```

## 使用腾讯健康组学平台

本插件集成了腾讯健康组学平台执行器，您可以在组学平台中开启 sandbox 以进行调试，也可以等待后续开放外网访问终端接口后，在您自己的电脑上进行相应配置后，使用腾讯组学平台提供的弹性计算资源。

为了使用腾讯健康组学平台执行器，您需要在腾讯云上开通并[使用腾讯健康组学平台](https://cloud.tencent.com/document/product/1643/86477)，然后按照以下步骤进行操作：

- 您需要在组学平台获取请求 endpoint 和 accessToken，用于接口调用和鉴权，并在nextflow 配置文件中进行如下配置：
  ```groovy
  tencentcloud {
      omics {
          endpoint = "http://your_endpoint/api"
          accessToken = "your_access_token"
      }
  }
  ```
  > 请注意：这里的 accessToken 用于腾讯健康组学平台 oauth 鉴权，与上文中提到的腾讯云 cam 临时授权 accessToken 不同，不可混用。
  
- 完成相关配置后，设置执行器为`tencentcloud-omics`，即可使用腾讯健康组学平台弹性计算能力。
  ```groovy
  process {
      executor = 'tencentcloud-omics'
  }
  ```

## Contributing

有关贡献问题或拉取请求的更多信息，请参阅我们的 [nf-tencentcloud 贡献指南](./CONTRIBUTING.md)。

## License

nf-tencentcloud 采用 Apache-2.0 许可证。有关详细信息，请参阅 [LICENSE](./LICENSE)。

## Support

该插件由腾讯健康组学平台团队开发，并可在腾讯健康组学平台上使用。腾讯健康组学平台基于 PaaS 架构，为基因组企业提供高效部署、灵活调度、用户友好的生物信息学云环境以及各种计算资源。

如有产品测试和使用方面的咨询，请通过电子邮件联系： omics@tencent.com