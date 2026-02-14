package com.learning.langchain.shared.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class TableStats {

    private String table;
    private int rowCount;

    @Override
    public String toString() {
        return "TableStats{" +
                "table='" + table + '\'' +
                ", rowCount=" + rowCount +
                '}';
    }

}
