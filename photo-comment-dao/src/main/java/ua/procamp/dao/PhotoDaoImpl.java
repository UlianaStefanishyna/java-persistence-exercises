package ua.procamp.dao;

import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;
import ua.procamp.util.OperationWrapper;

import javax.persistence.EntityManagerFactory;
import java.util.List;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {

    private final OperationWrapper operationWrapper;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.operationWrapper = new OperationWrapper(entityManagerFactory);
    }

    @Override
    public void save(Photo photo) {
        this.operationWrapper.handleVoidOperation(entityManager -> {
            entityManager.persist(photo);
        });
    }

    @Override
    public Photo findById(long id) {
        validateId(id);
        return this.operationWrapper
                .handleReturningResultOperation(entityManager -> entityManager.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return this.operationWrapper
                .handleReturningResultOperation(entityManager -> entityManager
                        .createQuery("select p from Photo p", Photo.class)
                        .getResultList());
    }

    @Override
    public void remove(Photo photo) {
        this.operationWrapper.handleVoidOperation(entityManager -> {
            Photo managed = entityManager.merge(photo);
            entityManager.remove(managed);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        validateId(photoId);
        this.operationWrapper.handleVoidOperation(entityManager -> {
            Photo photo = entityManager.find(Photo.class, photoId);
            photo.addComment(new PhotoComment(comment));
        });
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new RuntimeException("Id cannot be less or equal to zero");
        }
    }
}
