package org.readingroom.lending;

import java.util.Objects;

/** A physical book: 14 days for standard members, 28 for students. */
public record Book(int id, String title) implements LendingItem {

    public Book {
        Objects.requireNonNull(title, "title");
    }

    @Override
    public String typeLabel() {
        return "BOOK";
    }

    @Override
    public int loanDays(MemberKind kind) {
        return kind == MemberKind.STUDENT ? 28 : 14;
    }
}