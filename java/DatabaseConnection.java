/**
 * Database Connection Class for News Agency Management System
 * Handles PostgreSQL database connections and operations
 * 
 * @author BTech 4th Year CSE Student
 * @version 1.0
 */
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

public class DatabaseConnection {
    // Database connection parameters
    private static final String DB_HOST = System.getenv("PGHOST") != null ? System.getenv("PGHOST") : "localhost";
    private static final String DB_PORT = System.getenv("PGPORT") != null ? System.getenv("PGPORT") : "5432";
    private static final String DB_NAME = System.getenv("PGDATABASE") != null ? System.getenv("PGDATABASE") : "news_agency";
    private static final String DB_USER = System.getenv("PGUSER") != null ? System.getenv("PGUSER") : "postgres";
    private static final String DB_PASSWORD = System.getenv("PGPASSWORD") != null ? System.getenv("PGPASSWORD") : "";
    
    private static final String DB_URL = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);
    
    private Connection connection;
    private static DatabaseConnection instance;
    
    // Private constructor for singleton pattern
    private DatabaseConnection() throws SQLException {
        try {
            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
            
            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("ssl", "false");
            props.setProperty("autoReconnect", "true");
            props.setProperty("characterEncoding", "UTF-8");
            
            // Establish connection
            this.connection = DriverManager.getConnection(DB_URL, props);
            
            System.out.println("Database connection established successfully!");
            
            // Initialize database schema if needed
            initializeDatabase();
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found!", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }
    
    // Get singleton instance
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    // Get connection object
    public Connection getConnection() {
        return connection;
    }
    
    // Test database connection
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    // Initialize database schema
    private void initializeDatabase() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS articles (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                author VARCHAR(100),
                category VARCHAR(50),
                content TEXT NOT NULL,
                region VARCHAR(100),
                language VARCHAR(50),
                date DATE,
                status VARCHAR(20) NOT NULL DEFAULT 'draft' 
                    CHECK (status IN ('draft', 'published', 'pending', 'archived')),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            
            // Create indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_articles_status ON articles(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_articles_region ON articles(region)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_articles_language ON articles(language)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_articles_created_at ON articles(created_at DESC)");
            
            System.out.println("Database schema initialized successfully!");
        }
    }
    
    // CRUD Operations for Articles
    
    /**
     * Create a new article in the database
     * @param article Article to create
     * @return Generated article ID
     * @throws SQLException if operation fails
     */
    public int createArticle(Article article) throws SQLException {
        String sql = """
            INSERT INTO articles (title, author, category, content, region, language, date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getAuthor());
            pstmt.setString(3, article.getCategory());
            pstmt.setString(4, article.getContent());
            pstmt.setString(5, article.getRegion());
            pstmt.setString(6, article.getLanguage());
            pstmt.setDate(7, Date.valueOf(article.getDate()));
            pstmt.setString(8, article.getStatus());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        article.setId(id);
                        return id;
                    }
                }
            }
            
            throw new SQLException("Failed to create article, no ID obtained.");
        }
    }
    
    /**
     * Read an article by ID
     * @param id Article ID
     * @return Article object or null if not found
     * @throws SQLException if operation fails
     */
    public Article getArticleById(int id) throws SQLException {
        String sql = "SELECT * FROM articles WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToArticle(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get all articles with optional filters
     * @param region Filter by region (null for all)
     * @param language Filter by language (null for all)
     * @param status Filter by status (null for all)
     * @return List of articles
     * @throws SQLException if operation fails
     */
    public List<Article> getArticles(String region, String language, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM articles WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        
        if (region != null && !region.trim().isEmpty()) {
            sql.append(" AND region = ?");
            parameters.add(region);
        }
        
        if (language != null && !language.trim().isEmpty()) {
            sql.append(" AND language = ?");
            parameters.add(language);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            parameters.add(status);
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (rs.next()) {
                    articles.add(mapResultSetToArticle(rs));
                }
                return articles;
            }
        }
    }
    
    /**
     * Get published articles only
     * @return List of published articles
     * @throws SQLException if operation fails
     */
    public List<Article> getPublishedArticles() throws SQLException {
        return getArticles(null, null, Article.STATUS_PUBLISHED);
    }
    
    /**
     * Update an existing article
     * @param article Article with updated data
     * @return true if update successful
     * @throws SQLException if operation fails
     */
    public boolean updateArticle(Article article) throws SQLException {
        String sql = """
            UPDATE articles 
            SET title = ?, author = ?, category = ?, content = ?, 
                region = ?, language = ?, date = ?, status = ?, 
                updated_at = CURRENT_TIMESTAMP 
            WHERE id = ?
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, article.getTitle());
            pstmt.setString(2, article.getAuthor());
            pstmt.setString(3, article.getCategory());
            pstmt.setString(4, article.getContent());
            pstmt.setString(5, article.getRegion());
            pstmt.setString(6, article.getLanguage());
            pstmt.setDate(7, Date.valueOf(article.getDate()));
            pstmt.setString(8, article.getStatus());
            pstmt.setInt(9, article.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete an article by ID
     * @param id Article ID to delete
     * @return true if deletion successful
     * @throws SQLException if operation fails
     */
    public boolean deleteArticle(int id) throws SQLException {
        String sql = "DELETE FROM articles WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Search articles by title or content
     * @param searchTerm Search term
     * @return List of matching articles
     * @throws SQLException if operation fails
     */
    public List<Article> searchArticles(String searchTerm) throws SQLException {
        String sql = """
            SELECT * FROM articles 
            WHERE title ILIKE ? OR content ILIKE ? 
            ORDER BY created_at DESC
            """;
        
        String searchPattern = "%" + searchTerm + "%";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Article> articles = new ArrayList<>();
                while (rs.next()) {
                    articles.add(mapResultSetToArticle(rs));
                }
                return articles;
            }
        }
    }
    
    /**
     * Get distinct regions from articles
     * @return List of regions
     * @throws SQLException if operation fails
     */
    public List<String> getDistinctRegions() throws SQLException {
        String sql = "SELECT DISTINCT region FROM articles WHERE region IS NOT NULL ORDER BY region";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<String> regions = new ArrayList<>();
            while (rs.next()) {
                regions.add(rs.getString("region"));
            }
            return regions;
        }
    }
    
    /**
     * Get distinct languages from articles
     * @return List of languages
     * @throws SQLException if operation fails
     */
    public List<String> getDistinctLanguages() throws SQLException {
        String sql = "SELECT DISTINCT language FROM articles WHERE language IS NOT NULL ORDER BY language";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<String> languages = new ArrayList<>();
            while (rs.next()) {
                languages.add(rs.getString("language"));
            }
            return languages;
        }
    }
    
    /**
     * Get article statistics
     * @return Statistics as array [total, published, draft, pending, archived]
     * @throws SQLException if operation fails
     */
    public int[] getArticleStatistics() throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total,
                COUNT(CASE WHEN status = 'published' THEN 1 END) as published,
                COUNT(CASE WHEN status = 'draft' THEN 1 END) as draft,
                COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending,
                COUNT(CASE WHEN status = 'archived' THEN 1 END) as archived
            FROM articles
            """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new int[] {
                    rs.getInt("total"),
                    rs.getInt("published"),
                    rs.getInt("draft"),
                    rs.getInt("pending"),
                    rs.getInt("archived")
                };
            }
        }
        
        return new int[5]; // Return zeros if no data
    }
    
    /**
     * Map ResultSet to Article object
     * @param rs ResultSet from query
     * @return Article object
     * @throws SQLException if mapping fails
     */
    private Article mapResultSetToArticle(ResultSet rs) throws SQLException {
        Article article = new Article();
        
        article.setId(rs.getInt("id"));
        article.setTitle(rs.getString("title"));
        article.setAuthor(rs.getString("author"));
        article.setCategory(rs.getString("category"));
        article.setContent(rs.getString("content"));
        article.setRegion(rs.getString("region"));
        article.setLanguage(rs.getString("language"));
        
        Date date = rs.getDate("date");
        if (date != null) {
            article.setDate(date.toLocalDate());
        }
        
        article.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            article.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            article.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return article;
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Execute custom SQL query (for advanced operations)
     * @param sql SQL query to execute
     * @return ResultSet from query
     * @throws SQLException if operation fails
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    
    /**
     * Execute custom SQL update (for advanced operations)
     * @param sql SQL update to execute
     * @return Number of affected rows
     * @throws SQLException if operation fails
     */
    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
}