package com.example.backend.services;

import com.example.backend.Events.MessagesCreatedEvent;
import com.example.backend.models.Message;
import com.example.backend.repositories.MessageRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository repo;
    private final OpenAiService openAiService;

    private EntityManager entityManager;

    // method - event listener for setting embeddings for new messages
    @Async
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmbeddings(MessagesCreatedEvent messagesCreatedEvent) {
        List<Message> messages = messagesCreatedEvent.getMessages();
        List<float[]> embeddings = openAiService.embed(messages);
        SearchSession searchSession = Search.session(entityManager);
        int i = 0;
        for (Message m : messages) {
            m.setEmbedding(embeddings.get(i));
            searchSession.indexingPlan().addOrUpdate(m);
            i++;
        }
        System.out.println("set embeddings for " + messages.getFirst().getContent());
    }
}
