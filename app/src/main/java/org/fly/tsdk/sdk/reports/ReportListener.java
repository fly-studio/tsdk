package org.fly.tsdk.sdk.reports;

import org.fly.tsdk.sdk.models.ReportResult;
import org.fly.tsdk.sdk.query.exceptions.ResponseException;

public interface ReportListener<T extends ReportResult> {

    void callback(T result, ResponseException e);
}
