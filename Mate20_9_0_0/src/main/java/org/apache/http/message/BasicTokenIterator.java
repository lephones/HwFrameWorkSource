package org.apache.http.message;

import java.util.NoSuchElementException;
import org.apache.http.HeaderIterator;
import org.apache.http.ParseException;
import org.apache.http.TokenIterator;

@Deprecated
public class BasicTokenIterator implements TokenIterator {
    public static final String HTTP_SEPARATORS = " ,;=()<>@:\\\"/[]?{}\t";
    protected String currentHeader;
    protected String currentToken;
    protected final HeaderIterator headerIt;
    protected int searchPos;

    public BasicTokenIterator(HeaderIterator headerIterator) {
        if (headerIterator != null) {
            this.headerIt = headerIterator;
            this.searchPos = findNext(-1);
            return;
        }
        throw new IllegalArgumentException("Header iterator must not be null.");
    }

    public boolean hasNext() {
        return this.currentToken != null;
    }

    public String nextToken() throws NoSuchElementException, ParseException {
        if (this.currentToken != null) {
            String result = this.currentToken;
            this.searchPos = findNext(this.searchPos);
            return result;
        }
        throw new NoSuchElementException("Iteration already finished.");
    }

    public final Object next() throws NoSuchElementException, ParseException {
        return nextToken();
    }

    public final void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Removing tokens is not supported.");
    }

    protected int findNext(int from) throws ParseException {
        if (from >= 0) {
            from = findTokenSeparator(from);
        } else if (!this.headerIt.hasNext()) {
            return -1;
        } else {
            this.currentHeader = this.headerIt.nextHeader().getValue();
            from = 0;
        }
        int start = findTokenStart(from);
        if (start < 0) {
            this.currentToken = null;
            return -1;
        }
        int end = findTokenEnd(start);
        this.currentToken = createToken(this.currentHeader, start, end);
        return end;
    }

    protected String createToken(String value, int start, int end) {
        return value.substring(start, end);
    }

    protected int findTokenStart(int from) {
        if (from >= 0) {
            boolean found = false;
            while (!found && this.currentHeader != null) {
                int to = this.currentHeader.length();
                while (!found && from < to) {
                    char ch = this.currentHeader.charAt(from);
                    if (isTokenSeparator(ch) || isWhitespace(ch)) {
                        from++;
                    } else if (isTokenChar(this.currentHeader.charAt(from))) {
                        found = true;
                    } else {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Invalid character before token (pos ");
                        stringBuilder.append(from);
                        stringBuilder.append("): ");
                        stringBuilder.append(this.currentHeader);
                        throw new ParseException(stringBuilder.toString());
                    }
                }
                if (!found) {
                    if (this.headerIt.hasNext()) {
                        this.currentHeader = this.headerIt.nextHeader().getValue();
                        from = 0;
                    } else {
                        this.currentHeader = null;
                    }
                }
            }
            return found ? from : -1;
        } else {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Search position must not be negative: ");
            stringBuilder2.append(from);
            throw new IllegalArgumentException(stringBuilder2.toString());
        }
    }

    protected int findTokenSeparator(int from) {
        if (from >= 0) {
            boolean found = false;
            int to = this.currentHeader.length();
            while (!found && from < to) {
                char ch = this.currentHeader.charAt(from);
                StringBuilder stringBuilder;
                if (isTokenSeparator(ch)) {
                    found = true;
                } else if (isWhitespace(ch)) {
                    from++;
                } else if (isTokenChar(ch)) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Tokens without separator (pos ");
                    stringBuilder.append(from);
                    stringBuilder.append("): ");
                    stringBuilder.append(this.currentHeader);
                    throw new ParseException(stringBuilder.toString());
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Invalid character after token (pos ");
                    stringBuilder.append(from);
                    stringBuilder.append("): ");
                    stringBuilder.append(this.currentHeader);
                    throw new ParseException(stringBuilder.toString());
                }
            }
            return from;
        }
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Search position must not be negative: ");
        stringBuilder2.append(from);
        throw new IllegalArgumentException(stringBuilder2.toString());
    }

    protected int findTokenEnd(int from) {
        if (from >= 0) {
            int to = this.currentHeader.length();
            int end = from + 1;
            while (end < to && isTokenChar(this.currentHeader.charAt(end))) {
                end++;
            }
            return end;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Token start position must not be negative: ");
        stringBuilder.append(from);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    protected boolean isTokenSeparator(char ch) {
        return ch == ',';
    }

    protected boolean isWhitespace(char ch) {
        return ch == 9 || Character.isSpaceChar(ch);
    }

    protected boolean isTokenChar(char ch) {
        if (Character.isLetterOrDigit(ch)) {
            return true;
        }
        if (Character.isISOControl(ch) || isHttpSeparator(ch)) {
            return false;
        }
        return true;
    }

    protected boolean isHttpSeparator(char ch) {
        return HTTP_SEPARATORS.indexOf(ch) >= 0;
    }
}