package net.kaisoz.droidstorm.nxt.message;

/**
 * Abstracts a response received when a file searched.
 * Contains the opened file handler and the name of the file found
 *
 * @author Tomás Tormo Franco
 */
public class FindFileResponse {

    protected char[] mValues;
    public static final char NO_MORE_FILES = 0x0083;
    public static final char NO_MORE_HANDLES = 0x0081;
    public static final char FILE_NOT_FOUND = 0x0087;

    public FindFileResponse(char[] values) {
        this.mValues = values;
    }

    /**
     * Returns operation status
     *
     * @return
     */
    public char getStatus() {
        return (char) ((mValues[1] & 0xFF00) >> 8);
    }

    /**
     * Returns a handler for the found file name
     *
     * @return
     */
    public char getHandle() {
        return (char) (mValues[1] & 0x00FF);
    }

    /**
     * Returns the found file name
     *
     * @return
     */
    public String getFileName() {
        StringBuilder builder = null;
        if (mValues != null) {
            builder = new StringBuilder();
            int i = 2;
            while (i <= 11) {
                char fLetter = (char) ((mValues[i] & 0xFF00) >> 8);
                if (fLetter == ' ') break;
                builder.append(fLetter);
                char sLetter = (char) (mValues[i] & 0x00FF);
                if (sLetter == ' ') break;
                builder.append(sLetter);
                i++;
            }
            return builder.toString().trim();
        }
        return null;
    }
}
