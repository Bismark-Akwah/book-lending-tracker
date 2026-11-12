import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Library {
    public String name;
    public List<LendingItem> items = new ArrayList<>();
    public List<Member> members = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    // TEMPORARY: reproduces the old item.currentBorrower behaviour (including its
    // flaw) so this commit changes structure only. Replaced by the single
    // activeLoans map in the "enforce one active loan per item" commit.
    private final Map<Integer, String> borrowerByItemId = new HashMap<>();

    public Library(String name) {
        this.name = name;
    }

    public void addItem(LendingItem i) {
        items.add(i);
    }

    public void addMember(Member m) {
        members.add(m);
    }

    // Lend an item to a member. The item decides the loan period.
    public void borrow(Member m, LendingItem item) {
        int loanDays = item.loanDays(m.kind);
        borrowerByItemId.put(item.id(), m.name);
        System.out.println("Lent \"" + item.title() + "\" to " + m.name + " for " + loanDays + " days");
        m.history.add("Borrowed " + item.title());
        loans.add(new Loan(m.name, item.title(), loanDays));
    }

    public void returnItem(LendingItem item) {
        borrowerByItemId.remove(item.id());
        for (Loan l : loans) {
            if (l.itemTitle.equals(item.title()) && l.status.equals("ACTIVE")) {
                l.status = "RETURNED";
            }
        }
        System.out.println("Returned \"" + item.title() + "\"");
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public void printReport() {
        System.out.println();
        System.out.println("=== " + name + " - end of day ===");
        System.out.println("Items:");
        for (LendingItem i : items) {
            String borrower = borrowerByItemId.get(i.id());
            String status = borrower != null ? "ON_LOAN" : "AVAILABLE";
            System.out.println("  \"" + i.title() + "\" (" + i.typeLabel() + "): " + status
                    + (borrower != null ? " - with " + borrower : ""));
        }
        System.out.println("Active loans:");
        for (Loan l : loans) {
            if (l.status.equals("ACTIVE")) {
                System.out.println("  Loan #" + l.id + ": \"" + l.itemTitle + "\" -> " + l.memberName);
            }
        }
    }
}