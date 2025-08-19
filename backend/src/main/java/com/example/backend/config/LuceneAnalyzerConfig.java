package com.example.backend.config;

import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

public class LuceneAnalyzerConfig implements LuceneAnalysisConfigurer {
    @Override
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer("english").custom()
                .tokenizer("standard")
                .tokenFilter("lowercase")
                .tokenFilter("snowballPorter") // does stemming (extracts roots from words)
                .param("language", "English")
                .tokenFilter("asciiFolding"); // replaces characters with diacritics ("é", "à") with their ASCII equivalent
    }
}