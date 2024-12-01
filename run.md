# Steps to Run Spring Boot Blog App
## 1. Maven Build the Project
If you have installed Maven on your machine then use the below command:
```
mvn clean package
```
If you haven't insatlled Maven on your machine then use below command:
```
./mvnw clean package
 ```
Note: Go to root directory of the project and execute above command.
## 2. Create a Database
Before running Spring boot blog application, you need to create the MySQL database.

Use the below SQL database to create the MySQL database:
 ```sql
 create database myblog
 ```
Database name - myblog
## 3. Update application.properties
```sql
> set your database and password 
```
## 4. Configure Admin
User below Insert SQL statements to insert records into roles table:
```sql
> use .env file under App Resources to set Admin for App
```
## 5. Start Redis 
User below cmds:
```sql
> brew install redis
> brew services start redis
> redis-cli CONFIG GET port
```
## 6. Run Spring Boot Project
Use below command to run Spring boot application:
 ```
 mvn spring-boot:run
 ```
Once you run Spring boot application, Hibernate will create the database tables autimatically.

Now, Spring boot blog application is ready to use.
