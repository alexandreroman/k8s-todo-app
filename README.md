# Yet another Todo app for Kubernetes!

This project shows how to run a simple app on Kubernetes, using a
[Vue.js](https://vuejs.org/) frontend and a
[Spring Boot](https://spring.io/projects/spring-boot)
backend. As you might have guessed, this is a todo app. One more!

![Application architecture](/images/architecture.png)

This app is made of several components:

- **frontend**: a nginx container hosting HTML / JS / CSS files, leveraging Vue.js
- **backend**: a Spring Boot app exposing a simple REST API for getting / storing todo entries
- **database**: a PostgreSQL database instance storing todo entries
- **API gateway**: a Spring Cloud Gateway instance in front of all components, which is the only entry point behind an ingress route

The frontend component is built from a
[TodoMVC implementation in Vue.js](https://todomvc.com/examples/vue/)
written by [Evan You](http://evanyou.me/).

![Application screenshot](/images/app.png)

## Prerequisites

Make sure your cluster is running Kubernetes 1.19+.

Before using this app, you need to deploy these services to your Kubernetes cluster:

- [Spring Cloud Gateway for Kubernetes](https://docs.pivotal.io/scg-k8s/1-0): a Kubernetes operator for the open source Spring Cloud Gateway project, made by VMware Tanzu
- [VMware Tanzu SQL with Postgres](https://postgres-kubernetes.docs.pivotal.io/1-1/index.html): a Kubernetes operator for Postgres, made by VMware Tanzu

These services are part of [Tanzu Advanced](https://tanzu.vmware.com/tanzu/advanced), a modern stack to run Kubernetes at scale for on-premises and public clouds.

You also need to install these tools on your workstation in order to deploy the app:

- [ytt](https://carvel.dev/ytt/): an YAML templating tool, part of the [Carvel](https://carvel.dev) toolsuite, made by VMware Tanzu

## How to use it?

This project relies on ytt to generate Kubernetes manifests which fit your
environment.

In `config-env`, you will find some examples to create configuration overlays,
which override the default configuration defined in `config`.

Create your own configuration overlay for your environment, overriding
[default configuration values](config/values.yml):

```yaml
#@data/values
---
NAMESPACE: my-todo
DOMAIN: k8s-todo.domain.corp
```

Deploy this app with this command:

```shell
ytt -f config -f config-env/my-env | kubectl apply -f -
```

### Enabling external access

The app is not available for external access by default.
Depending on your environment, you may want to enable load balancer
or ingress support, using configuration overlay extensions in `config-ext`.

If you want to access the app using a load balancer, run this command:

```shell
ytt -f config -f config-env/my-env -f config-ext/load-balancer.yml | kubectl apply -f -
```

For ingress support, run this command:

```shell
ytt -f config -f config-env/my-env -f config-ext/ingress.yml | kubectl apply -f -
```

In case you have [cert-manager](https://cert-manager.io/) running in your cluster, you can automatically generate TLS certificates for the ingress route using this command:

```shell
ytt -f config -f config-env/my-env -f config-ext/ingress.yml -f config-ext/ingress-tls.yml | kubectl apply -f -
```

### Enabling `native-image`

This app supports `native-image` with GraalVM for the backend component, powered by
[Spring Native](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/).

A Spring Boot app built with GraalVM `native-image` consumes less memory at runtime,
and starts faster than using a JVM based app.

Deploy the app with `native-image` enabled with this command:

```shell
ytt -f config -f config-env/my-env -v USE_NATIVE_BACKEND=true | kubectl apply -f -
```

You may also want to set the property `USE_NATIVE_BACKEND` in your own configuration file:

```yaml
#@data/values
---
NAMESPACE: my-todo
DOMAIN: k8s-todo.domain.corp
USE_NATIVE_BACKEND: true
```

### Using kapp to deploy the app

[kapp is part of the Carvel toolsuite](https://carvel.dev/kapp), along with `ytt`.
It's a great tool for managing resources (pod, deployment, service, etc.)
for your Kubernetes app: all those elements are regrouped under a single
"application", with resource ordering.

You can combine `ytt` with `kapp` to deploy this app.
For example:

```shell
kapp deploy -a todo-dev -c -f <(ytt -f config -f config-env/dev -f config-ext/ingress.yml -f config-ext/ingress-tls.yml)
```

`kapp` displays the resources that you're about to deploy (with diff support),
and also monitors deployment, unlike `kubectl`: the command will wait on
the resources to become available before terminating.

A great companion to `ytt`!

## Building the app

Use targets defined in [Makefile](Makefile) to build the app on your workstation.

You need to install these tools in order to build this project:

- a Docker daemon
- [Cloud Native Buildpacks CLI](https://github.com/buildpacks/pack) - pack
- NodeJS runtime
- npm
- Python3
- Java SDK 11

Run this command to create new Docker images:

```shell
make package IMAGE_REPOSITORY=harbor.mydomain.corp/todo
```

## Contribute

Contributions are always welcome!

Feel free to open issues & send PR.

## License

Copyright &copy; 2021 [VMware, Inc. or its affiliates](https://vmware.com).

This project is licensed under the [Apache Software License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
