/**
 * News Agency Manager - Main Application Class
 * Console-based interface for managing news articles
 * 
 * @author BTech 4th Year CSE Student
 * @version 1.0
 */
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class NewsAgencyManager {
    private DatabaseConnection dbConnection;
    private Scanner scanner;
    private boolean running;
    
    public NewsAgencyManager() {
        this.scanner = new Scanner(System.in);
        this.running = true;
    }
    
    /**
     * Initialize the application
     */
    public void initialize() {
        try {
            System.out.println("=== News Agency Management System ===");
            System.out.println("Initializing database connection...");
            
            this.dbConnection = DatabaseConnection.getInstance();
            
            if (dbConnection.testConnection()) {
                System.out.println("✓ Database connection successful!");
                System.out.println("Welcome to the News Agency Management System\n");
            } else {
                System.err.println("✗ Database connection failed!");
                System.exit(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Main application loop
     */
    public void run() {
        initialize();
        
        while (running) {
            try {
                displayMainMenu();
                int choice = getIntInput("Enter your choice: ");
                handleMenuChoice(choice);
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        cleanup();
    }
    
    /**
     * Display main menu options
     */
    private void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           NEWS AGENCY MANAGEMENT");
        System.out.println("=".repeat(50));
        System.out.println("1. View All Articles");
        System.out.println("2. View Published Articles");
        System.out.println("3. Create New Article");
        System.out.println("4. Edit Article");
        System.out.println("5. Delete Article");
        System.out.println("6. Search Articles");
        System.out.println("7. View Statistics");
        System.out.println("8. Manage Regions & Languages");
        System.out.println("9. Exit");
        System.out.println("=".repeat(50));
    }
    
    /**
     * Handle menu choice selection
     */
    private void handleMenuChoice(int choice) {
        switch (choice) {
            case 1 -> viewAllArticles();
            case 2 -> viewPublishedArticles();
            case 3 -> createNewArticle();
            case 4 -> editArticle();
            case 5 -> deleteArticle();
            case 6 -> searchArticles();
            case 7 -> viewStatistics();
            case 8 -> manageRegionsAndLanguages();
            case 9 -> exitApplication();
            default -> System.out.println("Invalid choice! Please try again.");
        }
    }
    
    /**
     * View all articles
     */
    private void viewAllArticles() {
        try {
            System.out.println("\n--- ALL ARTICLES ---");
            List<Article> articles = dbConnection.getArticles(null, null, null);
            
            if (articles.isEmpty()) {
                System.out.println("No articles found.");
                return;
            }
            
            displayArticleList(articles, true);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving articles: " + e.getMessage());
        }
    }
    
    /**
     * View published articles only
     */
    private void viewPublishedArticles() {
        try {
            System.out.println("\n--- PUBLISHED ARTICLES ---");
            List<Article> articles = dbConnection.getPublishedArticles();
            
            if (articles.isEmpty()) {
                System.out.println("No published articles found.");
                return;
            }
            
            displayArticleList(articles, false);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving published articles: " + e.getMessage());
        }
    }
    
    /**
     * Create a new article
     */
    private void createNewArticle() {
        try {
            System.out.println("\n--- CREATE NEW ARTICLE ---");
            
            Article article = new Article();
            
            // Get article details from user
            System.out.print("Enter title: ");
            article.setTitle(scanner.nextLine());
            
            System.out.print("Enter author (optional): ");
            String author = scanner.nextLine();
            if (!author.trim().isEmpty()) {
                article.setAuthor(author);
            }
            
            System.out.print("Enter category (optional): ");
            String category = scanner.nextLine();
            if (!category.trim().isEmpty()) {
                article.setCategory(category);
            }
            
            System.out.print("Enter content: ");
            article.setContent(scanner.nextLine());
            
            System.out.print("Enter region: ");
            article.setRegion(scanner.nextLine());
            
            System.out.print("Enter language: ");
            article.setLanguage(scanner.nextLine());
            
            System.out.print("Enter date (YYYY-MM-DD) or press Enter for today: ");
            String dateStr = scanner.nextLine();
            if (dateStr.trim().isEmpty()) {
                article.setDate(LocalDate.now());
            } else {
                try {
                    article.setDate(LocalDate.parse(dateStr));
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Using today's date.");
                    article.setDate(LocalDate.now());
                }
            }
            
            System.out.println("Select status:");
            System.out.println("1. Draft");
            System.out.println("2. Published");
            System.out.println("3. Pending");
            System.out.println("4. Archived");
            
            int statusChoice = getIntInput("Enter status choice (1-4): ");
            switch (statusChoice) {
                case 1 -> article.setStatus(Article.STATUS_DRAFT);
                case 2 -> article.setStatus(Article.STATUS_PUBLISHED);
                case 3 -> article.setStatus(Article.STATUS_PENDING);
                case 4 -> article.setStatus(Article.STATUS_ARCHIVED);
                default -> {
                    System.out.println("Invalid choice. Setting status to Draft.");
                    article.setStatus(Article.STATUS_DRAFT);
                }
            }
            
            // Validate article
            if (!article.isValid()) {
                System.err.println("Article validation failed: " + article.getValidationErrors());
                return;
            }
            
            // Save article
            int articleId = dbConnection.createArticle(article);
            System.out.println("✓ Article created successfully with ID: " + articleId);
            
        } catch (SQLException e) {
            System.err.println("Error creating article: " + e.getMessage());
        }
    }
    
    /**
     * Edit an existing article
     */
    private void editArticle() {
        try {
            System.out.println("\n--- EDIT ARTICLE ---");
            
            int articleId = getIntInput("Enter article ID to edit: ");
            Article article = dbConnection.getArticleById(articleId);
            
            if (article == null) {
                System.out.println("Article not found with ID: " + articleId);
                return;
            }
            
            // Display current article
            System.out.println("\nCurrent Article:");
            displayArticleDetails(article);
            
            System.out.println("\nEnter new values (press Enter to keep current value):");
            
            // Update title
            System.out.print("Title [" + article.getTitle() + "]: ");
            String newTitle = scanner.nextLine();
            if (!newTitle.trim().isEmpty()) {
                article.setTitle(newTitle);
            }
            
            // Update author
            System.out.print("Author [" + (article.getAuthor() != null ? article.getAuthor() : "None") + "]: ");
            String newAuthor = scanner.nextLine();
            if (!newAuthor.trim().isEmpty()) {
                article.setAuthor(newAuthor);
            }
            
            // Update category
            System.out.print("Category [" + (article.getCategory() != null ? article.getCategory() : "None") + "]: ");
            String newCategory = scanner.nextLine();
            if (!newCategory.trim().isEmpty()) {
                article.setCategory(newCategory);
            }
            
            // Update content
            System.out.print("Content [" + article.getTruncatedContent(50) + "]: ");
            String newContent = scanner.nextLine();
            if (!newContent.trim().isEmpty()) {
                article.setContent(newContent);
            }
            
            // Update region
            System.out.print("Region [" + article.getRegion() + "]: ");
            String newRegion = scanner.nextLine();
            if (!newRegion.trim().isEmpty()) {
                article.setRegion(newRegion);
            }
            
            // Update language
            System.out.print("Language [" + article.getLanguage() + "]: ");
            String newLanguage = scanner.nextLine();
            if (!newLanguage.trim().isEmpty()) {
                article.setLanguage(newLanguage);
            }
            
            // Update date
            System.out.print("Date [" + article.getFormattedDate() + "] (YYYY-MM-DD): ");
            String newDateStr = scanner.nextLine();
            if (!newDateStr.trim().isEmpty()) {
                try {
                    article.setDate(LocalDate.parse(newDateStr));
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Keeping current date.");
                }
            }
            
            // Update status
            System.out.println("Current status: " + article.getStatus());
            System.out.println("Select new status:");
            System.out.println("1. Draft");
            System.out.println("2. Published");
            System.out.println("3. Pending");
            System.out.println("4. Archived");
            System.out.println("5. Keep current");
            
            int statusChoice = getIntInput("Enter status choice (1-5): ");
            switch (statusChoice) {
                case 1 -> article.setStatus(Article.STATUS_DRAFT);
                case 2 -> article.setStatus(Article.STATUS_PUBLISHED);
                case 3 -> article.setStatus(Article.STATUS_PENDING);
                case 4 -> article.setStatus(Article.STATUS_ARCHIVED);
                case 5 -> {
                    // Keep current status
                }
                default -> System.out.println("Invalid choice. Keeping current status.");
            }
            
            // Validate and update
            if (!article.isValid()) {
                System.err.println("Article validation failed: " + article.getValidationErrors());
                return;
            }
            
            if (dbConnection.updateArticle(article)) {
                System.out.println("✓ Article updated successfully!");
            } else {
                System.err.println("Failed to update article.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error editing article: " + e.getMessage());
        }
    }
    
    /**
     * Delete an article
     */
    private void deleteArticle() {
        try {
            System.out.println("\n--- DELETE ARTICLE ---");
            
            int articleId = getIntInput("Enter article ID to delete: ");
            Article article = dbConnection.getArticleById(articleId);
            
            if (article == null) {
                System.out.println("Article not found with ID: " + articleId);
                return;
            }
            
            // Display article to confirm
            System.out.println("\nArticle to delete:");
            displayArticleDetails(article);
            
            System.out.print("\nAre you sure you want to delete this article? (y/N): ");
            String confirmation = scanner.nextLine();
            
            if (confirmation.toLowerCase().equals("y") || confirmation.toLowerCase().equals("yes")) {
                if (dbConnection.deleteArticle(articleId)) {
                    System.out.println("✓ Article deleted successfully!");
                } else {
                    System.err.println("Failed to delete article.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting article: " + e.getMessage());
        }
    }
    
    /**
     * Search articles by keyword
     */
    private void searchArticles() {
        try {
            System.out.println("\n--- SEARCH ARTICLES ---");
            
            System.out.print("Enter search term: ");
            String searchTerm = scanner.nextLine();
            
            if (searchTerm.trim().isEmpty()) {
                System.out.println("Search term cannot be empty.");
                return;
            }
            
            List<Article> articles = dbConnection.searchArticles(searchTerm);
            
            if (articles.isEmpty()) {
                System.out.println("No articles found matching: " + searchTerm);
                return;
            }
            
            System.out.println("\nFound " + articles.size() + " article(s) matching: " + searchTerm);
            displayArticleList(articles, true);
            
        } catch (SQLException e) {
            System.err.println("Error searching articles: " + e.getMessage());
        }
    }
    
    /**
     * View article statistics
     */
    private void viewStatistics() {
        try {
            System.out.println("\n--- ARTICLE STATISTICS ---");
            
            int[] stats = dbConnection.getArticleStatistics();
            List<String> regions = dbConnection.getDistinctRegions();
            List<String> languages = dbConnection.getDistinctLanguages();
            
            System.out.println("Total Articles: " + stats[0]);
            System.out.println("Published: " + stats[1]);
            System.out.println("Draft: " + stats[2]);
            System.out.println("Pending: " + stats[3]);
            System.out.println("Archived: " + stats[4]);
            System.out.println("Regions: " + regions.size());
            System.out.println("Languages: " + languages.size());
            
            System.out.println("\nRegions: " + String.join(", ", regions));
            System.out.println("Languages: " + String.join(", ", languages));
            
        } catch (SQLException e) {
            System.err.println("Error retrieving statistics: " + e.getMessage());
        }
    }
    
    /**
     * Manage regions and languages
     */
    private void manageRegionsAndLanguages() {
        try {
            System.out.println("\n--- REGIONS & LANGUAGES ---");
            
            List<String> regions = dbConnection.getDistinctRegions();
            List<String> languages = dbConnection.getDistinctLanguages();
            
            System.out.println("Available Regions (" + regions.size() + "):");
            for (int i = 0; i < regions.size(); i++) {
                System.out.println((i + 1) + ". " + regions.get(i));
            }
            
            System.out.println("\nAvailable Languages (" + languages.size() + "):");
            for (int i = 0; i < languages.size(); i++) {
                System.out.println((i + 1) + ". " + languages.get(i));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving regions and languages: " + e.getMessage());
        }
    }
    
    /**
     * Exit application
     */
    private void exitApplication() {
        System.out.println("Thank you for using News Agency Management System!");
        running = false;
    }
    
    /**
     * Display list of articles
     */
    private void displayArticleList(List<Article> articles, boolean showStatus) {
        System.out.println("\n" + "-".repeat(120));
        System.out.printf("%-5s %-30s %-15s %-15s %-15s %-15s %s%n", 
                         "ID", "Title", "Region", "Language", "Date", 
                         showStatus ? "Status" : "Author", showStatus ? "Author" : "");
        System.out.println("-".repeat(120));
        
        for (Article article : articles) {
            String title = article.getTitle().length() > 30 ? 
                          article.getTitle().substring(0, 27) + "..." : article.getTitle();
            
            if (showStatus) {
                System.out.printf("%-5d %-30s %-15s %-15s %-15s %-15s %s%n",
                                 article.getId(), title, article.getRegion(), 
                                 article.getLanguage(), article.getFormattedDate(),
                                 article.getStatus(), 
                                 article.getAuthor() != null ? article.getAuthor() : "");
            } else {
                System.out.printf("%-5d %-30s %-15s %-15s %-15s %-15s%n",
                                 article.getId(), title, article.getRegion(), 
                                 article.getLanguage(), article.getFormattedDate(),
                                 article.getAuthor() != null ? article.getAuthor() : "");
            }
        }
        
        System.out.println("-".repeat(120));
        System.out.println("Total: " + articles.size() + " articles");
    }
    
    /**
     * Display detailed article information
     */
    private void displayArticleDetails(Article article) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Article ID: " + article.getId());
        System.out.println("Title: " + article.getTitle());
        System.out.println("Author: " + (article.getAuthor() != null ? article.getAuthor() : "Not specified"));
        System.out.println("Category: " + (article.getCategory() != null ? article.getCategory() : "Not specified"));
        System.out.println("Region: " + article.getRegion());
        System.out.println("Language: " + article.getLanguage());
        System.out.println("Date: " + article.getFormattedDate());
        System.out.println("Status: " + article.getStatus());
        System.out.println("Created: " + article.getFormattedCreatedAt());
        System.out.println("-".repeat(80));
        System.out.println("Content:");
        System.out.println(article.getContent());
        System.out.println("=".repeat(80));
    }
    
    /**
     * Get integer input from user with validation
     */
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        if (dbConnection != null) {
            dbConnection.closeConnection();
        }
        if (scanner != null) {
            scanner.close();
        }
    }
    
    /**
     * Main method - Application entry point
     */
    public static void main(String[] args) {
        try {
            NewsAgencyManager manager = new NewsAgencyManager();
            manager.run();
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}