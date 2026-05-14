# Banking Management System

This is a simple console-based Banking Management System project developed in Java using JDBC and MySQL.

## Features

- User registration
- User login
- Create bank account
- Deposit money
- Withdraw money
- Check balance
- Transfer money to another account
- View transaction history
- Change password
- Logout and exit

## Technologies Used

- Java
- JDBC
- MySQL
- Console input/output using Scanner

## Project Structure

```text
BankManagementSystem/
 ┣ src/
 ┃ ┣ db/
 ┃ ┣ model/
 ┃ ┣ dao/
 ┃ ┣ service/
 ┃ ┗ Main.java
 ┣ database/
 ┃ ┗ bank_management.sql
 ┗ README.md
```

## Database Setup

1. Open MySQL.
2. Run the SQL file:

```text
database/bank_management.sql
```

This will create the database, tables, and sample records.

## JDBC Setup

Open:

```text
src/db/DBConnection.java
```

Update your MySQL username and password:

```java
private static final String USERNAME = "root";
private static final String PASSWORD = "your_password";
```

Also make sure MySQL Connector/J jar is added to the classpath.

## How to Run

Compile the project:

```bat
javac -d out src\Main.java src\db\DBConnection.java src\model\User.java src\model\Account.java src\model\Transaction.java src\dao\UserDAO.java src\dao\AccountDAO.java src\dao\TransactionDAO.java src\service\BankingService.java
```

Run the project:

```bat
java -cp out Main
```

If you are using MySQL connector jar from command line, include it in the classpath while compiling and running.

## Sample Login

```text
Email: rahul@example.com
Password: pass123
```

## Note

This project is made for learning purposes and student submission. Passwords are stored in plain text to keep the project simple.
