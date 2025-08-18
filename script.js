// Newspaper Agency Management - Main JavaScript
// Written by a BTech 4th year CSE student
// This handles all the dynamic functionality for the news website

// Global state variables
let newsArticles = [];
let filteredNews = [];
let currentCarouselPosition = 0;
let articlesDisplayed = 6;
let currentPageNum = 1;

// Get DOM elements once on load for better performance
const loadingSpinner = document.getElementById('loading-spinner');
const searchInput = document.getElementById('search-input');
const clearSearchBtn = document.getElementById('clear-search');
const regionFilter = document.getElementById('region-filter');
const languageFilter = document.getElementById('language-filter');
const refreshBtn = document.getElementById('refresh-news');
const articlesContainer = document.getElementById('articles-container');
const noResults = document.getElementById('no-results');
const loadMoreBtn = document.getElementById('load-more');
const featuredCarousel = document.getElementById('featured-carousel');
const prevBtn = document.getElementById('prev-btn');
const nextBtn = document.getElementById('next-btn');
const modal = document.getElementById('article-modal');
const modalContent = document.getElementById('modal-article-content');
const closeModal = document.querySelector('.close-modal');
const datetimeDisplay = document.getElementById('current-datetime');

// Initialize everything when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('News website loading...');
    startApplication();
});

async function startApplication() {
    try {
        // Show current time and update every second
        updateCurrentDateTime();
        setInterval(updateCurrentDateTime, 1000);
        
        // Load all the initial data
        await loadAllInitialData();
        
        // Set up event listeners for user interactions
        setupAllEventHandlers();
        
        // Hide loading spinner
        hideLoadingSpinner();
        
        // Auto-refresh every 10 minutes to keep news fresh
        setInterval(refreshAllNews, 600000);
        
        console.log('News website loaded successfully!');
    } catch (error) {
        console.error('Error loading website:', error);
        showErrorMessage('Failed to load the news website. Please refresh the page.');
        hideLoadingSpinner();
    }
}

async function loadAllInitialData() {
    try {
        // Load articles and filters at the same time for faster loading
        await Promise.all([
            loadNewsArticles(),
            loadFilterOptions()
        ]);
        
        // Display the articles and carousel
        displayArticles();
        setupFeaturedCarousel();
        
    } catch (error) {
        console.error('Error loading initial data:', error);
        throw error;
    }
}

async function loadNewsArticles() {
    try {
        // Try to fetch from the API first
        const response = await fetch('/api/articles.php?action=get&status=published');
        
        if (response.ok) {
            const data = await response.json();
            
            if (data.success && data.articles) {
                newsArticles = data.articles;
                filteredNews = [...newsArticles];
                console.log(`Loaded ${newsArticles.length} articles from API`);
            } else {
                throw new Error('API returned invalid data');
            }
        } else {
            throw new Error(`API error: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error loading articles from API:', error);
        
        // Fallback to sample data for demonstration
        newsArticles = generateSampleArticles();
        filteredNews = [...newsArticles];
        
        showInfoMessage('Using sample articles. Please check your internet connection.');
    }
}

function generateSampleArticles() {
    // Sample articles for demonstration when API is not available
    return [
        {
            id: 1,
            title: "Technology Revolution in Indian Cities",
            content: "India is witnessing a technological revolution with cities like Bangalore, Hyderabad, and Pune emerging as major IT hubs. The adoption of artificial intelligence, machine learning, and blockchain technologies is transforming various sectors including healthcare, education, and finance.",
            region: "National",
            language: "English",
            date: "2024-01-15",
            featured: true
        },
        {
            id: 2,
            title: "हैदराबाद में नई मेट्रो लाइन का उद्घाटन",
            content: "हैदराबाद मेट्रो रेल की नई लाइन का आज उद्घाटन हुआ। इससे शहर के यातायात की समस्या में काफी राहत मिलने की उम्मीद है।",
            region: "Telangana",
            language: "Hindi",
            date: "2024-01-14",
            featured: true
        },
        {
            id: 3,
            title: "సాంకేతిక పరిజ్ఞానంలో కొత్త పురోగతి",
            content: "కృత్రిమ మేధస్సు రంగంలో భారతీయ కంపెనీలు కొత్త మైలురాయిని సాధించాయి. ఈ పరిజ్ఞానం ఆరోగ్య రంగంలో విప్లవాత్మక మార్పులను తీసుకురానుంది.",
            region: "Andhra Pradesh",
            language: "Telugu",
            date: "2024-01-13",
            featured: true
        },
        {
            id: 4,
            title: "National Education Policy Implementation Update",
            content: "The Ministry of Education announced significant progress in implementing the New Education Policy across all states. Universities are adapting their curricula to meet the new guidelines.",
            region: "National",
            language: "English",
            date: "2024-01-12",
            featured: false
        },
        {
            id: 5,
            title: "Climate Change Summit Results",
            content: "World leaders concluded the climate summit with ambitious targets for carbon neutrality. India pledged to increase renewable energy capacity significantly by 2030.",
            region: "National",
            language: "English",
            date: "2024-01-11",
            featured: false
        }
    ];
}

async function loadFilterOptions() {
    try {
        const response = await fetch('/api/articles.php?action=filters');
        
        if (response.ok) {
            const data = await response.json();
            
            if (data.success) {
                setupFilterDropdowns(data.regions || [], data.languages || []);
            } else {
                throw new Error('Failed to load filter options');
            }
        } else {
            throw new Error(`Filter API error: ${response.status}`);
        }
        
    } catch (error) {
        console.error('Error loading filters:', error);
        
        // Use default filter options
        const defaultRegions = ['National', 'Andhra Pradesh', 'Telangana', 'Karnataka', 'Tamil Nadu', 'Kerala'];
        const defaultLanguages = ['English', 'Hindi', 'Telugu', 'Tamil', 'Kannada', 'Malayalam'];
        
        setupFilterDropdowns(defaultRegions, defaultLanguages);
    }
}

function setupFilterDropdowns(regions, languages) {
    // Clear existing options
    regionFilter.innerHTML = '<option value="">All Regions</option>';
    languageFilter.innerHTML = '<option value="">All Languages</option>';
    
    // Add region options
    regions.forEach(region => {
        const option = document.createElement('option');
        option.value = region;
        option.textContent = region;
        regionFilter.appendChild(option);
    });
    
    // Add language options
    languages.forEach(language => {
        const option = document.createElement('option');
        option.value = language;
        option.textContent = language;
        languageFilter.appendChild(option);
    });
}

function setupAllEventHandlers() {
    // Search functionality
    searchInput.addEventListener('input', handleSearchInput);
    clearSearchBtn.addEventListener('click', clearSearchInput);
    
    // Filter functionality
    regionFilter.addEventListener('change', applyAllFilters);
    languageFilter.addEventListener('change', applyAllFilters);
    
    // Refresh button
    refreshBtn.addEventListener('click', refreshAllNews);
    
    // Load more button
    loadMoreBtn.addEventListener('click', loadMoreArticles);
    
    // Carousel navigation
    prevBtn.addEventListener('click', showPreviousCarouselItem);
    nextBtn.addEventListener('click', showNextCarouselItem);
    
    // Modal functionality
    closeModal.addEventListener('click', closeArticleModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeArticleModal();
    });
    
    // Keyboard shortcuts
    document.addEventListener('keydown', handleKeyboardShortcuts);
    
    // Mobile carousel support
    setupMobileCarouselSupport();
}

function handleSearchInput() {
    const searchTerm = searchInput.value.trim();
    
    // Show/hide clear button
    clearSearchBtn.style.display = searchTerm ? 'block' : 'none';
    
    // Debounce search to avoid too many calls
    clearTimeout(window.searchTimeout);
    window.searchTimeout = setTimeout(() => {
        applyAllFilters(searchTerm);
    }, 300);
}

function clearSearchInput() {
    searchInput.value = '';
    clearSearchBtn.style.display = 'none';
    applyAllFilters();
}

function applyAllFilters(searchTerm = null) {
    const search = searchTerm !== null ? searchTerm : searchInput.value.toLowerCase();
    const selectedRegion = regionFilter.value;
    const selectedLanguage = languageFilter.value;
    
    // Filter articles based on criteria
    filteredNews = newsArticles.filter(article => {
        const matchesSearch = !search || 
            article.title.toLowerCase().includes(search) ||
            article.content.toLowerCase().includes(search);
        
        const matchesRegion = !selectedRegion || article.region === selectedRegion;
        const matchesLanguage = !selectedLanguage || article.language === selectedLanguage;
        
        return matchesSearch && matchesRegion && matchesLanguage;
    });
    
    // Reset pagination
    currentPageNum = 1;
    articlesDisplayed = 6;
    
    // Update display
    displayArticles();
    updateLoadMoreButtonVisibility();
}

async function refreshAllNews() {
    refreshBtn.disabled = true;
    refreshBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Refreshing...';
    
    try {
        await loadNewsArticles();
        applyAllFilters();
        showSuccessMessage('News updated successfully!');
    } catch (error) {
        showErrorMessage('Failed to refresh news. Please try again.');
    } finally {
        refreshBtn.disabled = false;
        refreshBtn.innerHTML = '<i class="fa-solid fa-refresh"></i> Refresh News';
    }
}

function displayArticles() {
    const articlesToShow = filteredNews.slice(0, articlesDisplayed);
    
    if (articlesToShow.length === 0) {
        articlesContainer.innerHTML = '';
        noResults.classList.remove('hidden');
        loadMoreBtn.classList.add('hidden');
        return;
    }
    
    noResults.classList.add('hidden');
    
    articlesContainer.innerHTML = articlesToShow.map(article => 
        createArticleCard(article)
    ).join('');
    
    updateLoadMoreButtonVisibility();
}

function createArticleCard(article) {
    const truncatedContent = truncateText(article.content, 150);
    const formattedDate = formatDateString(article.date);
    
    return `
        <div class="article-card" onclick="openArticleModal(${JSON.stringify(article).replace(/"/g, '&quot;')})">
            <div class="article-meta">
                <span class="article-tag tag-region">${escapeHtmlText(article.region)}</span>
                <span class="article-tag tag-language">${escapeHtmlText(article.language)}</span>
                <span class="article-tag tag-date">${formattedDate}</span>
            </div>
            <h3>${escapeHtmlText(article.title)}</h3>
            <p>${escapeHtmlText(truncatedContent)}</p>
            <a href="#" class="read-more" onclick="event.stopPropagation(); openArticleModal(${JSON.stringify(article).replace(/"/g, '&quot;')})">
                Read More <i class="fa-solid fa-arrow-right"></i>
            </a>
        </div>
    `;
}

function setupFeaturedCarousel() {
    const featuredArticles = newsArticles.filter(article => article.featured);
    
    if (featuredArticles.length === 0) {
        featuredCarousel.innerHTML = '<p>No featured articles available</p>';
        return;
    }
    
    featuredCarousel.innerHTML = featuredArticles.map(article => 
        createCarouselItem(article)
    ).join('');
    
    updateCarouselPosition();
    updateCarouselButtons();
}

function createCarouselItem(article) {
    const truncatedContent = truncateText(article.content, 100);
    
    return `
        <div class="carousel-item" onclick="openArticleModal(${JSON.stringify(article).replace(/"/g, '&quot;')})">
            <h3>${escapeHtmlText(article.title)}</h3>
            <p>${escapeHtmlText(truncatedContent)}</p>
        </div>
    `;
}

function showPreviousCarouselItem() {
    const items = featuredCarousel.children;
    if (items.length === 0) return;
    
    currentCarouselPosition = Math.max(0, currentCarouselPosition - 1);
    updateCarouselPosition();
    updateCarouselButtons();
}

function showNextCarouselItem() {
    const items = featuredCarousel.children;
    if (items.length === 0) return;
    
    const maxPosition = Math.max(0, items.length - 1);
    currentCarouselPosition = Math.min(maxPosition, currentCarouselPosition + 1);
    updateCarouselPosition();
    updateCarouselButtons();
}

function updateCarouselPosition() {
    const itemWidth = 320; // Min width + gap
    const translateX = -currentCarouselPosition * (itemWidth + 24);
    featuredCarousel.style.transform = `translateX(${translateX}px)`;
}

function updateCarouselButtons() {
    const items = featuredCarousel.children;
    prevBtn.disabled = currentCarouselPosition === 0;
    nextBtn.disabled = currentCarouselPosition >= items.length - 1;
}

function loadMoreArticles() {
    articlesDisplayed += 6;
    displayArticles();
}

function updateLoadMoreButtonVisibility() {
    if (filteredNews.length > articlesDisplayed) {
        loadMoreBtn.classList.remove('hidden');
    } else {
        loadMoreBtn.classList.add('hidden');
    }
}

function openArticleModal(article) {
    const formattedDate = formatDateString(article.date);
    
    modalContent.innerHTML = `
        <h2>${escapeHtmlText(article.title)}</h2>
        <div class="article-meta">
            <span class="article-tag tag-region">${escapeHtmlText(article.region)}</span>
            <span class="article-tag tag-language">${escapeHtmlText(article.language)}</span>
            <span class="article-tag tag-date">${formattedDate}</span>
        </div>
        <p>${escapeHtmlText(article.content)}</p>
    `;
    
    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closeArticleModal() {
    modal.classList.add('hidden');
    document.body.style.overflow = '';
}

function handleKeyboardShortcuts(e) {
    // Close modal with Escape key
    if (e.key === 'Escape' && !modal.classList.contains('hidden')) {
        closeArticleModal();
    }
    
    // Focus search with Ctrl+F or Cmd+F
    if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault();
        searchInput.focus();
    }
}

function setupMobileCarouselSupport() {
    let startX = 0;
    let currentX = 0;
    let isDragging = false;
    
    function handleTouchStart(e) {
        startX = e.touches[0].clientX;
        isDragging = true;
    }
    
    function handleTouchMove(e) {
        if (!isDragging) return;
        currentX = e.touches[0].clientX;
    }
    
    function handleTouchEnd() {
        if (!isDragging) return;
        isDragging = false;
        
        const diffX = startX - currentX;
        
        if (Math.abs(diffX) > 50) {
            if (diffX > 0) {
                showNextCarouselItem();
            } else {
                showPreviousCarouselItem();
            }
        }
    }
    
    featuredCarousel.addEventListener('touchstart', handleTouchStart);
    featuredCarousel.addEventListener('touchmove', handleTouchMove);
    featuredCarousel.addEventListener('touchend', handleTouchEnd);
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

function showSuccessMessage(message) {
    showNotificationMessage(message, 'success');
}

function showErrorMessage(message) {
    showNotificationMessage(message, 'error');
}

function showInfoMessage(message) {
    showNotificationMessage(message, 'info');
}

function showNotificationMessage(message, type = 'info') {
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

function debounceFunction(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
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