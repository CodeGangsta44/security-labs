package edu.kpi.ip71.dovhopoliuk.random.model;

import java.util.Objects;

public class BetInfo {
    private String message;
    private AccountInfo account;
    private int realNumber;

    public BetInfo() {
    }

    public BetInfo(String message, AccountInfo account, int realNumber) {
        this.message = message;
        this.account = account;
        this.realNumber = realNumber;
    }

    @Override
    public String toString() {
        return "BetInfo{" +
                "message='" + message + '\'' +
                ", account=" + account +
                ", realNumber=" + realNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BetInfo betInfo = (BetInfo) o;
        return realNumber == betInfo.realNumber &&
                Objects.equals(message, betInfo.message) &&
                Objects.equals(account, betInfo.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, account, realNumber);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AccountInfo getAccount() {
        return account;
    }

    public void setAccount(AccountInfo account) {
        this.account = account;
    }

    public int getRealNumber() {
        return realNumber;
    }

    public void setRealNumber(int realNumber) {
        this.realNumber = realNumber;
    }
}
