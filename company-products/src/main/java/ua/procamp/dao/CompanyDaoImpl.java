package ua.procamp.dao;

import com.bobocode.util.EntityManagerUtil;
import ua.procamp.model.Company;

import javax.persistence.EntityManagerFactory;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        EntityManagerUtil entityManagerUtil = new EntityManagerUtil(this.entityManagerFactory);

        return entityManagerUtil.performReturningWithinTx(entityManager ->
                entityManager
                        .createQuery("select c from Company c left join fetch c.products where c.id=:id", Company.class)
                        .setParameter("id", id)
                        .getSingleResult());
    }
}
