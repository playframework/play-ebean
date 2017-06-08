/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import java.util.concurrent.CompletionStage;

import io.ebean.Ebean;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * Wraps an action in an Ebean transaction.
 */
public class TransactionalAction extends Action<Transactional> {
    public CompletionStage<Result> call(final Context ctx) {
        return Ebean.execute(() -> delegate.call(ctx));
    }
}
