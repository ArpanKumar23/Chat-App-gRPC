# gRPC Chat Application
This is a simple real time chat application that allows multiple clients to talk to one another built using gRPC for communication between server and clients. The project is fully containerized(hopefully) using Docker and managed using Docker Compose.

## Tech Stack

* **Language:** Kotlin (Java 21)
* **Framework:** Spring Boot 
* **RPC Framework:** gRPC & Protocol Buffers (Protobuf)
* **Database:** PostgreSQL & Spring Data JPA
* **Deployment:** Docker & Docker Compose

## How to Run

1. Clone the repository and then go to the root directory and run the following command. This should build the .jar file.
   
   ```bash
   ./gradlew clean build -x test
   

2. Now you can start the server and boot up a database using docker compose.

   ```bash
   docker compose up --build
   ```
   
3. The GRPC server should have started and will be listening on port 9090 , DB on port 5432.
   To run a client use the first command from the root directory.
* --console=plain stops docker from unneccessarily cluttering the terminal with progress bars.
* To run multiple Clients , open a new terminal and run the command again.
  
  ```bash
   ./gradlew runClient --console=plain
   ```

4. To Close the application:
   
   ```bash
     docker compose down
    ```
   


