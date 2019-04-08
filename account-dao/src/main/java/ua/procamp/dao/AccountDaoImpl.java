package ua.procamp.dao;

import ua.procamp.model.Account;
import ua.procamp.util.OperationWrapper;

import javax.persistence.EntityManagerFactory;
import java.util.List;

public class AccountDaoImpl implements AccountDao {

    private final OperationWrapper operationWrapper;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.operationWrapper = new OperationWrapper(emf);
    }

    @Override
    public void save(Account account) {
        this.operationWrapper.handleOperation(em -> em.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return this.operationWrapper.handleOperationReturningResult(em ->
                em.createQuery("select a from Account a where a.id = :id", Account.class)
                        .setParameter("id", id).getSingleResult());
    }

    @Override
    public Account findByEmail(String email) {
        return this.operationWrapper.handleOperationReturningResult(em ->
                em.createQuery("select a from Account a where a.email = :email", Account.class)
                        .setParameter("email", email).getSingleResult());
    }

    @Override
    public List<Account> findAll() {
        return this.operationWrapper.handleOperationReturningResult(em ->
                em.createQuery("select a from Account a", Account.class).getResultList());
    }

    @Override
    public void update(Account account) {
        this.operationWrapper.handleOperation(em -> em.merge(account));
    }

    @Override
    public void remove(Account account) {
        this.operationWrapper.handleOperation(em -> {
            Account merged = em.merge(account);
            em.remove(merged);
        });
    }
}