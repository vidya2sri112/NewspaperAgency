/**
 * Article Class for News Agency Management System
 * Represents a news article with all its properties
 * 
 * @author BTech 4th Year CSE Student
 * @version 1.0
 */
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Article {
    // Article properties
    private int id;
    private String title;
    private String author;
    private String category;
    private String content;
    private String region;
    private String language;
    private LocalDate date;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constants for article status
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ARCHIVED = "archived";
    
    // Default constructor
    public Article() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = STATUS_DRAFT;
        this.date = LocalDate.now();
    }
    
    // Constructor with essential fields
    public Article(String title, String content, String region, String language) {
        this();
        this.title = title;
        this.content = content;
        this.region = region;
        this.language = language;
    }
    
    // Constructor with all fields
    public Article(int id, String title, String author, String category, 
                   String content, String region, String language, 
                   LocalDate date, String status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.content = content;
        this.region = region;
        this.language = language;
        this.date = date;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        updateTimestamp();
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
        updateTimestamp();
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
        updateTimestamp();
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
        updateTimestamp();
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
        updateTimestamp();
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
        updateTimestamp();
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
        updateTimestamp();
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        if (isValidStatus(status)) {
            this.status = status;
            updateTimestamp();
        } else {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    private boolean isValidStatus(String status) {
        return STATUS_DRAFT.equals(status) || 
               STATUS_PUBLISHED.equals(status) || 
               STATUS_PENDING.equals(status) || 
               STATUS_ARCHIVED.equals(status);
    }
    
    // Validation methods
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               content != null && !content.trim().isEmpty() &&
               region != null && !region.trim().isEmpty() &&
               language != null && !language.trim().isEmpty() &&
               date != null &&
               isValidStatus(status);
    }
    
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (title == null || title.trim().isEmpty()) {
            errors.append("Title is required. ");
        }
        if (content == null || content.trim().isEmpty()) {
            errors.append("Content is required. ");
        }
        if (region == null || region.trim().isEmpty()) {
            errors.append("Region is required. ");
        }
        if (language == null || language.trim().isEmpty()) {
            errors.append("Language is required. ");
        }
        if (date == null) {
            errors.append("Date is required. ");
        }
        if (!isValidStatus(status)) {
            errors.append("Invalid status. ");
        }
        
        return errors.toString().trim();
    }
    
    // Utility methods
    public String getFormattedDate() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }
    
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getTruncatedContent(int maxLength) {
        if (content == null) return "";
        return content.length() > maxLength ? 
               content.substring(0, maxLength) + "..." : content;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Article{id=%d, title='%s', author='%s', category='%s', " +
            "region='%s', language='%s', date=%s, status='%s', " +
            "createdAt=%s, updatedAt=%s}",
            id, title, author, category, region, language, 
            date, status, createdAt, updatedAt
        );
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Article article = (Article) obj;
        return id == article.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}