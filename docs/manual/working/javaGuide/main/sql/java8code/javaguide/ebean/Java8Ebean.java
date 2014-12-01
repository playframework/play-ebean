package javaguide.ebean;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxRunnable;
import org.junit.*;
import play.test.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static play.test.Helpers.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class Java8Ebean extends WithApplication {

    @Override
    protected FakeApplication provideFakeApplication() {
        Map<String, String> config = new HashMap<String, String>();
        config.putAll(inMemoryDatabase());
        config.put("ebean.default", "javaguide.ebean.Task");
        return fakeApplication(config);
    }

    @Test
    public void txRunnable() {

        createTask();

        //#txrunnable
        //###insert: import com.avaje.ebean.*;

        //###insert: ...

        Ebean.execute(() -> {
            // code running in "REQUIRED" transactional scope
            // ... as "REQUIRED" is the default TxType
            System.out.println(Ebean.currentTransaction());

            Task task = Task.find.byId(34L);
            task.done = true;

            task.save();
        });
        //#txrunnable

        assertThat(Task.find.byId(34L).done, is(true));
    }

    private void createTask() {
        Task task = new Task();
        task.id = 34L;
        task.name = "coco";
        task.save();
    }

}
