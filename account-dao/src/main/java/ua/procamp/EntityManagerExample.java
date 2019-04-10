package ua.procamp;

import ua.procamp.model.Account;
import ua.procamp.model.Card;
import ua.procamp.util.EntityManagerUtil;
import ua.procamp.util.TestDataGenerator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerExample {
    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("SingleAccountEntityPostgres");

        EntityManagerUtil entityManagerUtil = new EntityManagerUtil(entityManagerFactory);

        Account account = TestDataGenerator.generateAccount();
        entityManagerUtil.performWithinTx(em -> {

            em.persist(account);

            Card card = new Card();
            card.setName("Monobank");
            card.setHolder(account);
            em.persist(card);

            Card card2 = new Card();
            card2.setName("privat");
            card2.setHolder(account);
            em.persist(card2);
        });

        entityManagerUtil.performWithinTx(entityManager -> {
            Account singleResult = entityManager
                    .createQuery("select a from Account a left join fetch a.card", Account.class)
                    .getSingleResult();

            Account found = entityManager.find(Account.class, account.getId());
            System.out.println("I AM HERE");
            found.getCard().stream()
                    .forEach(card -> System.out.println(card.getName()));
        });

        entityManagerFactory.close();
    }

    private static void test(EntityManagerFactory entityManagerFactory) {
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
    }
}