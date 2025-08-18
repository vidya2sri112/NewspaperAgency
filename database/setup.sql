-- Database Setup Script for News Agency Management System
-- PostgreSQL Database Schema and Initial Data

-- Drop existing tables if they exist (for fresh setup)
DROP TABLE IF EXISTS articles CASCADE;

-- Create articles table
CREATE TABLE articles (
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
);

-- Create indexes for better performance
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_region ON articles(region);
CREATE INDEX idx_articles_language ON articles(language);
CREATE INDEX idx_articles_created_at ON articles(created_at DESC);
CREATE INDEX idx_articles_date ON articles(date DESC);
CREATE INDEX idx_articles_title ON articles USING gin(to_tsvector('english', title));
CREATE INDEX idx_articles_content ON articles USING gin(to_tsvector('english', content));

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_articles_updated_at BEFORE UPDATE
    ON articles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO articles (title, content, region, language, date, status) VALUES 
(
    'Technology Revolution in Indian Cities',
    'India is witnessing a technological revolution with cities like Bangalore, Hyderabad, and Pune emerging as major IT hubs. The adoption of artificial intelligence, machine learning, and blockchain technologies is transforming various sectors including healthcare, education, and finance. Major tech companies are establishing research centers in these cities, creating employment opportunities for millions of skilled professionals. The government''s Digital India initiative has further accelerated this transformation, making technology accessible to rural areas as well.',
    'National',
    'English',
    '2024-01-15',
    'published'
),
(
    'हैदराबाद में नई मेट्रो लाइन का उद्घाटन',
    'हैदराबाद मेट्रो रेल की नई लाइन का आज उद्घाटन हुआ। इससे शहर के यातायात की समस्या में काफी राहत मिलने की उम्मीद है। नई लाइन से लाखों यात्रियों को फायदा होगा और यह शहर के विकास में एक महत्वपूर्ण कदम है। मेट्रो रेल सेवा से पर्यावरण को भी फायदा होगा क्योंकि यह प्रदूषण कम करने में मदद करेगी।',
    'Telangana',
    'Hindi',
    '2024-01-14',
    'published'
),
(
    'సాంకేతిక పరిజ్ఞానంలో కొత్త పురోగతి',
    'కృత్రిమ మేధస్సు రంగంలో భారతీయ కంపెనీలు కొత్త మైలురాయిని సాధించాయి। ఈ పరిజ్ఞానం ఆరోగ్య రంగంలో విప్లవాత్మక మార్పులను తీసుకురానుంది। వైద్య నిర్ధారణ, చికిత్స పద్ధతులు మరియు ఔషధ అభివృద్ధిలో AI యొక్క ఉపయోగం వేగంగా పెరుగుతోంది। తెలుగు రాష్ట్రాలు ఈ రంగంలో ముందుకు వెళ్లుతున్నాయి.',
    'Andhra Pradesh',
    'Telugu',
    '2024-01-13',
    'published'
),
(
    'National Education Policy Implementation Update',
    'The Ministry of Education announced significant progress in implementing the New Education Policy across all states. Universities are adapting their curricula to meet the new guidelines, focusing on interdisciplinary learning and skill development. The policy emphasizes mother tongue education in early years and aims to achieve 100% literacy by 2030. Digital infrastructure in schools is being upgraded to support modern teaching methods.',
    'National',
    'English',
    '2024-01-12',
    'published'
),
(
    'Climate Change Summit Results',
    'World leaders concluded the climate summit with ambitious targets for carbon neutrality. India pledged to increase renewable energy capacity significantly by 2030 and reduce carbon emissions by 45%. The summit highlighted the urgent need for global cooperation in addressing climate change. New funding mechanisms were announced to support developing countries in their transition to clean energy.',
    'National',
    'English',
    '2024-01-11',
    'published'
),
(
    'कर्नाटक में नई IT नीति की घोषणा',
    'कर्नाटक सरकार ने राज्य में सूचना प्रौद्योगिकी क्षेत्र को बढ़ावा देने के लिए नई नीति की घोषणा की है। इस नीति से राज्य में रोजगार के नए अवसर पैदा होंगे और स्टार्टअप इकोसिस्टम को मजबूती मिलेगी। बेंगलुरु को भारत की सिलिकॉन वैली के रूप में स्थापित करने के लिए विशेष प्रावधान किए गए हैं।',
    'Karnataka',
    'Hindi',
    '2024-01-10',
    'published'
),
(
    'तमिलनाडु में स्वास्थ्य सेवा में सुधार',
    'तमिलनाडु सरकार ने राज्य की स्वास्थ्य सेवाओं में व्यापक सुधार की योजना की घोषणा की है। नए अस्पतालों का निर्माण और मौजूदा चिकित्सा सुविधाओं का आधुनिकीकरण किया जाएगा। डिजिटल हेल्थ रिकॉर्ड सिस्टम लागू करके मरीजों को बेहतर सेवा प्रदान की जाएगी।',
    'Tamil Nadu',
    'Hindi',
    '2024-01-09',
    'published'
),
(
    'മലയാളത്തിൽ ഡിജിറ്റൽ വിദ്യാഭ്യാസം',
    'കേരളത്തിൽ മലയാളഭാഷയിൽ ഡിജിറ്റൽ വിദ്യാഭ്യാസ പദ്ധതി ആരംഭിച്ചു. ഈ പദ്ധതിയിലൂടെ സംസ്ഥാനത്തെ എല്ലാ വിദ്യാർത്ഥികൾക്കും ഉയർന്ന നിലവാരമുള്ള വിദ്യാഭ്യാസം ലഭ്യമാകും. ആധുനിക സാങ്കേതികവിദ്യ ഉപയോഗിച്ച് പരമ്പരാഗത വിദ്യാഭ്യാസ രീതികളെ മെച്ചപ്പെടുത്തുന്നതാണ് പദ്ധതിയുടെ ലക്ഷ്യം.',
    'Kerala',
    'Malayalam',
    '2024-01-08',
    'published'
),
(
    'পশ্চিমবঙ্গে নতুন শিল্প নীতি',
    'পশ্চিমবঙ্গ সরকার রাজ্যে নতুন শিল্প নীতি ঘোষণা করেছে। এই নীতির মাধ্যমে রাজ্যে ছোট ও মাঝারি শিল্পের বিকাশ ত্বরান্বিত হবে। বিশেষ করে তথ্যপ্রযুক্তি, স্বাস্থ্যসেবা এবং পর্যটন খাতে নতুন বিনিয়োগ আকর্ষণের জন্য বিশেষ সুবিধা প্রদান করা হবে। এর ফলে হাজার হাজার মানুষের কর্মসংস্থানের সুযোগ সৃষ্টি হবে।',
    'West Bengal',
    'Bengali',
    '2024-01-07',
    'published'
),
(
    'ગુજરાતમાં સૌર ઊર્જા પ્રોજેક્ટ',
    'ગુજરાત સરકારે રાજ્યમાં મોટા પાયે સૌર ઊર્જા પ્રોજેક્ટની શરૂઆત કરી છે. આ પ્રોજેક્ટથી રાજ્યની ઊર્જા જરૂરિયાતોનો મોટો ભાગ પૂરો થશે અને પર્યાવરણને પણ ફાયદો થશે. નવીકરણીય ઊર્જાના ક્ષેત્રમાં ગુજરાત દેશમાં અગ્રણી બનવાની દિશામાં આ મહત્વપૂર્ણ પગલું છે. આ પ્રોજેક્ટથી હજારો લોકોને રોજગારી પણ મળશે.',
    'Gujarat',
    'Gujarati',
    '2024-01-06',
    'published'
),
(
    'Space Technology Advancement in ISRO',
    'Indian Space Research Organisation (ISRO) announced breakthrough developments in satellite technology and space exploration missions. The new satellite series will enhance communication capabilities across remote areas of India. Advanced navigation systems and earth observation capabilities will support various civilian and scientific applications. The mission represents a significant milestone in India''s space program and positions the country as a major player in global space technology.',
    'National',
    'English',
    '2024-01-05',
    'published'
),
(
    'Renewable Energy Initiatives in Rajasthan',
    'Rajasthan government launched ambitious renewable energy projects focusing on solar and wind power generation. The state aims to become carbon neutral by 2030 through these green energy initiatives. Large-scale solar parks and wind farms are being established across the desert regions. These projects will not only meet the state''s energy demands but also contribute surplus clean energy to the national grid.',
    'Rajasthan',
    'English',
    '2024-01-04',
    'published'
),
(
    'Digital Banking Revolution in Rural Areas',
    'Banking sector witnesses significant transformation with digital banking services reaching remote villages across India. Mobile banking applications and digital payment systems have revolutionized financial inclusion in rural areas. Farmers and small business owners now have access to credit facilities and banking services without traveling to distant bank branches. The initiative has been particularly successful in states like Uttar Pradesh, Bihar, and Odisha.',
    'National',
    'English',
    '2024-01-03',
    'published'
),
(
    'Healthcare Infrastructure Development',
    'Union Ministry of Health announced comprehensive healthcare infrastructure development plan for tier-2 and tier-3 cities. The plan includes establishment of new medical colleges, super-specialty hospitals, and primary health centers. Telemedicine facilities will connect rural areas with specialist doctors in major cities. The initiative aims to provide quality healthcare services to every citizen regardless of their geographical location.',
    'National',
    'English',
    '2024-01-02',
    'published'
),
(
    'महाराष्ट्र में कृषि तकनीक का विकास',
    'महाराष्ट्र सरकार ने कृषि क्षेत्र में आधुनिक तकनीक के उपयोग को बढ़ावा देने के लिए नई योजनाओं की घोषणा की है। ड्रोन तकनीक, सटीक कृषि, और स्मार्ट सिंचाई प्रणाली का उपयोग करके किसानों की आय दोगुनी करने का लक्ष्य है। कृषि विज्ञान केंद्रों में किसानों को नई तकनीकों का प्रशिक्षण दिया जा रहा है। यह पहल खासकर महाराष्ट्र के सूखाप्रभावित क्षेत्रों में किसानों के लिए वरदान साबित हो रही है।',
    'Maharashtra',
    'Hindi',
    '2024-01-01',
    'published'
);

-- Create view for published articles with additional metadata
CREATE VIEW published_articles_view AS
SELECT 
    id,
    title,
    author,
    category,
    content,
    region,
    language,
    date,
    created_at,
    updated_at,
    LENGTH(content) as content_length,
    CASE 
        WHEN LENGTH(content) > 500 THEN 'Long'
        WHEN LENGTH(content) > 200 THEN 'Medium'
        ELSE 'Short'
    END as content_type
FROM articles 
WHERE status = 'published'
ORDER BY created_at DESC;

-- Create view for article statistics by region
CREATE VIEW region_statistics AS
SELECT 
    region,
    COUNT(*) as total_articles,
    COUNT(CASE WHEN status = 'published' THEN 1 END) as published_articles,
    COUNT(CASE WHEN status = 'draft' THEN 1 END) as draft_articles,
    AVG(LENGTH(content)) as avg_content_length
FROM articles
GROUP BY region
ORDER BY total_articles DESC;

-- Create view for article statistics by language
CREATE VIEW language_statistics AS
SELECT 
    language,
    COUNT(*) as total_articles,
    COUNT(CASE WHEN status = 'published' THEN 1 END) as published_articles,
    COUNT(CASE WHEN status = 'draft' THEN 1 END) as draft_articles,
    AVG(LENGTH(content)) as avg_content_length
FROM articles
GROUP BY language
ORDER BY total_articles DESC;

-- Insert additional sample data for testing
INSERT INTO articles (title, content, region, language, date, status, author, category) VALUES 
(
    'Artificial Intelligence in Healthcare',
    'AI-powered diagnostic tools are revolutionizing healthcare delivery in India. Machine learning algorithms can now detect diseases with accuracy comparable to experienced doctors. Hospitals are implementing AI-driven systems for patient monitoring, drug discovery, and treatment planning. The technology is particularly beneficial in rural areas where specialist doctors are scarce.',
    'National',
    'English',
    CURRENT_DATE - INTERVAL '1 day',
    'published',
    'Dr. Priya Sharma',
    'Technology'
),
(
    'Sustainable Agriculture Practices',
    'Farmers across India are adopting sustainable agriculture practices to combat climate change and improve crop yields. Organic farming, crop rotation, and integrated pest management are becoming increasingly popular. Government subsidies for organic certification and bio-fertilizers are encouraging more farmers to switch to sustainable methods.',
    'National',
    'English',
    CURRENT_DATE - INTERVAL '2 days',
    'published',
    'Rajesh Kumar',
    'Agriculture'
),
(
    'Draft Article - Future of Transportation',
    'This is a draft article about the future of transportation in smart cities. Content to be developed further with research on electric vehicles, autonomous driving, and public transport innovations.',
    'National',
    'English',
    CURRENT_DATE,
    'draft',
    'Transport Analyst',
    'Transportation'
);

-- Create full-text search function
CREATE OR REPLACE FUNCTION search_articles(search_term TEXT)
RETURNS TABLE (
    id INTEGER,
    title VARCHAR(255),
    content TEXT,
    region VARCHAR(100),
    language VARCHAR(50),
    date DATE,
    status VARCHAR(20),
    rank REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        a.id,
        a.title,
        a.content,
        a.region,
        a.language,
        a.date,
        a.status,
        ts_rank(to_tsvector('english', a.title || ' ' || a.content), plainto_tsquery('english', search_term)) as rank
    FROM articles a
    WHERE to_tsvector('english', a.title || ' ' || a.content) @@ plainto_tsquery('english', search_term)
    ORDER BY rank DESC, a.created_at DESC;
END;
$$ LANGUAGE plpgsql;

-- Grant necessary permissions (if needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_user;

-- Display setup completion message
DO $$
BEGIN
    RAISE NOTICE 'News Agency Database Setup Completed Successfully!';
    RAISE NOTICE 'Tables created: articles';
    RAISE NOTICE 'Indexes created: 7 indexes for optimized queries';
    RAISE NOTICE 'Sample data: % articles inserted', (SELECT COUNT(*) FROM articles);
    RAISE NOTICE 'Views created: published_articles_view, region_statistics, language_statistics';
    RAISE NOTICE 'Functions created: update_updated_at_column, search_articles';
END $$;