record Book(int id, String title) implements LendingItem {

    @Override
    public String typeLabel() {
        return "BOOK";
    }

    @Override
    public int loanDays(MemberKind kind) {
        return kind == MemberKind.STUDENT ? 28 : 14;
    }
}