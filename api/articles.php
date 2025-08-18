<?php
/**
 * Articles API for News Agency Management System
 * Handles CRUD operations for articles
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once '../config/database.php';

try {
    $pdo = getDatabaseConnection();
    
    $method = $_SERVER['REQUEST_METHOD'];
    $action = $_GET['action'] ?? '';
    
    switch ($method) {
        case 'GET':
            handleGetRequest($pdo, $action);
            break;
            
        case 'POST':
            handlePostRequest($pdo);
            break;
            
        case 'PUT':
            handlePutRequest($pdo);
            break;
            
        case 'DELETE':
            handleDeleteRequest($pdo);
            break;
            
        default:
            respondWithError('Method not allowed', 405);
    }
    
} catch (Exception $e) {
    error_log("API Error: " . $e->getMessage());
    respondWithError('Internal server error', 500);
}

/**
 * Handle GET requests
 */
function handleGetRequest($pdo, $action) {
    switch ($action) {
        case 'get':
            getPublishedArticles($pdo);
            break;
            
        case 'get_all':
            getAllArticles($pdo);
            break;
            
        case 'filters':
            getFilterOptions($pdo);
            break;
            
        default:
            getPublishedArticles($pdo);
    }
}

/**
 * Get published articles for public view
 */
function getPublishedArticles($pdo) {
    try {
        $sql = "SELECT id, title, content, region, language, date, created_at 
                FROM articles 
                WHERE status = 'published' 
                ORDER BY created_at DESC";
        
        $stmt = $pdo->prepare($sql);
        $stmt->execute();
        $articles = $stmt->fetchAll();
        
        // Add featured flag to first 3 articles
        foreach ($articles as $index => &$article) {
            $article['featured'] = $index < 3;
        }
        
        respondWithSuccess(['articles' => $articles]);
        
    } catch (PDOException $e) {
        error_log("Error fetching published articles: " . $e->getMessage());
        respondWithError('Failed to fetch articles');
    }
}

/**
 * Get all articles for admin view
 */
function getAllArticles($pdo) {
    try {
        $sql = "SELECT id, title, content, region, language, date, status, created_at, updated_at 
                FROM articles 
                ORDER BY created_at DESC";
        
        $stmt = $pdo->prepare($sql);
        $stmt->execute();
        $articles = $stmt->fetchAll();
        
        respondWithSuccess(['articles' => $articles]);
        
    } catch (PDOException $e) {
        error_log("Error fetching all articles: " . $e->getMessage());
        respondWithError('Failed to fetch articles');
    }
}

/**
 * Get filter options (regions and languages)
 */
function getFilterOptions($pdo) {
    try {
        // Get distinct regions
        $regionStmt = $pdo->query("SELECT DISTINCT region FROM articles WHERE region IS NOT NULL ORDER BY region");
        $regions = $regionStmt->fetchAll(PDO::FETCH_COLUMN);
        
        // Get distinct languages
        $languageStmt = $pdo->query("SELECT DISTINCT language FROM articles WHERE language IS NOT NULL ORDER BY language");
        $languages = $languageStmt->fetchAll(PDO::FETCH_COLUMN);
        
        respondWithSuccess([
            'regions' => $regions,
            'languages' => $languages
        ]);
        
    } catch (PDOException $e) {
        error_log("Error fetching filter options: " . $e->getMessage());
        respondWithError('Failed to fetch filter options');
    }
}

/**
 * Handle POST requests (Create article)
 */
function handlePostRequest($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input || $input['action'] !== 'create') {
        respondWithError('Invalid request data');
        return;
    }
    
    $title = trim($input['title'] ?? '');
    $content = trim($input['content'] ?? '');
    $region = trim($input['region'] ?? '');
    $language = trim($input['language'] ?? '');
    $date = $input['date'] ?? '';
    
    // Validate required fields
    if (empty($title) || empty($content) || empty($region) || empty($language) || empty($date)) {
        respondWithError('All fields are required');
        return;
    }
    
    try {
        $sql = "INSERT INTO articles (title, content, region, language, date, status) 
                VALUES (:title, :content, :region, :language, :date, 'published')";
        
        $stmt = $pdo->prepare($sql);
        $stmt->execute([
            ':title' => $title,
            ':content' => $content,
            ':region' => $region,
            ':language' => $language,
            ':date' => $date
        ]);
        
        $articleId = $pdo->lastInsertId();
        
        respondWithSuccess([
            'message' => 'Article created successfully',
            'id' => $articleId
        ]);
        
    } catch (PDOException $e) {
        error_log("Error creating article: " . $e->getMessage());
        respondWithError('Failed to create article');
    }
}

/**
 * Handle PUT requests (Update article)
 */
function handlePutRequest($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input || $input['action'] !== 'update') {
        respondWithError('Invalid request data');
        return;
    }
    
    $id = $input['id'] ?? 0;
    $title = trim($input['title'] ?? '');
    $content = trim($input['content'] ?? '');
    $region = trim($input['region'] ?? '');
    $language = trim($input['language'] ?? '');
    $date = $input['date'] ?? '';
    
    // Validate required fields
    if (!$id || empty($title) || empty($content) || empty($region) || empty($language) || empty($date)) {
        respondWithError('All fields are required');
        return;
    }
    
    try {
        $sql = "UPDATE articles 
                SET title = :title, content = :content, region = :region, 
                    language = :language, date = :date, updated_at = CURRENT_TIMESTAMP 
                WHERE id = :id";
        
        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([
            ':id' => $id,
            ':title' => $title,
            ':content' => $content,
            ':region' => $region,
            ':language' => $language,
            ':date' => $date
        ]);
        
        if ($stmt->rowCount() > 0) {
            respondWithSuccess(['message' => 'Article updated successfully']);
        } else {
            respondWithError('Article not found or no changes made');
        }
        
    } catch (PDOException $e) {
        error_log("Error updating article: " . $e->getMessage());
        respondWithError('Failed to update article');
    }
}

/**
 * Handle DELETE requests
 */
function handleDeleteRequest($pdo) {
    $input = json_decode(file_get_contents('php://input'), true);
    
    if (!$input || $input['action'] !== 'delete') {
        respondWithError('Invalid request data');
        return;
    }
    
    $id = $input['id'] ?? 0;
    
    if (!$id) {
        respondWithError('Article ID is required');
        return;
    }
    
    try {
        $sql = "DELETE FROM articles WHERE id = :id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([':id' => $id]);
        
        if ($stmt->rowCount() > 0) {
            respondWithSuccess(['message' => 'Article deleted successfully']);
        } else {
            respondWithError('Article not found');
        }
        
    } catch (PDOException $e) {
        error_log("Error deleting article: " . $e->getMessage());
        respondWithError('Failed to delete article');
    }
}

/**
 * Send success response
 */
function respondWithSuccess($data = []) {
    http_response_code(200);
    echo json_encode(['success' => true] + $data);
    exit();
}

/**
 * Send error response
 */
function respondWithError($message, $statusCode = 400) {
    http_response_code($statusCode);
    echo json_encode([
        'success' => false,
        'message' => $message
    ]);
    exit();
}
?>