package io.github.ashwithpoojary98;

public class UnsafeLiteral {

    private String value;

    public UnsafeLiteral(String value, boolean replaceQuotes) {
        if (value == null) {
            value = "";
        }

        if (replaceQuotes) {
            value = value.replace("'", "''");
        }

        this.value = value;
    }

    // Getter for the value
    public String getValue() {
        return value;
    }

    // Optional: Setter if you need to modify the value later
    public void setValue(String value) {
        this.value = value;
    }

    // Overloaded constructor for default value of replaceQuotes
    public UnsafeLiteral(String value) {
        this(value, true); // Default to true
    }
}
