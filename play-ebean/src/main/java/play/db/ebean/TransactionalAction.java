/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import java.util.concurrent.CompletionStage;

import io.ebean.Ebean;
import play.mvc.Action;
import play.mvc.Http.Request;
import play.mvc.Result;

/**
 * Wraps an action in an Ebean transaction.
 */
public class TransactionalAction extends Action<Transactional> {
    public CompletionStage<Result> call(final Request req) {
        return Ebean.executeCall(() -> delegate.call(req));
    }
}
