package de.jsilbereisen.perfumator.model;

import com.github.javaparser.Position;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a range of code by starting line/column and ending line/column.
 */
@Data
public class CodeRange implements Comparable<CodeRange> {

    private int beginLine;

    private int endLine;

    private int beginColumn;

    private int endColumn;

    public CodeRange() { }

    public CodeRange(int beginLine, int endLine) {
        this.beginLine = beginLine;
        this.endLine = endLine;
    }

    public CodeRange(int beginLine, int beginColumn, int endLine, int endColumn) {
        this(beginLine, endLine);

        this.beginColumn = beginColumn;
        this.endColumn = endColumn;
    }

    @NotNull
    public static CodeRange of(int beginLine, int endLine) {
        return new CodeRange(beginLine, endLine);
    }

    @NotNull
    public static CodeRange of(int beginLine, int beginColumn, int endLine, int endColumn) {
        return new CodeRange(beginLine, beginColumn, endLine, endColumn);
    }

    @NotNull
    public static Optional<CodeRange> of(@NotNull NodeWithRange<?> node) {
        if (node.getBegin().isEmpty() || node.getEnd().isEmpty()) {
            return Optional.empty();
        }

        Position begin = node.getBegin().get();
        Position end = node.getEnd().get();

        return Optional.of(new CodeRange(begin.line, begin.column, end.line, end.column));
    }

    @Override
    public int compareTo(@NotNull CodeRange other) {
        int comparison = beginLine - other.beginLine;
        if (comparison != 0) {
            return comparison;
        }

        comparison = endLine - other.endLine;
        if (comparison != 0) {
            return comparison;
        }

        comparison = beginColumn - other.beginColumn;
        if (comparison != 0) {
            return comparison;
        }

        return endColumn - other.endColumn;
    }

    @Override
    public @NotNull String toString() {
        boolean areColumnsNonZero = beginColumn != 0 || endColumn != 0;

        if (areColumnsNonZero) {
            return String.format("%d:%d-%d:%d", beginLine, beginColumn, endLine, endColumn);
        } else {
            return String.format("%d-%d", beginLine, endLine);
        }
    }
}
