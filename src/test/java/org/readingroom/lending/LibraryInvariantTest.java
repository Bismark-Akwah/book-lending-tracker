package org.readingroom.lending;

import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Hammers the library with random borrows and returns and checks the rule the
 * business says must never be broken: an item is on at most one loan at a time.
 */
class LibraryInvariantTest {

    private static final int STEPS = 2_000;
    private static final long SEED = 2026L; // fixed, so any failure can be reproduced

    @Test
    void itemIsNeverOnTwoActiveLoansWhateverTheSequenceOfActions() {
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(OutputStream.nullOutputStream())); // keep test output quiet
            runRandomDay(new Random(SEED));
        } finally {
            System.setOut(originalOut);
        }
    }

    private void runRandomDay(Random random) {
        Library library = new Library("Invariant Library");
        List<LendingItem> items = List.of(
                new Book(1, "Alpha"), new Book(2, "Alpha"), // two copies of one title
                new AudioKit(3, "Bravo"), new AudioKit(4, "Charlie"), new Book(5, "Delta"));
        List<Member> members = List.of(
                new Member("Kofi", MemberKind.STANDARD),
                new Member("Ama", MemberKind.STUDENT),
                new Member("Yaw", MemberKind.STANDARD));
        items.forEach(library::addItem);
        members.forEach(library::addMember);

        Map<Integer, String> expectedBorrower = new HashMap<>(); // independent record of the truth

        for (int step = 0; step < STEPS; step++) {
            LendingItem item = items.get(random.nextInt(items.size()));

            if (random.nextBoolean()) {
                Member member = members.get(random.nextInt(members.size()));
                if (expectedBorrower.containsKey(item.id())) {
                    assertThrows(ItemUnavailableException.class, () -> library.borrow(member, item));
                } else {
                    library.borrow(member, item);
                    expectedBorrower.put(item.id(), member.name());
                }
            } else {
                library.returnItem(item);
                expectedBorrower.remove(item.id());
            }

            assertRuleHolds(library, items, expectedBorrower, step);
        }
    }

    private void assertRuleHolds(Library library, List<LendingItem> items,
                                 Map<Integer, String> expectedBorrower, int step) {
        Map<Integer, Long> activeLoansPerItem = library.loans().stream()
                .filter(Loan::isActive)
                .collect(Collectors.groupingBy(Loan::itemId, Collectors.counting()));

        for (long count : activeLoansPerItem.values()) {
            assertTrue(count <= 1, "An item had two active loans at step " + step);
        }
        for (LendingItem item : items) {
            assertEquals(Optional.ofNullable(expectedBorrower.get(item.id())),
                    library.findBorrower(item), "Wrong borrower for item " + item.id() + " at step " + step);
        }
        assertEquals(expectedBorrower.size(), activeLoansPerItem.size(),
                "Number of active loans disagrees at step " + step);
    }
}