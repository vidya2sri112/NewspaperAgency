# News Agency Management System

A comprehensive news management system with web interface, admin panel, and Java backend with JDBC connectivity.

## 🚀 Quick Start Guide

### 1. **Immediate Access (Already Running)**
- **Main Website**: http://localhost:5000
- **Admin Panel**: http://localhost:5000/admin.html
- **API Endpoint**: http://localhost:5000/api/articles.php

### 2. **Java Backend Setup**
```bash
# Run the automated setup script
./compile_and_run.sh

# Or manually compile and run:
cd java
javac -cp .:../lib/postgresql.jar *.java
java -cp .:../lib/postgresql.jar NewsAgencyManager
```

## 📁 Project Structure

```
📦 News Agency Management System
├── 🌐 Web Frontend
│   ├── index.html          # Main news website
│   ├── admin.html          # Admin management panel
│   ├── styles.css          # Complete styling
│   ├── script.js           # Frontend functionality
│   └── admin.js            # Admin panel logic
│
├── 🔧 Backend API (PHP)
│   ├── api/
│   │   └── articles.php    # REST API for articles
│   └── config/
│       └── database.php    # Database configuration
│
├── ☕ Java Backend (JDBC)
│   ├── Article.java        # Article model class
│   ├── DatabaseConnection.java # JDBC connection manager
│   └── NewsAgencyManager.java  # Console application
│
├── 🗄️ Database
│   └── database/
│       └── setup.sql       # PostgreSQL schema & data
│
└── 🛠️ Setup
    ├── compile_and_run.sh  # Automated setup script
    └── README.md           # This documentation
```

## 🎯 System Features

### **Web Frontend**
- ✅ Responsive design with modern UI
- ✅ Article browsing with search & filters
- ✅ Featured news carousel
- ✅ Multi-language support (English, Hindi, Telugu, etc.)
- ✅ Real-time article loading

### **Admin Panel**
- ✅ Complete CRUD operations for articles
- ✅ Article status management (Draft/Published/Pending/Archived)
- ✅ Statistics dashboard
- ✅ Form validation and error handling
- ✅ Search and filter functionality

### **Java Backend (JDBC)**
- ✅ Full database connectivity with PostgreSQL
- ✅ Article management operations
- ✅ Search and filtering capabilities
- ✅ Console-based interface
- ✅ Singleton database connection pattern
- ✅ Comprehensive error handling

### **Database Features**
- ✅ PostgreSQL with optimized indexes
- ✅ Multi-language article storage
- ✅ Automatic timestamp management
- ✅ Data validation constraints
- ✅ Sample data included

## 🔧 Step-by-Step Execution Process

### **Phase 1: Web Application (Already Running)**
1. **Frontend Access**:
   - Main site: http://localhost:5000
   - Admin panel: http://localhost:5000/admin.html

2. **Test Frontend Features**:
   - Browse articles on main page
   - Use search and filter functionality
   - Navigate featured news carousel
   - Access admin panel for article management

### **Phase 2: Java Backend Setup**
1. **Automated Setup**:
   ```bash
   ./compile_and_run.sh
   ```

2. **Manual Setup** (if needed):
   ```bash
   # Compile Java files
   cd java
   javac -cp .:../lib/postgresql.jar Article.java
   javac -cp .:../lib/postgresql.jar DatabaseConnection.java
   javac -cp .:../lib/postgresql.jar NewsAgencyManager.java
   
   # Run console application
   java -cp .:../lib/postgresql.jar NewsAgencyManager
   ```

### **Phase 3: Java Console Application Usage**
1. **Menu Options**:
   - View All Articles
   - View Published Articles  
   - Create New Article
   - Edit Article
   - Delete Article
   - Search Articles
   - View Statistics
   - Manage Regions & Languages
   - Exit

2. **Sample Operations**:
   - Create articles with validation
   - Update existing articles
   - Search by keywords
   - View comprehensive statistics

## 🗄️ Database Schema

```sql
-- Articles table structure
CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    category VARCHAR(50),
    content TEXT NOT NULL,
    region VARCHAR(100),
    language VARCHAR(50),
    date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🌐 API Endpoints

### **GET Requests**
- `GET /api/articles.php` - Get published articles
- `GET /api/articles.php?action=get_all` - Get all articles (admin)
- `GET /api/articles.php?action=filters` - Get filter options

### **POST Requests**
- `POST /api/articles.php` - Create new article
  ```json
  {
    "action": "create",
    "title": "Article Title",
    "content": "Article content...",
    "region": "Region Name",
    "language": "Language",
    "date": "2024-01-01"
  }
  ```

### **PUT Requests**
- `PUT /api/articles.php` - Update existing article
  ```json
  {
    "action": "update",
    "id": 1,
    "title": "Updated Title",
    "content": "Updated content...",
    "region": "Updated Region",
    "language": "Updated Language",
    "date": "2024-01-01"
  }
  ```

### **DELETE Requests**
- `DELETE /api/articles.php` - Delete article
  ```json
  {
    "action": "delete",
    "id": 1
  }
  ```

## ⚙️ System Requirements

- **Languages**: Java 21+, PHP 8.2+, JavaScript (ES6+)
- **Database**: PostgreSQL
- **Dependencies**: PostgreSQL JDBC driver
- **Environment**: Replit (configured)

## 🧪 Testing the System

### **Web Interface Testing**
1. Open http://localhost:5000
2. Verify article loading and display
3. Test search functionality with keywords
4. Filter articles by region/language
5. Navigate carousel with arrow buttons

### **Admin Panel Testing**
1. Open http://localhost:5000/admin.html
2. Create new articles with form validation
3. Edit existing articles
4. Delete articles with confirmation
5. View statistics dashboard

### **Java Backend Testing**
1. Run: `./compile_and_run.sh`
2. Test database connectivity
3. Perform CRUD operations
4. Search articles by keywords
5. View comprehensive statistics

## 🔍 Troubleshooting

### **Common Issues & Solutions**

1. **Java Compilation Error**:
   ```bash
   # Ensure JDBC driver is available
   find /nix/store -name "postgresql-*.jar"
   ```

2. **Database Connection Issues**:
   - Database is automatically configured with environment variables
   - Connection details are in `config/database.php`

3. **Web Server Issues**:
   - Server runs on port 5000
   - Check workflow status in Replit

4. **API Issues**:
   - Check PHP error logs
   - Verify database connection
   - Test endpoints with browser or curl

## 📊 Sample Data

The system includes multilingual sample articles in:
- **English**: Technology, education, health articles
- **Hindi**: Regional development news
- **Telugu**: Technology advancement news
- **Malayalam**: Digital education initiatives
- **Bengali**: Industrial policy updates
- **Gujarati**: Renewable energy projects

## 🎓 Educational Value

This project demonstrates:
- **Full-stack development** with multiple technologies
- **Database design** with proper indexing and constraints
- **JDBC connectivity** with singleton pattern
- **REST API development** with PHP
- **Frontend-backend integration**
- **Multi-language content management**
- **Responsive web design**
- **CRUD operations** across all tiers

## 📝 Next Steps

1. **Test all components** using the provided URLs
2. **Run Java console application** for backend testing
3. **Create new articles** through both web and console interfaces
4. **Verify data consistency** between interfaces
5. **Explore advanced features** like search and filtering

---

**🎉 Your complete News Agency Management System is ready!**

**Web Interface**: http://localhost:5000  
**Admin Panel**: http://localhost:5000/admin.html  
**Java Console**: `./compile_and_run.sh`"# NewspaperAgency" 
"# NewspaperAgency" 
