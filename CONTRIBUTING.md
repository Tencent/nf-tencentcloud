# Contributing

We encourage you to promote the development of nf-tencentcloud by submitting issues and pull requests.
​                       

## Issue Submission

#### For contributors

Before submitting an issue, please make sure it meets the following conditions:

- It must be a bug or a feature addition.
- It must be related to nf-tencentcloud.
- You have already searched in the issues and have not found a similar issue or solution.

If you meet the above conditions, you are welcome to submit an issue.

​             

##  Pull request

In addition to hearing your feedback and suggestions, we also hope that you will accept direct help in the form of code and send pull request requests to our GitHub.

Here are the specific steps:

#### Fork the repository

Click the `Fork` button to fork the project repository you want to participate in to your own Github.

#### Clone the forked project

In your own github, find the forked project and git clone it to your local machine.

```bash
$ git clone git@github.com:<yourname>/nf-tencentcloud.git
```

#### Add nf-tencentcloud repository

Connect the forked source repository to the local repository:

```bash
$ git remote add <name> <url>
# For example:
$ git remote add origin git@github.com:Tencent/nf-tencentcloud.git
```

#### Keep in sync with nf-tencentcloud repository

Update the upstream repository:

```bash
$ git pull --rebase <name> <branch>
# Equivalent to the following two commands
$ git fetch <name> <branch>
$ git rebase <name>/<branch>
```