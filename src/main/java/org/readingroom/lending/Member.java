package org.readingroom.lending;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A person who can borrow items. */
public final class Member {
    private final String name;
    private final MemberKind kind;
    private final List<String> history = new ArrayList<>();

    public Member(String name, MemberKind kind) {
        this.name = Objects.requireNonNull(name, "name");
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    public String name() {
        return name;
    }

    public MemberKind kind() {
        return kind;
    }

    /** Read-only copy, so callers cannot rewrite a member's history. */
    public List<String> history() {
        return List.copyOf(history);
    }

    void recordBorrowed(String itemTitle) {
        history.add("Borrowed " + itemTitle);
    }
}