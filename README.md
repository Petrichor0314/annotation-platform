# Annotation Platform
## Overview
This is a collaborative text annotation platform built with Jakarta EE, Spring Boot, and MySQL. The platform enables teams to work together on annotating text datasets. It uses Thymeleaf as the templating engine and Tailwind CSS for modern, responsive styling.
## Purpose
The Annotation Platform serves as a centralized environment where teams can collaborate on text annotation tasks:
- **Administrators** can manage users, datasets, and the overall platform
- **Users** focus on the annotation work, contributing to dataset labeling and classification

This platform streamlines the annotation process for natural language processing (NLP) projects, sentiment analysis, content categorization, and other text-based machine learning tasks.
## Features
- User authentication and registration
- Role-based access control (Admin and User roles)
- Dataset management and assignment
- Text annotation interface with customizable labeling options
- Progress tracking and quality metrics
- Email notifications for task assignments and updates
- Responsive design for desktop and mobile use

## Technology Stack
- **Backend**: Jakarta EE, Spring Boot, Spring Data JPA, Spring MVC
- **Frontend**: Thymeleaf templating engine, Tailwind CSS
- **Database**: MySQL
- **Development Tools**: Maven, Docker, npm

## Prerequisites
- JDK 17 or higher
- Docker and Docker Compose
- MySQL database (provided via Docker)
- Maven or Gradle (for building the application)
- npm (for Tailwind CSS processing)

## Getting Started
### Clone the Repository
``` bash
git clone https://github.com/Petrichor0314/annotation-platform.git
cd annotation-platform
```
### Environment Setup
1. Create a `.env` file based on the provided `.env.example`:
``` 
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=annotation
MYSQL_TCP_PORT=3307

SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SPRING_DATASOURCE_URL=jdbc:mysql://db:3307/annotation
SPRING_MAIL_USERNAME=your-email@example.com
SPRING_MAIL_PASSWORD="your-app-password"
```
Make sure to replace the email credentials with your own.
### Running with Docker
The application can be easily run using Docker Compose:
``` bash
docker-compose up -d
```
This will:
- Set up a MySQL database container
- Build and run the Spring Boot application
- Configure the network for communication between containers

### Manual Build and Run
If you prefer to run the application without Docker:
1. Start a MySQL database server (make sure it matches the configuration in the application properties)
2. Install Node.js dependencies and build the Tailwind CSS:
``` bash
npm install
npm run build:css
```
1. Build the application:
``` bash
./mvnw clean package
```
1. Run the application:
``` bash
java -jar target/annotation-platform.jar
```
## User Roles and Functionality
### Administrator Role
Administrators have full control over the platform:
- User management (create, update, delete, assign roles)
- Dataset management (upload, configure, assign to users)
- Annotation review and quality control
- System configuration and monitoring

### Annotator Role
Annotators focus on the annotation tasks:
- View assigned datasets
- Annotate text according to defined guidelines
- Track personal progress
- Submit completed annotations for review

## Project Structure
- `src/main/java/com/mohamedoujdid/annotationplatform/` - Main application code
    - `admin/` - Admin-specific controllers and services
    - `user/` - User model and management
    - Other packages for dataset handling, annotations, etc.

- `src/main/resources/`
    - `application.properties` and `application.yml` - Application configuration
    - `templates/` - Thymeleaf templates
    - `static/` - Static resources including Tailwind CSS

## Database Configuration
The application uses MySQL as its database. The connection details are specified in the `.env` file and `application.properties`.
## Testing the Application
1. Access the application at [http://localhost:8080](http://localhost:8080)
2. Register a new user account or use the default admin account if one is provided
3. Testing as an Administrator:
    - Create and manage users
    - Upload and configure datasets
    - Assign annotation tasks to users
    - Review completed annotations

4. Testing as an Annotator:
    - Log in with an annotator account
    - Navigate to assigned datasets
    - Complete annotation tasks
    - View personal progress

## Frontend Development
The frontend uses:
- **Thymeleaf** for server-side HTML rendering
- **Tailwind CSS** for utility-first styling
- Custom JavaScript for interactive elements

To modify the Tailwind CSS configuration, edit the `tailwind.config.js` file.
## Troubleshooting
- If you encounter database connection issues, ensure your MySQL server is running and accessible
- For Docker-related issues, check if all containers are running with `docker-compose ps`
- Check the application logs with `docker-compose logs app` or in your IDE

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Added a lit feature fr fr'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
