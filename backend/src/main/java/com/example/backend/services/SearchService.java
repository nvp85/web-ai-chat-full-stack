package com.example.backend.services;

import com.example.backend.models.Message;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class SearchService {
    @Autowired
    private EntityManager entityManager;

    @Transactional
    public List<Message> search(String query) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Message> result = searchSession.search(Message.class)
                .where(f -> f.match()
                        .fields("content")
                        .matching(query))
                .fetch(10);
        List<Message> hits = result.hits();
        return hits;
    }
}
