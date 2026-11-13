package org.readingroom.lending;

/** Something the library can lend. Each kind of item owns its own loan rules. */
public interface LendingItem {

    int id();

    String title();

    /** Label used in the end-of-day report, for example "BOOK". */
    String typeLabel();

    /** Number of days this item may be borrowed by a member of the given kind. */
    int loanDays(MemberKind kind);
}