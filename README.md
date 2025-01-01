# PlanPulse - Collaborative Task Management System

PlanPulse is a powerful and intuitive task management platform designed to enhance team collaboration and productivity. Inspired by Jira, it provides users with robust tools to create boards, manage tasks, and collaborate efficiently.

## Features

### Backend

- **Task and Board Management**: Create, update, and organize tasks within customizable boards to streamline workflows.
- **JWT-Based Authentication**: Secure login and session management using JSON Web Tokens.
- **Role-Based Access Control**: Define user roles and permissions for enhanced security and functionality.
- **File Handling**: Upload and manage attachments for tasks to keep all related documents in one place.
- **Deployment Ready**: Containerized with Docker for easy deployment and scalability.
- **NoSQL Database**: Utilizes MongoDB for efficient storage and retrieval of data.

## Tech Stack

- **Back-End**: Spring Boot
- **Front-End**: React (In progress)
- **Database**: MongoDB
- **Authentication**: JWT (JSON Web Tokens)
- **Containerization**: Docker
- **Deployment**: Google Cloud Run or GCP

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

- Java 21 or higher
- npm
- Docker and Docker Compose
- MongoDB

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/NeuralRevenant/plan-pulse.git
cd planpulse
```

### 2. Set Up the Back-End (Spring Boot)

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Configure the application properties in `src/main/resources/application.yml` with your MongoDB connection details.

3. Build the application:
   ```bash
   ./mvnw clean package
   ```

4. Run the back-end server:
   ```bash
   java -jar target/planpulse-backend.jar
   ```

### 3. Run with Docker (Optional)

You can use the given Dockerfile to containerize and run the application if required.

## Usage

1. The backend runs on port 8080 by default if not provided.
2. Sign up or log in with your credentials.
3. Create boards, manage tasks, and collaborate with your team.

## Directory Structure

```
planpulse/
├── backend-code        # Spring Boot back-end code (React code is in progress)
├── Dockerfile
└── README.md
```

## Future Enhancements

- Real-time notifications and activity tracking
- Integration with third-party services (e.g., Slack, Google Drive)
- Advanced task analytics and reporting
- Dark mode in the UI

## Contributing

Contributions are welcome! If you would like to contribute:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -m 'Add feature-name'`).
4. Push to the branch (`git push origin feature-name`).
5. Open a pull request.

## Contact

For questions, suggestions, or collaboration, feel free to reach out:

- **GitHub**: [NeuralRevenant](https://github.com/NeuralRevenant)

---

Hope this helps! 🚀
