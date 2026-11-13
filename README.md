# The Reading Room: Community Library Lending Tracker

A small lending tracker for a volunteer-run community library. This repository is a rebuild of the original single-file program. It behaves like the original, with one deliberate exception: **an item can now be on only one loan at a time**.

See [`DIAGNOSIS.md`](DIAGNOSIS.md) for the written diagnosis of the original code and the design sketch, which was committed before any code change.



## Running it

Requires JDK 21 and Maven.

```
mvn test                                                    # run all tests
mvn compile
java -cp target/classes org.readingroom.LendingTracker      # run the demo
```

## Project layout

```
src/main/java/org/readingroom/
    LendingTracker.java                 demo program (uses only the public API)
    lending/
        Library.java                    owns all loan state; enforces one active loan per item
        LendingItem.java                interface: id, title, typeLabel, loanDays(MemberKind)
        Book.java, AudioKit.java        records; each owns its own loan rules
        Member.java, MemberKind.java    a borrower, and STANDARD / STUDENT
        Loan.java, LoanStatus.java      one item lent to one member
        ItemUnavailableException.java   thrown when an item is already out
src/test/java/...                       unit tests, a randomized rule test and an end-to-end output test
src/test/resources/
    golden-original.txt                 output of the ORIGINAL program, captured before any change
    golden-rebuilt.txt                  approved output of this version
```

## Behaviour: what changed and what did not

The original output was saved as `golden-original.txt` before any refactoring. During the refactoring commits the program's output was compared against it and did not change. The one intended change is in the commit `fix: enforce one active loan per item`.

```
original                                                     rebuilt
Lent "Things Fall Apart" to Ama Owusu for 28 days      -->   Rejected: "Things Fall Apart" is already on loan to Kofi Mensah
  "Things Fall Apart" (BOOK): ON_LOAN - with Ama Owusu -->     "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah
  Loan #3: "Things Fall Apart" -> Ama Owusu            -->   (no second loan)
```

You can reproduce the comparison with:

```
git diff --no-index src/test/resources/golden-original.txt src/test/resources/golden-rebuilt.txt
```

Everything else is the same as before: the same console messages, the same loan periods (book 14 days, or 28 for students; audio kit 7 days, or 14 for students), the same report layout, and loan ids that count up from 1.

Known quirk kept from the original: returning an item that is not on loan does nothing but still prints `Returned "..."`. I left it alone because this is a rebuild, not a redesign.

## Design decisions

### 1. One owner of loan state, with availability derived, not stored

In the original, "is this item out, and with whom?" was stored twice: on the item (`status`, `currentBorrower`) and in the loan list. Nothing kept the two in agreement, and `borrow()` never checked, which is how one book could be out to two people.

Now `Library` is the only place that holds this, and an item's availability is **derived** from its active loan. Items have no status field, so the contradictory state cannot be represented. `borrow()` throws `ItemUnavailableException` if the item already has an active loan, and a rejected borrow changes nothing (no loan, no history entry, no loan id used).

*Alternative I rejected:* adding `if (item.status == ON_LOAN)` to the old code. That fixes the symptom but leaves two copies of the truth that can drift apart again.

### 2. A plain interface for items, not an enum and not a sealed interface

Each kind of item answers `loanDays(MemberKind)` itself, so the borrowing code never asks "what type are you?". The old `if BOOK ... else if AUDIO_KIT` chain, with its copy-pasted branches, is gone.

- *Enum of item types with a switch:* every new type would mean editing a central switch, which is the problem the volunteers described.
- *Sealed interface:* it would give compiler-checked exhaustive switches, but nothing here switches over item types, and it would force an edit to the `permits` list for every new item kind. That conflicts with "adding e-loans must not force a rewrite".
- *Abstract base class:* books and audio kits share a contract, not state or behaviour, so inheritance adds coupling for no gain.

Item ids are supplied when an item is created, and `Library.addItem` rejects duplicates. Records compare by value, so without an id two copies of the same title would be indistinguishable, which is a weakness of the original (it matched loans by title).

### 3. Encapsulation

All fields are private. `Loan` and `Member` have no public mutators: only `Library`, which lives in the same package, can create or close a loan or add to a member's history (those methods are package-private). Collections returned to callers are read-only copies. Constructors validate their input, and a loan period of zero or less is rejected, which protects against a badly written future item type.

### 4. Modern Java, only where it helps

- **Records** for `Book` and `AudioKit`: immutable by construction, with no boilerplate.
- **Enums** for `MemberKind` and `LoanStatus`, replacing strings that could be mistyped.
- **`Optional`** for "who has this item?", instead of returning null.
- **Streams** only for the "find the active loan" lookup, where they read more clearly than a loop.

### 5. A known trade-off

`Library` still prints the "Lent" and "Returned" confirmation lines and the report, because that kept the output identical to the original. A cleaner design would return events and let a separate class print them. I did not do that because it was not needed for the brief.

## Changes from my original sketch

`DIAGNOSIS.md` was committed before any code, so it still shows the first plan. What changed while building:

- **No separate `activeLoans` map.** Availability is derived from the single loan list, so there is one source of truth (see decision 1). A second map would have recreated the original two-sources problem on a smaller scale.
- **No `ReportPrinter` class.** `Library` still prints the report, which kept the output identical to the original. Extracting it was a tidy-up the brief did not need.
- **Accessor names.** The sketch shows `getItems()` and `getActiveLoans()`. The code uses `items()` and `loans()`, and callers filter on `Loan.isActive()`.
- **Everything else matches the sketch:** the `LendingItem` interface with `Book` and `AudioKit` records, the `MemberKind` and `LoanStatus` enums, `ItemUnavailableException`, and `itemId` on `Loan`.

## How this makes e-loans easy to add

The library has said e-loans (digital items with their own borrowing rules) are coming.

**New version:** write one new file. Nothing in `Library`, `Loan`, `Member` or the report changes.

**Old version:** `borrow()` would have needed a third branch, copying the status, borrower and print code from the other two. Worse, if someone forgot to add the branch, an unknown type silently produced a 0-day loan without marking the item as out. The original gave no warning for that.

The test `newKindOfItemCanBeLentWithoutChangingLibrary` proves it: it defines a new item type inside the test file and lends it through an unmodified `Library`.

*Limit of the current design:* an item type can set its own loan period, but every item still allows only one borrower at a time. If e-loans ever allow several people to hold the same digital copy at once, `Library` would need a small extension, such as asking the item how many simultaneous loans it allows. I have not built that, since the business has not described it.

## How to add a new kind of item

1. Create a record that implements `LendingItem`. For example, `src/main/java/org/readingroom/lending/EBook.java`:

   ```java
   package org.readingroom.lending;

   /** A digital book: 21 days for everyone. */
   public record EBook(int id, String title) implements LendingItem {

       @Override
       public String typeLabel() {
           return "EBOOK";
       }

       @Override
       public int loanDays(MemberKind kind) {
           return 21;
       }
   }
   ```

2. Register it: `library.addItem(new EBook(3, "Digital Edition"));`
3. Add a test next to the existing loan-period tests, then run `mvn test`.

The loan period must be positive, and the id must not already be in use. `Library` rejects both with a clear error.

## Improvement beyond the brief: a randomized test of the one-loan rule

The rule the library said must never be broken is that an item cannot be on two loans at once. The hand-written tests check specific cases, but volunteers adding e-loans will create sequences nobody anticipated.

`LibraryInvariantTest` runs 2,000 random borrows and returns, including two copies of the same title, with a fixed seed so any failure can be reproduced. After every step it checks the library's answer against an independent record of who should hold each item, and that no item has more than one active loan. If a future change breaks the rule, the test fails and reports the step where it happened.

I checked that it works by temporarily removing the availability check in `Library.borrow`: the test failed immediately, and passed again once the check was restored. It needs no new dependency and does not change what the tool does, so it protects the library without altering behaviour.

## Tests

There are 16 tests: all four loan periods, the one-loan rule, that a rejected borrow changes nothing, that a returned item can be borrowed again, that copies with the same title stay separate, input checks, the output of the whole demo, the e-loan extension point, and the randomized rule test above. Run them with `mvn test`.

## Commit history

The history is deliberate: diagnosis first, then a test that captures the original output, then refactoring steps that each keep that output identical, then the single behaviour change, then the remaining tests and documentation. Squashing it would hide that evidence, so the rebuild branch was merged with a merge commit.