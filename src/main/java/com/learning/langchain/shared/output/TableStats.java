package com.learning.langchain.shared.output;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
