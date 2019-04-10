package ua.procamp.util;

import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("all")
public class OperationWrapper {

    private final EntityManagerFactory entityManagerFactory;

    public OperationWrapper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void handleVoidOperation(Consumer<EntityManager> entityManagerConsumer) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.unwrap(Session.class).setDefaultReadOnly(true);
        try {
            entityManagerConsumer.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public <T> T handleReturningResultOperation(Function<EntityManager, T> entityManagerTFunction) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.unwrap(Session.class).setDefaultReadOnly(true);
        try {
            T result = entityManagerTFunction.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
           entityManager.close();
        }
    }
}