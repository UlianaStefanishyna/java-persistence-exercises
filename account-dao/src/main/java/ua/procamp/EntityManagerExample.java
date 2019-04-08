package ua.procamp;

import ua.procamp.model.Account;
import ua.procamp.util.TestDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerExample {
    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("SingleAccountEntityH2");

        EntityManager entityManager = entityManagerFactory.createEntityManager(); // start session

        entityManager.getTransaction().begin();

        try {
            Account account = TestDataGenerator.generateAccount(); // state NEW
            System.out.println(account);

            entityManager.persist(account); // state PERSISTENT
            System.out.println(account);

            entityManager.detach(account); // state DETACHED - remove from the session
            entityManager.merge(account); // back to the session -> to PERSISTENT

            Account account1 = entityManager.find(Account.class, account.getId());
            System.out.println(account1);

            entityManager.remove(account1); // state REMOVED
            entityManager.getTransaction().commit();

        } catch (Exception e) {

            entityManager.getTransaction().rollback();

        } finally {

            entityManager.close(); // close session - close physical connection to db
            // account is detached
        }

        entityManagerFactory.close();
    }
}