# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a JeecgBoot-based Energy Management System (EMS) - a low-code development platform built on Spring Boot 2.7.18 and Vue 3. The project consists of a Java backend and Vue3 frontend for managing energy consumption, monitoring, and analysis.

**Version**: 3.7.2
**Architecture**: Front-end/back-end separation (Spring Boot + Vue3 + Ant Design Vue)

## Project Structure

```
JeecgBoot/
├── jeecg-boot/                          # Backend (Java/Spring Boot)
│   ├── jeecg-boot-base-core/           # Core framework
│   ├── jeecg-module-system/            # System module
│   │   └── jeecg-system-start/         # Main application entry point
│   ├── jeecg-module-energy/            # Energy management module (custom)
│   └── jeecg-module-demo/              # Demo module
├── jeecgboot-vue3/                      # Frontend (Vue3 + TypeScript + Vite5)
│   ├── src/
│   │   ├── views/                      # Page components
│   │   │   └── EnergyStatistics/       # Energy statistics views
│   │   ├── api/                        # API definitions
│   │   ├── components/                 # Reusable components
│   │   └── store/                      # Pinia state management
└── db/                                  # Database scripts
```

## Development Commands

### Backend (Java/Maven)

**Build the project:**
```bash
cd jeecg-boot
mvn clean install -DskipTests
```

**Run backend (development):**
```bash
cd jeecg-boot/jeecg-module-system/jeecg-system-start
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Or use the provided batch script (Windows):**
```bash
start-backend.bat
```

**Build JAR:**
```bash
cd jeecg-boot/jeecg-module-system/jeecg-system-start
mvn clean package -DskipTests
```

**Run tests:**
```bash
mvn test
```

### Frontend (Vue3/Vite)

**Install dependencies:**
```bash
cd jeecgboot-vue3
pnpm install
# or npm install
```

**Run development server:**
```bash
pnpm dev
# or npm run dev
```

**Or use the provided batch script (Windows):**
```bash
start-frontend.bat
```

**Build for production:**
```bash
pnpm build
# or npm run build
```

**Lint and format:**
```bash
pnpm batch:prettier
```

**Preview production build:**
```bash
pnpm preview
```

## Architecture & Key Concepts

### Backend Architecture

- **Framework**: Spring Boot 2.7.18 with MyBatis-Plus 3.5.3.2
- **Security**: Apache Shiro 1.12.0 + JWT 3.11.0
- **Database**: MySQL 5.7+ (also supports Oracle, PostgreSQL, SQLServer, 达梦, 人大金仓)
- **Connection Pool**: Druid 1.1.22
- **API Documentation**: Swagger/Knife4j
- **Scheduled Tasks**: Quartz (database-backed)
- **Code Generation**: Built-in code generator for CRUD operations

**Module Structure:**
- `jeecg-boot-base-core`: Core framework utilities and base classes
- `jeecg-module-system`: System management (users, roles, permissions, menus)
- `jeecg-module-energy`: Custom energy management module with:
  - InfluxDB integration for time-series data
  - Energy monitoring and statistics
  - Team energy tracking
  - Real-time data monitoring

**Entry Point**: `jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/java/org/jeecg/JeecgSystemApplication.java`

**Configuration Files**:
- `application.yml`: Main configuration
- `application-dev.yml`: Development environment
- `application-prod.yml`: Production environment
- `application-test.yml`: Test environment

**Default Server**: `http://localhost:8080/jeecg-boot`

### Frontend Architecture

- **Framework**: Vue 3.4.19 + TypeScript 4.9.5
- **Build Tool**: Vite 5.2.11
- **UI Library**: Ant Design Vue 4.1.2
- **State Management**: Pinia 2.1.7
- **Router**: Vue Router 4.3.0
- **Charts**: ECharts 5.4.3
- **Table**: vxe-table 4.6.17
- **HTTP Client**: Axios 1.6.7

**Key Frontend Patterns:**
- Composition API (not Options API)
- TypeScript for type safety
- Pinia stores for state management
- Dynamic routing with permission control
- Component-based architecture

**Development Server**: `http://localhost:3100` (default Vite port)

### Database

- **Primary Database**: MySQL 5.7+
- **Time-Series Database**: InfluxDB (for energy data)
- **Migration Tool**: Flyway (enabled by default)
- **Migration Scripts**: `jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/flyway/sql/mysql/`

### Energy Module Specifics

The `jeecg-module-energy` is a custom module for energy management:

**Key Features:**
- Real-time energy monitoring
- Energy consumption statistics
- Team-based energy tracking
- Historical data analysis
- Scheduled data synchronization jobs

**Database Tables:**
- Team information (`team_info`)
- Team dimension relations (`team_dimension_relation`)
- Team shift schedules (`team_shift_schedule`)
- Energy statistics and monitoring tables

## Coding Conventions

### Backend (Java)

- Use Lombok annotations (`@Data`, `@Slf4j`, etc.)
- Follow Spring Boot best practices
- Controllers: RESTful API design with `@RestController`
- Services: Business logic in service layer with `@Service`
- Mappers: MyBatis-Plus for database operations
- Entity naming: Match database table names
- Use `@AutoLog` for operation logging
- API versioning in URL paths

### Frontend (Vue3)

From `.cursorrules`:
- **Always use Composition API** (not Options API)
- Use `const` instead of `function` for component methods
- Event handlers should be prefixed with "handle" (e.g., `handleClick`)
- Use descriptive variable and function names
- **Always use Tailwind classes** for styling (avoid CSS or `<style>` tags)
- Implement accessibility features (tabindex, aria-label, etc.)
- Use early returns for better readability
- Define TypeScript types when possible

## Common Development Workflows

### Adding a New API Endpoint

1. Create entity in `jeecg-module-energy/src/main/java/org/jeecg/modules/energy/entity/`
2. Create mapper interface in `mapper/`
3. Create mapper XML in `src/main/resources/mapper/`
4. Create service interface and implementation in `service/`
5. Create controller in `controller/`
6. Add API definition in frontend `src/api/`

### Creating a New Frontend Page

1. Create view component in `jeecgboot-vue3/src/views/`
2. Add route in `src/router/`
3. Create API service in `src/api/`
4. Add menu configuration in backend system management

### Working with Scheduled Tasks

- Quartz jobs are database-backed
- Job classes extend `QuartzJobBean`
- Configure jobs through admin UI or database
- Jobs are located in `jeecg-module-energy/src/main/java/org/jeecg/modules/energy/job/`

## Environment Requirements

### Backend
- Java 8+ (supports Java 17)
- Maven 3.6+
- MySQL 5.7+ or compatible database
- Redis (for caching and sessions)

### Frontend
- Node.js 20.15.0+ (requires Node 18/20+)
- pnpm (recommended) or npm
- Modern browser with ES6+ support

## Important Notes

- **Character Encoding**: The project uses UTF-8. Windows batch scripts set `chcp 65001` for proper encoding.
- **Port Conflicts**: Backend runs on 8080, frontend on 3100. Ensure ports are available.
- **Database Migrations**: Flyway is enabled by default. New migrations go in `flyway/sql/mysql/`.
- **Code Generator**: Use the built-in code generator for rapid CRUD development.
- **Multi-tenancy**: The platform supports SAAS multi-tenant architecture.
- **Microservices**: Can switch between monolithic and microservices architecture (Spring Cloud Alibaba).

## Testing

Backend tests use JUnit and Spring Boot Test. Run with:
```bash
mvn test
```

Frontend testing setup includes Jest and Vue Test Utils (configured but minimal tests).

## Documentation

- Official Docs: https://help.jeecg.com
- Online Demo: http://boot3.jeecg.com
- API Docs: Available at `/jeecg-boot/doc.html` when backend is running
