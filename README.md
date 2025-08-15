# Starter-Code-Sistema-Distribu-do-de-Monitoramento-Java-

# 📡 Sistema Distribuído de Monitoramento (Java)

Este projeto implementa um sistema distribuído com **eleição de líder** usando o **Algoritmo de Bully** e comunicação via **TCP** e **gRPC**.

---

## 🚀 Tecnologias
- **Java 17+**
- **Maven**
- **gRPC**
- **TCP Sockets**
- **Algoritmo de Bully**

---

## 📂 Estrutura Básica
- **NodeApp.java** → Classe principal de cada nó.
- **BullyAlgorithm.java** → Implementação da eleição de líder.
- **TcpClient / TcpServer** → Comunicação entre nós.
- **GrpcService** → Exposição de dados via gRPC.

---

## ⚙️ Pré-requisitos

Antes de rodar, instale:
- [Java 17+](https://adoptium.net/)
- [Maven](https://maven.apache.org/download.cgi)

---

## 📌 Como Executar

### 1️⃣ Abrir o terminal
No Windows, use **Prompt de Comando** ou **PowerShell**.

---

### 2️⃣ Ir até a pasta do projeto

cd C:\Users\SEU_USUARIO\documentos\Starter-Code-Sistema-Distribu-do-de-Monitoramento-Java

### 3️⃣ Iniciar o primeiro nó

Abra um terminal e execute:

mvn exec:java -Dexec.mainClass="com.example.monitor.NodeApp" -Dexec.args="--id 1 --tcp 7001 --grpc 50051 --peers 127.0.0.1:7002:2"

## Aguarde até aparecer:

[NodeApp] Node 1 iniciado. Portas: TCP=7001 gRPC=50051

### 4️⃣ Iniciar o segundo nó

mvn exec:java -Dexec.mainClass="com.example.monitor.NodeApp" -Dexec.args="--id 2 --tcp 7002 --grpc 50052 --peers 127.0.0.1:7001:1"

### 🛡️ Liberar portas no Windows (se necessário)

netsh advfirewall firewall add rule name="Java TCP Test" dir=in action=allow protocol=TCP localport=7001,7002
