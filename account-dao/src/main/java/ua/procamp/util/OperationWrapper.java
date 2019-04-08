package ua.procamp.util;

import org.hibernate.Session;
import ua.procamp.exception.AccountDaoException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Consumer;
import java.util.function.Function;

public class OperationWrapper {

    private final EntityManagerFactory entityManagerFactory;

    public OperationWrapper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public <T> T handleOperationReturningResult(Function<EntityManager, T> entityManagerConsumer) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.unwrap(Session.class).setDefaultReadOnly(true);
        entityManager.getTransaction().begin();
        try {
            T result = entityManagerConsumer.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing query. Transaction is rolled back", e);
        } finally {
            entityManager.close();
        }
    }

    public void handleOperation(Consumer<EntityManager> op) {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            op.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing query. Transaction is rolled back", e);
        } finally {
            entityManager.close();
        }
    }
}