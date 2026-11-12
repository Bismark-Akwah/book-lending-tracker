package org.readingroom.lending;

/** Thrown when someone tries to lend an item that is already out on loan. */
public final class ItemUnavailableException extends RuntimeException {

    ItemUnavailableException(String itemTitle, String borrowerName) {
        super("\"" + itemTitle + "\" is already on loan to " + borrowerName);
    }
}