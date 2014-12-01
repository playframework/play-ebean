package javaguide.ebean;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;
import org.junit.*;
import play.db.ebean.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.test.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class JavaEbean extends WithApplication {

    @Override
    protected FakeApplication provideFakeApplication() {
        Map<String, String> config = new HashMap<String, String>();
        config.putAll(inMemoryDatabase());
        config.put("ebean.default", "javaguide.ebean.Task");
        return fakeApplication(config);
    }

    @Test
    public void taskOperations() {
        createTask();

        //#operations
        // Find all tasks
        List<Task> tasks = Task.find.all();

        // Find a task by ID
        Task anyTask = Task.find.byId(34L);

        // Delete a task by ID
        Task.find.ref(34L).delete();

        // More complex task query
        List<Task> cocoTasks = Task.find.where()
                .ilike("name", "%coco%")
                .orderBy("dueDate asc")
                .findPagedList(1, 25)
                .getList();
        //#operations

        assertThat(tasks.size(), equalTo(1));
        assertThat(tasks.get(0).name, equalTo("coco"));
        assertThat(anyTask.name, equalTo("coco"));
        assertThat(cocoTasks.size(), equalTo(0));
    }

    @Test
    public void transactionExplanation() {
        createTask();

        //#transaction
        // Created implicit transaction
        Task task = Task.find.byId(34L);
        // Transaction commited or rollbacked

        task.done = true;

        // Created implicit transaction
        task.save();
        // Transaction commited or rollbacked
        //#transaction

        assertThat(Task.find.byId(34L).done, is(true));
    }

    @Test
    public void txRunnable() {

        createTask();

        //#txrunnable
        //###insert: import com.avaje.ebean.*;

        //###insert: ...

        Ebean.execute(new TxRunnable() {
            public void run() {

                // code running in "REQUIRED" transactional scope
                // ... as "REQUIRED" is the default TxType
                System.out.println(Ebean.currentTransaction());

                Task task = Task.find.byId(34L);
                task.done = true;

                task.save();
            }
        });
        //#txrunnable

        assertThat(Task.find.byId(34L).done, is(true));
    }

    public class TransactionController extends Controller {

        //#annotation
        //###insert: import play.db.ebean.Transactional;

        //###insert: ...

        @Transactional
        public Result done(long id) {
            Task task = Task.find.byId(34L);
            task.done = true;

            task.save();
            return ok();
        }
        //#annotation

    }

    @Test
    public void traditional() {
        createTask();

        //#traditional
        Ebean.beginTransaction();
        try {
            Task task = Task.find.byId(34L);
            task.done = true;

            task.save();

            Ebean.commitTransaction();
        } finally {
            Ebean.endTransaction();
        }
        //#traditional

        assertThat(Task.find.byId(34L).done, is(true));
    }

    private void createTask() {
        Task task = new Task();
        task.id = 34L;
        task.name = "coco";
        task.save();
    }

}
