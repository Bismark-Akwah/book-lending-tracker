// The Reading Room - community library lending tracker
// Original program, built by a volunteer. New volunteers are told not to touch it.
// Run it first to see the sort of thing they are worried about, and look closely
// at the items and active loans in the end-of-day report.

public class LendingTracker {

    public static void main(String[] args) {
        Library lib = new Library("The Reading Room");

        LendingItem novel = new Book(1, "Things Fall Apart");
        LendingItem audioCourse = new AudioKit(2, "French Audio Course");
        lib.addItem(novel);
        lib.addItem(audioCourse);

        Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
        lib.addMember(kofi);
        lib.addMember(ama);

        // a normal afternoon
        lib.borrow(kofi, novel);
        lib.borrow(ama, audioCourse);
        lib.returnItem(audioCourse);

        // ... and the sort of thing that worries them:
        lib.borrow(ama, novel); // the novel is already out with Kofi, but nothing stops this

        lib.printReport();
    }
}