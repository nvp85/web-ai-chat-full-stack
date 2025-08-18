package com.example.backend.init;

import com.example.backend.models.Message;
import com.example.backend.models.Chat;
import com.example.backend.repositories.MessageRepository;
import com.example.backend.services.OpenAiService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NoArgsConstructor
public class LuceneDataInitializer implements CommandLineRunner {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OpenAiService openAiService;

    private boolean runBackfill = false;

    @Override
    @Transactional
    public void run(String... args) throws InterruptedException {
        System.out.println("start...");
        if (!runBackfill) return;;
        List<Message> messages = messageRepository.findAll(Sort.by("id"));
        SearchSession searchSession = Search.session(entityManager);

        List<float[]> embeddings = openAiService.embed(messages);
        int i = 0;
        for (Message m : messages) {
            m.setEmbedding(embeddings.get(i));
            searchSession.indexingPlan().addOrUpdate(m);
            i++;
        }
        MassIndexer indexer = searchSession.massIndexer(Chat.class);
        indexer.startAndWait();
        entityManager.flush();
        entityManager.clear();
        System.out.println("finish");
    }

}
