/*
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.TxCallable;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * Wraps an action in an Ebean transaction.
 */
public class TransactionalAction extends Action<Transactional> {
    
    public F.Promise<Result> call(final Context ctx) throws Throwable {
        Action<Transactional> action = new Action<Transactional>() {
            @Override
            public F.Promise<Result> call(Context context) throws Throwable {
                return Ebean.execute(new TxCallable<F.Promise<Result>>() {
                    public F.Promise<Result> call() {
                        try {
                            return delegate.call(ctx);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    }
                });
            }
        };

        Action<?> previousDelegate = this;
        Action<?> lastDelegate = delegate;

        while (lastDelegate.delegate != null) {
            previousDelegate = lastDelegate;
            lastDelegate = lastDelegate.delegate;
        }

        action.delegate = lastDelegate;
        previousDelegate.delegate = action;

        return delegate.call(ctx);
    }
    
}
