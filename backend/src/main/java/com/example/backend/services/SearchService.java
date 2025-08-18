package com.example.backend.services;

import com.example.backend.models.Message;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.List;

@Service
public class SearchService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OpenAiService openAiService;

    @Transactional
    public List<Message> searchVector(String query) {
        float[] queryEmbeddingsVector = openAiService.embed(Collections.singletonList(new Message(query, "user"))).getFirst();
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Message> result = searchSession.search(Message.class)
                .where(f -> f.knn(200)
                        .field("embedding")
                        .matching(queryEmbeddingsVector)
                        .requiredMinimumScore(0.61f)
                )
                .fetch(5);
        List<Message> hits = result.hits();
        return hits;
    }

    @Transactional
    public List<Message> searchText(String query) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Message> result = searchSession.search(Message.class)
                .where(f -> f.match()
                        .fields("content")
                        .matching(query)
                )
                .fetch(5);
        List<Message> hits = result.hits();
        return hits;
    }
}
