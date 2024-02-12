/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package models;

import org.junit.*;

import static org.junit.Assert.*;

import play.test.*;
import play.Application;

import static play.test.Helpers.*;

public class TestTask extends WithApplication {

    protected Application provideApplication() {
        return fakeApplication(inMemoryDatabase());
    }

    @Test
    public void saveAndFind() {
        Task task = new Task();
        task.id = 10L;
        task.name = "Hello";
        task.done = false;
        task.save();

        Task saved = Task.find.byId(10L);
        assertEquals("Hello", saved.name);
    }

}
