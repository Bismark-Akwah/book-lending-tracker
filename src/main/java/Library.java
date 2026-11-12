import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** The lending desk: stocks items, registers members, lends and takes items back. */
final class Library {
    private final String name;
    private final List<LendingItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private int nextLoanId = 1;

    // TEMPORARY: reproduces the old item.currentBorrower behaviour (including its
    // flaw) so this commit changes structure only. Replaced by the single
    // activeLoans map in the "enforce one active loan per item" commit.
    private final Map<Integer, String> borrowerByItemId = new HashMap<>();

    Library(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    String name() {
        return name;
    }

    void addItem(LendingItem item) {
        Objects.requireNonNull(item, "item");
        boolean idTaken = items.stream().anyMatch(existing -> existing.id() == item.id());
        if (idTaken) {
            throw new IllegalArgumentException("An item with id " + item.id() + " already exists");
        }
        items.add(item);
    }

    void addMember(Member member) {
        members.add(Objects.requireNonNull(member, "member"));
    }

    /** Lends an item to a member. The item decides the loan period. */
    void borrow(Member member, LendingItem item) {
        int loanDays = item.loanDays(member.kind());
        borrowerByItemId.put(item.id(), member.name());
        System.out.println("Lent \"" + item.title() + "\" to " + member.name() + " for " + loanDays + " days");
        member.recordBorrowed(item.title());
        loans.add(new Loan(nextLoanId++, item.id(), member.name(), item.title(), loanDays));
    }

    void returnItem(LendingItem item) {
        borrowerByItemId.remove(item.id());
        for (Loan loan : loans) {
            if (loan.itemTitle().equals(item.title()) && loan.isActive()) {
                loan.markReturned();
            }
        }
        System.out.println("Returned \"" + item.title() + "\"");
    }

    List<LendingItem> items() {
        return List.copyOf(items);
    }

    List<Member> members() {
        return List.copyOf(members);
    }

    List<Loan> loans() {
        return List.copyOf(loans);
    }

    void printReport() {
        System.out.println();
        System.out.println("=== " + name + " - end of day ===");
        System.out.println("Items:");
        for (LendingItem item : items) {
            String borrower = borrowerByItemId.get(item.id());
            String status = borrower != null ? "ON_LOAN" : "AVAILABLE";
            System.out.println("  \"" + item.title() + "\" (" + item.typeLabel() + "): " + status
                    + (borrower != null ? " - with " + borrower : ""));
        }
        System.out.println("Active loans:");
        for (Loan loan : loans) {
            if (loan.isActive()) {
                System.out.println("  Loan #" + loan.id() + ": \"" + loan.itemTitle() + "\" -> " + loan.memberName());
            }
        }
    }
}