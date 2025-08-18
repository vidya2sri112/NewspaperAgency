<?php
/**
 * Database Configuration for News Agency Management System
 * PostgreSQL connection configuration
 */

// Database configuration using environment variables
$host = $_ENV['PGHOST'] ?? 'localhost';
$port = $_ENV['PGPORT'] ?? '5432';
$dbname = $_ENV['PGDATABASE'] ?? 'news_agency';
$username = $_ENV['PGUSER'] ?? 'postgres';
$password = $_ENV['PGPASSWORD'] ?? '';

// PDO options for security and performance
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES => false,
    PDO::ATTR_PERSISTENT => false,
    PDO::ATTR_TIMEOUT => 30
];

/**
 * Get database connection
 * @return PDO Database connection object
 * @throws Exception If connection fails
 */
function getDatabaseConnection() {
    global $host, $port, $dbname, $username, $password, $options;
    
    static $pdo = null;
    
    if ($pdo === null) {
        try {
            $dsn = "pgsql:host=$host;port=$port;dbname=$dbname";
            $pdo = new PDO($dsn, $username, $password, $options);
            
            // Test the connection
            $pdo->query('SELECT 1');
            
            error_log("Database connection established successfully");
            
            // Initialize database schema if needed
            initializeDatabase($pdo);
            
        } catch (PDOException $e) {
            error_log("Database connection failed: " . $e->getMessage());
            throw new Exception("Database connection failed: " . $e->getMessage());
        }
    }
    
    return $pdo;
}

/**
 * Initialize database schema
 * @param PDO $pdo Database connection
 */
function initializeDatabase($pdo) {
    try {
        // Create articles table if it doesn't exist
        $sql = "
        CREATE TABLE IF NOT EXISTS articles (
            id SERIAL PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            author VARCHAR(100),
            category VARCHAR(50),
            content TEXT NOT NULL,
            region VARCHAR(100),
            language VARCHAR(50),
            date DATE,
            status VARCHAR(20) NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'pending', 'archived')),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )";
        
        $pdo->exec($sql);
        
        // Create index for better performance
        $pdo->exec("CREATE INDEX IF NOT EXISTS idx_articles_status ON articles(status)");
        $pdo->exec("CREATE INDEX IF NOT EXISTS idx_articles_region ON articles(region)");
        $pdo->exec("CREATE INDEX IF NOT EXISTS idx_articles_language ON articles(language)");
        $pdo->exec("CREATE INDEX IF NOT EXISTS idx_articles_created_at ON articles(created_at DESC)");
        
        // Insert sample data if table is empty
        insertSampleData($pdo);
        
        error_log("Database schema initialized successfully");
        
    } catch (PDOException $e) {
        error_log("Failed to initialize database: " . $e->getMessage());
    }
}

/**
 * Insert sample data if table is empty
 * @param PDO $pdo Database connection
 */
function insertSampleData($pdo) {
    try {
        // Check if table has data
        $stmt = $pdo->query("SELECT COUNT(*) FROM articles");
        $count = $stmt->fetchColumn();
        
        if ($count == 0) {
            $sampleData = [
                [
                    'title' => 'Technology Revolution in Indian Cities',
                    'content' => 'India is witnessing a technological revolution with cities like Bangalore, Hyderabad, and Pune emerging as major IT hubs. The adoption of artificial intelligence, machine learning, and blockchain technologies is transforming various sectors including healthcare, education, and finance.',
                    'region' => 'National',
                    'language' => 'English',
                    'date' => '2024-01-15',
                    'status' => 'published'
                ],
                [
                    'title' => 'हैदराबाद में नई मेट्रो लाइन का उद्घाटन',
                    'content' => 'हैदराबाद मेट्रो रेल की नई लाइन का आज उद्घाटन हुआ। इससे शहर के यातायात की समस्या में काफी राहत मिलने की उम्मीद है।',
                    'region' => 'Telangana',
                    'language' => 'Hindi',
                    'date' => '2024-01-14',
                    'status' => 'published'
                ],
                [
                    'title' => 'సాంకేతిక పరిజ్ఞానంలో కొత్త పురోగతి',
                    'content' => 'కృత్రిమ మేధస్సు రంగంలో భారతీయ కంపెనీలు కొత్త మైలురాయిని సాధించాయి. ఈ పరిజ్ఞానం ఆరోగ్య రంగంలో విప్లవాత్మక మార్పులను తీసుకురానుంది.',
                    'region' => 'Andhra Pradesh',
                    'language' => 'Telugu',
                    'date' => '2024-01-13',
                    'status' => 'published'
                ],
                [
                    'title' => 'National Education Policy Implementation Update',
                    'content' => 'The Ministry of Education announced significant progress in implementing the New Education Policy across all states. Universities are adapting their curricula to meet the new guidelines.',
                    'region' => 'National',
                    'language' => 'English',
                    'date' => '2024-01-12',
                    'status' => 'published'
                ],
                [
                    'title' => 'Climate Change Summit Results',
                    'content' => 'World leaders concluded the climate summit with ambitious targets for carbon neutrality. India pledged to increase renewable energy capacity significantly by 2030.',
                    'region' => 'National',
                    'language' => 'English',
                    'date' => '2024-01-11',
                    'status' => 'published'
                ]
            ];
            
            $sql = "INSERT INTO articles (title, content, region, language, date, status) 
                    VALUES (:title, :content, :region, :language, :date, :status)";
            
            $stmt = $pdo->prepare($sql);
            
            foreach ($sampleData as $article) {
                $stmt->execute($article);
            }
            
            error_log("Sample data inserted successfully");
        }
        
    } catch (PDOException $e) {
        error_log("Failed to insert sample data: " . $e->getMessage());
    }
}

/**
 * Close database connection
 */
function closeDatabaseConnection() {
    global $pdo;
    $pdo = null;
}
?>