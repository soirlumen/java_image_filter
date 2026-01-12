package image;

/**
 * V průběhu dlouhých operací je občas zavolána, aby hlásila, kolik procent je již splněno
 */
@FunctionalInterface
public interface ProgressPercents {
    void update(int percent); // 0-100
}