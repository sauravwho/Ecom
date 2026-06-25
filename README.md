# Ecom - Spring Boot Application

This is an e-commerce application built using Spring Boot.

## Technologies Used

*   **Spring Boot**: Framework for building the application.
*   **Java**: Programming language.
*   **Maven/Gradle**: Build automation tool (please specify which one is used in your project).
*   **H2 Database/PostgreSQL/MySQL**: Database (please specify which one is used).
*   **Spring Data JPA**: For database interaction.
*   **Spring Security**: For authentication and authorization (if applicable).
*   **RESTful APIs**: For communication with clients.

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Maven or Gradle (depending on your project setup)
*   Your preferred IDE (IntelliJ IDEA, Eclipse, VS Code)

### Setup and Running the Application

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/sauravwho/Ecom.git
    cd Ecom
    ```

2.  **Configure the database:**
    *   If using an in-memory database like H2, no additional setup is required.
    *   If using an external database (PostgreSQL, MySQL, etc.), update the `src/main/resources/application.properties` or `application.yml` file with your database credentials and connection details.

3.  **Build the project:**
    *   **Using Maven:**
        ```bash
        mvn clean install
        ```
    *   **Using Gradle:**
        ```bash
        ./gradlew clean build
        ```

4.  **Run the application:**
    *   **Using Maven:**
        ```bash
        mvn spring-boot:run
        ```
    *   **Using Gradle:**
        ```bash
        ./gradlew bootRun
        ```
    *   Alternatively, you can run the `main` method in the `EcomApplication.java` file from your IDE.

5.  The application will start on `http://localhost:8080` (default port).