package ua.procamp.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ua.procamp.exception.DaoOperationException;
import ua.procamp.exception.ResourseNotFoundException;
import ua.procamp.model.Programs;

@Slf4j
@RunWith(JUnit4.class)
public class OptimisticLockingTest {

    private OptimisticLocking optimisticLocking;

    @Before
    public void init() {
        this.optimisticLocking = new OptimisticLocking();
    }

    @Test(expected = ResourseNotFoundException.class)
    public void updateEntityWithNullableParam_thenThrowException() {
        Programs programs = null;
        optimisticLocking.updateEntity(programs);
    }

    @Test(expected = DaoOperationException.class)
    public void updateEntityWithWrongId_thenThrowException() {
        Programs programs = Programs.builder().id(-1L).build();
        optimisticLocking.updateEntity(programs);
    }
}