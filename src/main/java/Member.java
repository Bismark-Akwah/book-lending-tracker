import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A person who can borrow items. */
final class Member {
    private final String name;
    private final MemberKind kind;
    private final List<String> history = new ArrayList<>();

    Member(String name, MemberKind kind) {
        this.name = Objects.requireNonNull(name, "name");
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    String name() {
        return name;
    }

    MemberKind kind() {
        return kind;
    }

    /** Read-only copy, so callers cannot rewrite a member's history. */
    List<String> history() {
        return List.copyOf(history);
    }

    void recordBorrowed(String itemTitle) {
        history.add("Borrowed " + itemTitle);
    }
}