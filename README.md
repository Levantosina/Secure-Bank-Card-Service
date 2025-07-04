# Secure-Bank-Card-Service

**A Bank Card Management System built using Spring Boot and JWT 
for authentication and authorization. AES encryption is also used 
to securely store confidential card information, as well as for 
password protection. The system allows users to safely manage and 
store their bank card data.**

### **Technologies Used**
- **Spring Boot** – Framework for building REST APIs.
- **Spring Security** – Used to protect endpoints and handle JWT authentication.
- **JWT (JSON Web Tokens)** – Used for user authentication and authorization.
- **AES Encryption** – Ensures secure encryption of card data.
- **Docker** – For containerizing the application and its dependencies.
- **PostgreSQL** – Used for storing data.
- **pgAdmin** – For managing PostgreSQL databases.
- **Docker Compose** – For defining and running multi-container Docker applications.
- **Swagger** – For API documentation.

### **Project Setup**
```bash
  git clone https://github.com/Levantosina/Secure-Bank-Card-Service.git
  ```
  
```bash
  cd bank-card-management 
   ```
```bash
  ./mvnw clean package -DskipTests         
```

```bash
  docker-compose up --build
 ```



### **Application Access**
- pgAdmin: localhost:5050
- Bank Card Service API: localhost:8080
- Swagger UI: localhost:8080/swagger-ui/index.html#/

### **Authentication via Swagger**
- Navigate to **/api/v1/auth/register** to register as a regular user or an admin.

🔸 **Example request for a regular user:**
```
{
  "email": "user@example.com",
  "password": "yourPassword"
  }
```
🔸 **Example request for an administrator:**
```
{
  "email": "user@example.com",
  "password": "yourPassword",
  "role": "ROLE_ADMIN"
  }
```
- Navigate to /api/v1/auth/login
- Provide your credentials
- Example response
```
{
  "token": "eyJhbGciOiJIUzI1NiI6..."
}
```
- Click the "Authorize" button in the upper right corner of the Swagger interface.
- Paste your token in the format: eyJhbGciOiJIUzI1NiIsInR5cCI6...
- Now you are authenticated and can call protected endpoints through Swagger.


### **Architecture and Security**
- **JWT Authentication.**
    JWT is used for user authentication. All protected endpoints require a token,
    which can be obtained through the login endpoint.

- **AES Encryption**
  AES is used to securely store bank card numbers and passwords.

- **Spring Security.**
  Security protects all endpoints. BCryptPasswordEncoder is used 
  for handling passwords.

- **Swagger.**
  Swagger provides an automatically generated UI for interacting with 
  the REST API and testing endpoints conveniently.
