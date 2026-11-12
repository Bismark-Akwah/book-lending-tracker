record AudioKit(int id, String title) implements LendingItem {

    @Override
    public String typeLabel() {
        return "AUDIO_KIT";
    }

    @Override
    public int loanDays(MemberKind kind) {
        return kind == MemberKind.STUDENT ? 14 : 7;
    }
}