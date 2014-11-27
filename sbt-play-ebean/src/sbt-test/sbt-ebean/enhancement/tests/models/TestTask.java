package models;

import org.junit.*;
import static org.junit.Assert.*;
import play.test.*;
import static play.test.Helpers.*;

public class TestTask extends WithApplication {

    protected FakeApplication provideFakeApplication() {
        return fakeApplication(inMemoryDatabase());
    }

    @Test
    public void saveAndFind() {
        Task task = new Task();
        task.id = 10l;
        task.name = "Hello";
        task.done = false;
        task.save();

        Task saved = Task.find.byId(10l);
        assertEquals("Hello", saved.name);
    }

}
