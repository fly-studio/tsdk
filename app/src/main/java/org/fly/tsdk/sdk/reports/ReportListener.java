package org.fly.tsdk.sdk.reports;

import org.fly.tsdk.query.exceptions.ResponseException;
import org.fly.tsdk.sdk.models.ReportResult;

public interface ReportListener<T extends ReportResult> {

    void dispatchResult(T result, ResponseException e);
}
