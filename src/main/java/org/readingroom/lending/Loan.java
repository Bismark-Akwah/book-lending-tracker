package org.readingroom.lending;

import java.util.Objects;

/** A record of one item lent to one member. Created and closed only by {@link Library}. */
public final class Loan {
    private final int id;
    private final int itemId;
    private final String memberName;
    private final String itemTitle;
    private final int loanDays;
    private LoanStatus status = LoanStatus.ACTIVE;

    Loan(int id, int itemId, String memberName, String itemTitle, int loanDays) {
        if (loanDays <= 0) {
            throw new IllegalArgumentException("Loan period must be positive, was " + loanDays);
        }
        this.id = id;
        this.itemId = itemId;
        this.memberName = Objects.requireNonNull(memberName, "memberName");
        this.itemTitle = Objects.requireNonNull(itemTitle, "itemTitle");
        this.loanDays = loanDays;
    }

    public int id() {
        return id;
    }

    public int itemId() {
        return itemId;
    }

    public String memberName() {
        return memberName;
    }

    public String itemTitle() {
        return itemTitle;
    }

    public int loanDays() {
        return loanDays;
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    void markReturned() {
        status = LoanStatus.RETURNED;
    }
}