package org.readingroom.lending;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the lending rules of {@link Library}. */
class LibraryTest {

    private Library library;
    private Member kofi;
    private Member ama;
    private LendingItem novel;

    @BeforeEach
    void setUp() {
        library = new Library("Test Library");
        kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        ama = new Member("Ama Owusu", MemberKind.STUDENT);
        novel = new Book(1, "Things Fall Apart");
        library.addItem(novel);
        library.addMember(kofi);
        library.addMember(ama);
    }

    // ---- Loan periods: the original rules, preserved ----

    @ParameterizedTest(name = "{0} for a {1} member is {2} days")
    @CsvSource({
            "BOOK,      STANDARD, 14",
            "BOOK,      STUDENT,  28",
            "AUDIO_KIT, STANDARD, 7",
            "AUDIO_KIT, STUDENT,  14"
    })
    void loanPeriodDependsOnItemAndMemberKind(String itemType, MemberKind kind, int expectedDays) {
        LendingItem item = itemType.equals("BOOK")
                ? new Book(10, "Sample")
                : new AudioKit(10, "Sample");
        library.addItem(item);
        Member member = new Member("Sample Member", kind);

        library.borrow(member, item);

        assertEquals(expectedDays, library.loans().get(0).loanDays());
    }

    // ---- The rule that must never be broken ----

    @Test
    void itemAlreadyOnLoanCannotBeBorrowedAgain() {
        library.borrow(kofi, novel);

        ItemUnavailableException rejection =
                assertThrows(ItemUnavailableException.class, () -> library.borrow(ama, novel));

        assertEquals("\"Things Fall Apart\" is already on loan to Kofi Mensah", rejection.getMessage());
    }

    @Test
    void rejectedBorrowChangesNothing() {
        library.borrow(kofi, novel);

        assertThrows(ItemUnavailableException.class, () -> library.borrow(ama, novel));

        assertEquals(Optional.of("Kofi Mensah"), library.findBorrower(novel));
        assertEquals(1, library.loans().size());
        assertTrue(ama.history().isEmpty());
    }

    @Test
    void rejectedBorrowDoesNotConsumeALoanId() {
        library.borrow(kofi, novel);
        assertThrows(ItemUnavailableException.class, () -> library.borrow(ama, novel));
        library.returnItem(novel);

        library.borrow(ama, novel);

        assertEquals(2, library.loans().get(1).id());
    }

    @Test
    void returnedItemCanBeBorrowedByTheNextMember() {
        library.borrow(kofi, novel);
        library.returnItem(novel);
        assertTrue(library.findBorrower(novel).isEmpty());

        library.borrow(ama, novel);

        assertEquals(Optional.of("Ama Owusu"), library.findBorrower(novel));
    }

    @Test
    void copiesWithTheSameTitleAreTrackedIndependently() {
        LendingItem secondCopy = new Book(2, "Things Fall Apart");
        library.addItem(secondCopy);

        library.borrow(kofi, novel);
        assertDoesNotThrow(() -> library.borrow(ama, secondCopy));
        library.returnItem(novel);

        assertTrue(library.findBorrower(novel).isEmpty());
        assertEquals(Optional.of("Ama Owusu"), library.findBorrower(secondCopy));
    }

    // ---- Behaviour kept from the original ----

    @Test
    void returningAnItemThatIsNotOutIsHarmless() {
        assertDoesNotThrow(() -> library.returnItem(novel));

        assertTrue(library.findBorrower(novel).isEmpty());
        assertTrue(library.loans().isEmpty());
    }

    // ---- Guards against bad input ----

    @Test
    void duplicateItemIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> library.addItem(new AudioKit(1, "Different item, same id")));
    }

    @Test
    void itemWithNonPositiveLoanPeriodIsRejectedWithoutChangingState() {
        LendingItem broken = new BrokenItem(20, "Broken");
        library.addItem(broken);

        assertThrows(IllegalArgumentException.class, () -> library.borrow(kofi, broken));

        assertTrue(library.findBorrower(broken).isEmpty());
        assertTrue(library.loans().isEmpty());
        assertTrue(kofi.history().isEmpty());
    }

    @Test
    void memberHistoryCannotBeChangedFromOutside() {
        library.borrow(kofi, novel);

        assertThrows(UnsupportedOperationException.class, () -> kofi.history().add("forged entry"));
    }

    // ---- Extension: a new kind of item needs no change to Library ----

    @Test
    void newKindOfItemCanBeLentWithoutChangingLibrary() {
        LendingItem ebook = new EBook(30, "Digital Edition");
        library.addItem(ebook);

        library.borrow(kofi, ebook);

        assertEquals(21, library.loans().get(0).loanDays());
        assertThrows(ItemUnavailableException.class, () -> library.borrow(ama, ebook));
    }

    /** Stands in for the planned e-loans: its own rules, defined entirely outside Library. */
    private record EBook(int id, String title) implements LendingItem {
        @Override
        public String typeLabel() {
            return "EBOOK";
        }

        @Override
        public int loanDays(MemberKind kind) {
            return 21;
        }
    }

    /** An item written badly on purpose, to prove Library protects itself. */
    private record BrokenItem(int id, String title) implements LendingItem {
        @Override
        public String typeLabel() {
            return "BROKEN";
        }

        @Override
        public int loanDays(MemberKind kind) {
            return 0;
        }
    }
}