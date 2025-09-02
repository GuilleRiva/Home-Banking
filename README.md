# 🏦 Home Banking API  

API de **Home Banking** desarrollada en **Java 21 con Spring Boot 3**, diseñada con estándares de nivel bancario.  
El proyecto busca simular el backend de un sistema bancario real, con foco en **seguridad, auditoría, microservicios y escalabilidad hacia un SaaS multi-tenant**.  

---

## 🚀 Características principales  

✅ Autenticación y autorización con **Spring Security + JWT (access & refresh tokens)**  
✅ Manejo de **roles y permisos** (ADMIN, USER, etc.)  
✅ **Gestión de cuentas**: creación, eliminación, consulta por ID, alias o CBU  
✅ **Transferencias** entre cuentas con validaciones y auditoría  
✅ **Préstamos**: simulación y otorgamiento  
✅ **Pagos y métodos de pago**  
✅ **Auditoría completa**: logs de acciones con fecha, IP de origen y tipo de evento  
✅ Documentación interactiva con **Swagger / OpenAPI**  
✅ Contenerización con **Docker & Docker Compose**  
✅ Monitoreo con **Spring Actuator**  

---

## 🛠️ Tecnologías utilizadas  

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)  
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)  
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)  
![MySQL](https://img.shields.io/badge/MySQL_8-005C84?style=for-the-badge&logo=mysql&logoColor=white)  
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)  
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)  
![Lombok](https://img.shields.io/badge/Lombok-AA0000?style=for-the-badge&logo=java&logoColor=white)  
![MapStruct](https://img.shields.io/badge/MapStruct-000000?style=for-the-badge&logo=java&logoColor=white)  

---

## ⚙️ Arquitectura  

- **Capa de repositorio**: JPA / Hibernate  
- **Capa de servicio**: lógica de negocio, validaciones y reglas bancarias  
- **Capa de controlador**: endpoints REST documentados con Swagger  
- **Seguridad**:  
  - JWT con refresh tokens  
  - Revocación de tokens  
  - Control de roles en endpoints  
  - Auditoría de eventos críticos  

---

## 📌 Lo que viene  

🔹 Escalado a **arquitectura de microservicios**  
🔹 Implementación de **multi-tenancy (SaaS)**:  
   - *Shared schema con tenantId* (primera fase)  
   - Posible evolución a *Database per tenant*  
🔹 Integración de **n8n** para automatizaciones bancarias  
🔹 Despliegue en **Kubernetes** con monitoreo avanzado (**Prometheus + Grafana**)  
🔹 Seguridad reforzada: rate limiting, bloqueo de IPs maliciosas, HTTPS obligatorio, almacenamiento seguro de secretos  

---

## ▶️ Cómo correr el proyecto  

### Requisitos previos  
- Docker & Docker Compose  
- Java 21  
- Maven  

### Pasos  
```bash
# Clonar el repositorio
git clone https://github.com/TU_USUARIO/home-banking-api.git

# Construir el proyecto
mvn clean package -DskipTests

# Levantar con Docker Compose
docker-compose up -d
