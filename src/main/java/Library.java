import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The lending desk: stocks items, registers members, lends and takes items back.
 * It is the only owner of loan state, and it guarantees an item is on at most one
 * active loan at a time.
 */
final class Library {
    private final String name;
    private final List<LendingItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private int nextLoanId = 1;

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

    /**
     * Lends an item to a member. The item decides the loan period.
     *
     * @throws ItemUnavailableException if the item is already on loan; nothing changes in that case
     */
    void borrow(Member member, LendingItem item) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(item, "item");

        Optional<Loan> existing = findActiveLoan(item);
        if (existing.isPresent()) {
            throw new ItemUnavailableException(item.title(), existing.get().memberName());
        }

        int loanDays = item.loanDays(member.kind());
        Loan loan = new Loan(nextLoanId, item.id(), member.name(), item.title(), loanDays);
        nextLoanId++;
        loans.add(loan);
        member.recordBorrowed(item.title());
        System.out.println("Lent \"" + item.title() + "\" to " + member.name() + " for " + loanDays + " days");
    }

    /** Takes an item back. Returning an item that is not out is harmless, as in the original. */
    void returnItem(LendingItem item) {
        Objects.requireNonNull(item, "item");
        findActiveLoan(item).ifPresent(Loan::markReturned);
        System.out.println("Returned \"" + item.title() + "\"");
    }

    /** Name of the member currently holding the item, or empty if it is available. */
    Optional<String> findBorrower(LendingItem item) {
        return findActiveLoan(item).map(Loan::memberName);
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
            Optional<String> borrower = findBorrower(item);
            String status = borrower.isPresent() ? "ON_LOAN" : "AVAILABLE";
            System.out.println("  \"" + item.title() + "\" (" + item.typeLabel() + "): " + status
                    + borrower.map(b -> " - with " + b).orElse(""));
        }
        System.out.println("Active loans:");
        for (Loan loan : loans) {
            if (loan.isActive()) {
                System.out.println("  Loan #" + loan.id() + ": \"" + loan.itemTitle() + "\" -> " + loan.memberName());
            }
        }
    }

    private Optional<Loan> findActiveLoan(LendingItem item) {
        return loans.stream()
                .filter(loan -> loan.itemId() == item.id() && loan.isActive())
                .findFirst();
    }
}