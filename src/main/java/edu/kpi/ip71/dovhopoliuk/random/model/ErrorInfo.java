package edu.kpi.ip71.dovhopoliuk.random.model;

import java.util.Objects;

public class ErrorInfo {
    private String error;

    @Override
    public String toString() {
        return "ErrorInfo{" +
                "error='" + error + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorInfo errorInfo = (ErrorInfo) o;
        return Objects.equals(error, errorInfo.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ErrorInfo() {
    }

    public ErrorInfo(String error) {
        this.error = error;
    }
}
