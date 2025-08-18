// Admin Panel JavaScript for Newspaper Agency Management
// Handles all admin operations like CRUD for articles

// Global variables for admin functionality
let adminArticles = [];
let adminRegions = [];
let adminLanguages = [];
let editingArticleId = null;

// DOM element references
const loadingSpinner = document.getElementById('loading-spinner');
const adminSearch = document.getElementById('admin-search');
const articlesTableBody = document.getElementById('articles-table-body');
const editModal = document.getElementById('edit-modal');
const datetimeDisplay = document.getElementById('current-datetime');

// Statistics display elements
const totalArticlesEl = document.getElementById('total-articles');
const totalRegionsEl = document.getElementById('total-regions');
const totalLanguagesEl = document.getElementById('total-languages');
const articlesTodayEl = document.getElementById('articles-today');

// Form elements for adding/editing
const createArticleForm = document.getElementById('create-article-form');
const editArticleForm = document.getElementById('edit-article-form');
const articleRegionSelect = document.getElementById('article-region');
const articleLanguageSelect = document.getElementById('article-language');
const editRegionSelect = document.getElementById('edit-region');
const editLanguageSelect = document.getElementById('edit-language');

// Start admin panel when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin panel initializing...');
    initializeAdminPanel();
});

async function initializeAdminPanel() {
    try {
        // Start updating time display
        updateCurrentDateTime();
        setInterval(updateCurrentDateTime, 1000);
        
        // Load all admin data
        await loadAllAdminData();
        
        // Setup event listeners
        setupAdminEventListeners();
        
        // Hide loading spinner
        hideLoadingSpinner();
        
        // Set today's date as default for new articles
        const todayInput = document.getElementById('article-date');
        if (todayInput) {
            todayInput.valueAsDate = new Date();
        }
        
        console.log('Admin panel loaded successfully!');
        
    } catch (error) {
        console.error('Failed to initialize admin panel:', error);
        showNotification('Failed to load admin panel. Please refresh the page.', 'error');
        hideLoadingSpinner();
    }
}

async function loadAllAdminData() {
    try {
        // Load everything in parallel for better performance
        await Promise.all([
            loadAdminArticles(),
            loadAdminFilters()
        ]);
        
        // Update the UI
        updateStatistics();
        renderAdminArticles();
        
    } catch (error) {
        console.error('Failed to load admin data:', error);
        throw error;
    }
}

async function loadAdminArticles() {
    try {
        const response = await fetch('/api/articles.php?action=get_all');
        
        if (response.ok) {
            const data = await response.json();
            
            if (data.success && data.articles) {
                adminArticles = data.articles;
                console.log(`Loaded ${adminArticles.length} articles for admin`);
            } else {
                throw new Error(data.message || 'Failed to load articles');
            }
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error loading admin articles:', error);
        
        // Use sample data for demonstration
        adminArticles = generateSampleAdminArticles();
        showNotification('Using sample data. Check your API connection.', 'warning');
    }
}

function generateSampleAdminArticles() {
    return [
        {
            id: 1,
            title: "Technology Revolution in Indian Cities",
            content: "India is witnessing a technological revolution with cities like Bangalore, Hyderabad, and Pune emerging as major IT hubs. The adoption of artificial intelligence, machine learning, and blockchain technologies is transforming various sectors including healthcare, education, and finance.",
            region: "National",
            language: "English",
            date: "2024-01-15",
            status: "published"
        },
        {
            id: 2,
            title: "हैदराबाद में नई मेट्रो लाइन का उद्घाटन",
            content: "हैदराबाद मेट्रो रेल की नई लाइन का आज उद्घाटन हुआ। इससे शहर के यातायात की समस्या में काफी राहत मिलने की उम्मीद है।",
            region: "Telangana",
            language: "Hindi",
            date: "2024-01-14",
            status: "published"
        },
        {
            id: 3,
            title: "Climate Change Summit Results",
            content: "World leaders concluded the climate summit with ambitious targets for carbon neutrality. India pledged to increase renewable energy capacity significantly by 2030.",
            region: "National",
            language: "English",
            date: "2024-01-11",
            status: "draft"
        }
    ];
}

async function loadAdminFilters() {
    try {
        // Use default filter options for now
        adminRegions = ['National', 'Andhra Pradesh', 'Telangana', 'Karnataka', 'Tamil Nadu', 'Kerala', 'Maharashtra', 'Delhi'];
        adminLanguages = ['English', 'Hindi', 'Telugu', 'Tamil', 'Kannada', 'Malayalam', 'Marathi'];
        
        populateAdminDropdowns();
        
    } catch (error) {
        console.error('Error loading admin filters:', error);
        
        // Use minimal default options
        adminRegions = ['National', 'Andhra Pradesh', 'Telangana'];
        adminLanguages = ['English', 'Hindi', 'Telugu'];
        
        populateAdminDropdowns();
    }
}

function populateAdminDropdowns() {
    // Populate region dropdowns
    populateDropdown(articleRegionSelect, adminRegions, 'Select Region');
    populateDropdown(editRegionSelect, adminRegions, 'Select Region');
    
    // Populate language dropdowns
    populateDropdown(articleLanguageSelect, adminLanguages, 'Select Language');
    populateDropdown(editLanguageSelect, adminLanguages, 'Select Language');
}

function populateDropdown(selectElement, options, placeholder) {
    selectElement.innerHTML = `<option value="">${placeholder}</option>`;
    
    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option;
        optionElement.textContent = option;
        selectElement.appendChild(optionElement);
    });
}

function setupAdminEventListeners() {
    // Search functionality
    adminSearch.addEventListener('input', handleAdminSearch);
    
    // Form submissions
    createArticleForm.addEventListener('submit', handleCreateArticle);
    editArticleForm.addEventListener('submit', handleEditArticle);
    
    // Modal functionality
    editModal.addEventListener('click', (e) => {
        if (e.target === editModal) closeEditModal();
    });
}

function updateStatistics() {
    // Update article statistics
    totalArticlesEl.textContent = adminArticles.length;
    
    // Count articles created today
    const today = new Date().toISOString().split('T')[0];
    const articlesCreatedToday = adminArticles.filter(article => {
        const articleDate = new Date(article.created_at || article.date).toISOString().split('T')[0];
        return articleDate === today;
    }).length;
    articlesTodayEl.textContent = articlesCreatedToday;
    
    // Update region and language counts
    totalRegionsEl.textContent = adminRegions.length;
    totalLanguagesEl.textContent = adminLanguages.length;
}

function handleAdminSearch() {
    const searchTerm = adminSearch.value.toLowerCase();
    renderAdminArticles(searchTerm);
}

function renderAdminArticles(searchTerm = '') {
    const filteredArticles = adminArticles.filter(article => {
        if (!searchTerm) return true;
        
        return article.title.toLowerCase().includes(searchTerm) ||
               article.content.toLowerCase().includes(searchTerm) ||
               article.region.toLowerCase().includes(searchTerm) ||
               article.language.toLowerCase().includes(searchTerm);
    });
    
    if (filteredArticles.length === 0) {
        articlesTableBody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; padding: 2rem; color: var(--text-muted);">
                    No articles found
                </td>
            </tr>
        `;
        return;
    }
    
    articlesTableBody.innerHTML = filteredArticles.map(article => `
        <tr>
            <td>${escapeHtmlText(truncateText(article.title, 50))}</td>
            <td>${escapeHtmlText(article.region)}</td>
            <td>${escapeHtmlText(article.language)}</td>
            <td>${formatDateString(article.date)}</td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-primary btn-small" onclick="editArticle(${article.id})" title="Edit">
                        <i class="fa-solid fa-edit"></i>
                    </button>
                    <button class="btn btn-danger btn-small" onclick="deleteArticle(${article.id})" title="Delete">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function handleCreateArticle(e) {
    e.preventDefault();
    
    // Clear previous errors
    clearFormErrors();
    
    const formData = new FormData(createArticleForm);
    const articleData = {
        title: formData.get('title'),
        content: formData.get('content'),
        region: formData.get('region'),
        language: formData.get('language'),
        date: formData.get('date')
    };
    
    // Validate form
    if (!validateCreateForm(articleData)) {
        return;
    }
    
    try {
        const response = await fetch('/api/articles.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'create',
                ...articleData
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Article created successfully!', 'success');
            clearCreateForm();
            await loadAdminArticles();
            updateStatistics();
            renderAdminArticles();
            showTab('articles');
        } else {
            showNotification(result.message || 'Failed to create article', 'error');
        }
        
    } catch (error) {
        console.error('Error creating article:', error);
        showNotification('Error creating article. Please try again.', 'error');
    }
}

async function editArticle(articleId) {
    const article = adminArticles.find(a => a.id === articleId);
    if (!article) {
        showNotification('Article not found', 'error');
        return;
    }
    
    // Populate edit form
    document.getElementById('edit-article-id').value = article.id;
    document.getElementById('edit-title').value = article.title;
    document.getElementById('edit-content').value = article.content;
    document.getElementById('edit-region').value = article.region;
    document.getElementById('edit-language').value = article.language;
    document.getElementById('edit-date').value = article.date;
    
    editingArticleId = articleId;
    editModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

async function handleEditArticle(e) {
    e.preventDefault();
    
    if (!editingArticleId) return;
    
    // Clear previous errors
    clearEditFormErrors();
    
    const formData = new FormData(editArticleForm);
    const articleData = {
        id: editingArticleId,
        title: formData.get('title'),
        content: formData.get('content'),
        region: formData.get('region'),
        language: formData.get('language'),
        date: formData.get('date')
    };
    
    // Validate form
    if (!validateEditForm(articleData)) {
        return;
    }
    
    try {
        const response = await fetch('/api/articles.php', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'update',
                ...articleData
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Article updated successfully!', 'success');
            closeEditModal();
            await loadAdminArticles();
            updateStatistics();
            renderAdminArticles();
        } else {
            showNotification(result.message || 'Failed to update article', 'error');
        }
        
    } catch (error) {
        console.error('Error updating article:', error);
        showNotification('Error updating article. Please try again.', 'error');
    }
}

async function deleteArticle(articleId) {
    if (!confirm('Are you sure you want to delete this article? This action cannot be undone.')) {
        return;
    }
    
    try {
        const response = await fetch('/api/articles.php', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                action: 'delete',
                id: articleId
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Article deleted successfully!', 'success');
            await loadAdminArticles();
            updateStatistics();
            renderAdminArticles();
        } else {
            showNotification(result.message || 'Failed to delete article', 'error');
        }
        
    } catch (error) {
        console.error('Error deleting article:', error);
        showNotification('Error deleting article. Please try again.', 'error');
    }
}

function closeEditModal() {
    editModal.classList.add('hidden');
    document.body.style.overflow = '';
    editingArticleId = null;
    clearEditFormErrors();
}

function clearCreateForm() {
    createArticleForm.reset();
    clearFormErrors();
    
    // Reset date to today
    const todayInput = document.getElementById('article-date');
    if (todayInput) {
        todayInput.valueAsDate = new Date();
    }
}

function validateCreateForm(data) {
    let isValid = true;
    
    if (!data.title || data.title.trim().length === 0) {
        showError('title-error', 'Title is required');
        isValid = false;
    } else if (data.title.length > 255) {
        showError('title-error', 'Title cannot exceed 255 characters');
        isValid = false;
    }
    
    if (!data.content || data.content.trim().length === 0) {
        showError('content-error', 'Content is required');
        isValid = false;
    } else if (data.content.length > 5000) {
        showError('content-error', 'Content cannot exceed 5000 characters');
        isValid = false;
    }
    
    if (!data.region) {
        showError('region-error', 'Region is required');
        isValid = false;
    }
    
    if (!data.language) {
        showError('language-error', 'Language is required');
        isValid = false;
    }
    
    if (!data.date) {
        showError('date-error', 'Date is required');
        isValid = false;
    } else {
        const selectedDate = new Date(data.date);
        const today = new Date();
        today.setHours(23, 59, 59, 999); // End of today
        
        if (selectedDate > today) {
            showError('date-error', 'Date cannot be in the future');
            isValid = false;
        }
    }
    
    return isValid;
}

function validateEditForm(data) {
    let isValid = true;
    
    if (!data.title || data.title.trim().length === 0) {
        showEditError('edit-title-error', 'Title is required');
        isValid = false;
    } else if (data.title.length > 255) {
        showEditError('edit-title-error', 'Title cannot exceed 255 characters');
        isValid = false;
    }
    
    if (!data.content || data.content.trim().length === 0) {
        showEditError('edit-content-error', 'Content is required');
        isValid = false;
    } else if (data.content.length > 5000) {
        showEditError('edit-content-error', 'Content cannot exceed 5000 characters');
        isValid = false;
    }
    
    if (!data.region) {
        showEditError('edit-region-error', 'Region is required');
        isValid = false;
    }
    
    if (!data.language) {
        showEditError('edit-language-error', 'Language is required');
        isValid = false;
    }
    
    if (!data.date) {
        showEditError('edit-date-error', 'Date is required');
        isValid = false;
    } else {
        const selectedDate = new Date(data.date);
        const today = new Date();
        today.setHours(23, 59, 59, 999); // End of today
        
        if (selectedDate > today) {
            showEditError('edit-date-error', 'Date cannot be in the future');
            isValid = false;
        }
    }
    
    return isValid;
}

function showError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
    }
}

function showEditError(elementId, message) {
    const errorElement = document.getElementById(elementId);
    if (errorElement) {
        errorElement.textContent = message;
    }
}

function clearFormErrors() {
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(element => {
        element.textContent = '';
    });
}

function clearEditFormErrors() {
    const editErrorElements = document.querySelectorAll('#edit-modal .error-message');
    editErrorElements.forEach(element => {
        element.textContent = '';
    });
}

// Tab functionality
function showTab(tabName) {
    // Update tab buttons
    const tabButtons = document.querySelectorAll('.tab-button');
    tabButtons.forEach(button => {
        button.classList.remove('active');
    });
    
    // Update tab panels
    const tabPanels = document.querySelectorAll('.tab-panel');
    tabPanels.forEach(panel => {
        panel.classList.remove('active');
    });
    
    // Show selected tab
    const selectedTab = document.getElementById(`${tabName}-tab`);
    const selectedButton = document.querySelector(`[onclick="showTab('${tabName}')"]`);
    
    if (selectedTab) {
        selectedTab.classList.add('active');
    }
    if (selectedButton) {
        selectedButton.classList.add('active');
    }
}

// Utility functions
function updateCurrentDateTime() {
    const now = new Date();
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };
    datetimeDisplay.textContent = now.toLocaleDateString('en-US', options);
}

function hideLoadingSpinner() {
    loadingSpinner.classList.add('hidden');
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 2rem;
        right: 2rem;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        color: white;
        font-weight: 500;
        z-index: 10000;
        max-width: 400px;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
        transform: translateX(100%);
        transition: transform 0.3s ease-out;
    `;
    
    // Set background color based on type
    const colors = {
        success: '#10b981',
        error: '#ef4444',
        info: '#3b82f6',
        warning: '#f59e0b'
    };
    notification.style.background = colors[type] || colors.info;
    
    notification.textContent = message;
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 10);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 5000);
}

function formatDateString(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength).trim() + '...';
}

function escapeHtmlText(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}