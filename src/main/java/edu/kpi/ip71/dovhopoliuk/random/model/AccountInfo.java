package edu.kpi.ip71.dovhopoliuk.random.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class AccountInfo {
    private long id;
    private long money;
    private LocalDateTime deletionTime;

    public AccountInfo() {
    }

    public AccountInfo(int id, int money, LocalDateTime deletionTime) {
        this.id = id;
        this.money = money;
        this.deletionTime = deletionTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMoney() {
        return money;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public LocalDateTime getDeletionTime() {
        return deletionTime;
    }

    public void setDeletionTime(LocalDateTime deletionTime) {
        this.deletionTime = deletionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountInfo that = (AccountInfo) o;
        return id == that.id &&
                money == that.money &&
                Objects.equals(deletionTime, that.deletionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, money, deletionTime);
    }

    @Override
    public String toString() {
        return "AccountInfoDto{" +
                "id=" + id +
                ", money=" + money +
                ", deletionTime=" + deletionTime +
                '}';
    }
}
