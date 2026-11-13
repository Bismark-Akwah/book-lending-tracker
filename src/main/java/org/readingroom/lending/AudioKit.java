package org.readingroom.lending;

import java.util.Objects;

/** An audio course kit: 7 days for standard members, 14 for students. */
public record AudioKit(int id, String title) implements LendingItem {

    public AudioKit {
        Objects.requireNonNull(title, "title");
    }

    @Override
    public String typeLabel() {
        return "AUDIO_KIT";
    }

    @Override
    public int loanDays(MemberKind kind) {
        return kind == MemberKind.STUDENT ? 14 : 7;
    }
}