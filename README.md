# Mail Client (Backend Repository)

This repository (`mail-back`) contains the backend service and the complete Kubernetes infrastructure manifests for the **Mail Client** project. The backend is built with Java 21 and Spring Boot, designed to run in a Kubernetes cluster alongside a React frontend, handling authentication, routing, and data processing.

## Tech Stack

* **Core Project:** Mail Client
* **Backend Stack:** Java 21, Spring Boot 3.x, Spring Security (JWT)
* **Databases & Messaging:** PostgreSQL, Apache Kafka
* **Infrastructure & Orchestration:** Docker, Kubernetes (Minikube / K3s)
* **Cloud & IaC:** AWS (EC2, Security Groups), HashiCorp Terraform
* **API Gateway:** Envoy Gateway (Kubernetes Gateway API)
* **CI/CD:** GitHub Actions, Docker Hub

## Architecture Overview

The application operates as a set of containerized microservices within a Kubernetes cluster. 
Incoming traffic is intercepted by the **Envoy Gateway**, which intelligently routes HTTP requests to the React frontend or Spring Boot backend services based on defined `HTTPRoute` rules. Authentication is handled via stateless JWT tokens, completely disabling legacy CSRF and session-based configurations for modern REST API compatibility. The infrastructure also relies on PostgreSQL for persistent state and Apache Kafka for asynchronous event processing.

## Prerequisites

Before you begin, ensure you have the following installed:
* Git
* Docker Desktop
* Minikube (for local testing) and `kubectl`
* Java 21 and Maven (for local development)
* Terraform and AWS CLI (for cloud deployment)

## Quick Start (Local Kubernetes Deployment)

Follow these steps to deploy the entire Mail Client stack locally using Minikube. All manifests are located in the `k8s/` directory.

**1. Clone the repository**
```bash
git clone [https://github.com/grim27/mail-back.git](https://github.com/grim27/mail-back.git)
cd mail-back
```

**2. Start Minikube**
```bash
minikube start
```

**3. Install Envoy Gateway**
Ensure the Gateway API CRDs and Envoy Gateway controller are installed in your cluster.
```bash
helm upgrade -i eg oci://docker.io/envoyproxy/gateway-helm --version v1.1.0 -n envoy-gateway-system --create-namespace
```

**4. Apply Global Configurations and Stateful Workloads**
Before applying, review `k8s/config.yaml` and `k8s/postgres.yaml`. You may need to update the environment variables, database names, and passwords to match your local setup. Once verified, apply them:
```bash
kubectl apply -f k8s/config.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/kafka.yaml
```
*Wait for the Kafka and Postgres pods to reach the `Running` state before proceeding.*

**5. Configure the API Gateway**
Apply the GatewayClass and the Gateway routing definitions:
```bash
kubectl apply -f k8s/gatewayclass.yaml
kubectl apply -f k8s/gateway.yaml
```

**6. Deploy the Application Layer**
Finally, deploy the Spring Boot backend and the React frontend:
```bash
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml
```

**7. Expose the Gateway**
Forward the traffic from the Envoy Gateway to your local machine:
```bash
kubectl port-forward svc/envoy-default-main-gateway-0c7e158b 8080:80 -n envoy-gateway-system
```
*Note: The exact service name for Envoy might differ slightly. Use `kubectl get svc -n envoy-gateway-system` to verify.*

The Mail Client application is now accessible at `http://localhost:8080`. Backend API requests are automatically routed to `http://localhost:8080/api`.

## Cloud Infrastructure (AWS & Terraform)

The project includes a dedicated `aws-terraform` branch containing Infrastructure as Code (IaC) to automatically provision a production-ready cloud environment in AWS.

The Terraform configuration accomplishes the following:
* **Compute:** Provisions an AWS EC2 `t3.micro` instance running Ubuntu 22.04 LTS with a 20GB gp3 root volume in the `il-central-1` region.
* **Kubernetes Cluster:** Automatically bootstraps a single-node **K3s** (Lightweight Kubernetes) cluster upon instance initialization via a `user_data` bash script. It also allocates a 4GB swap file for memory stability.
* **Networking:** Configures an AWS Security Group with strict ingress rules to allow traffic for SSH (22), HTTP (80), HTTPS (443), the Envoy API Gateway (8080), and secure access to the K3s Kubernetes API (6443).

To deploy the cloud infrastructure, checkout the `aws-terraform` branch and execute the standard Terraform workflow:
```bash
git checkout aws-terraform
terraform init
terraform plan
terraform apply
```

## CI/CD Pipeline

This backend repository is integrated with **GitHub Actions**. 
Any push to the `main` branch automatically triggers the `deploy.yml` workflow, which performs the following steps:
1. Checks out the source code.
2. Builds the `.jar` executable using Maven.
3. Builds a new Docker image and tags it with the Git commit hash.
4. Pushes the image to Docker Hub (defaults to the `grim27` namespace).
5. Updates the `k8s/backend.yaml` manifest with the new image tag.
6. Commits the updated manifest back to the repository.

*Note for Forks: If you fork this repository, you must update the Docker Hub namespace in `.github/workflows/deploy.yml` and `k8s/` manifests to match your username. You also need to add your personal `DOCKER_USERNAME` and `DOCKER_PASSWORD` to your GitHub Repository Secrets for the pipeline to work.*

## Security Notes

* **CORS:** Explicitly configured in Spring Security to allow cross-origin requests from the Gateway.
* **Authentication:** Stateless JWT filters ensure secure endpoint access. Registration (`/api/users/register`) and login are exposed via `permitAll()`.
